package service.exam;

import exceptions.ExamGenerationException;
import models.Chapter;
import models.ExamType;
import models.Points;
import models.Subtask;
import models.SubtaskDifficulty;
import models.Variant;
import service.exam.dto.CandidateTask;
import service.exam.dto.GenerateExamValues;
import service.exam.dto.GeneratedChapter;
import service.exam.dto.GeneratedExam;
import service.exam.dto.GeneratedSubtask;
import service.exam.dto.SelectionState;
import validation.exam.ExamGenerationValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class ExamGenerationService {

    private final Random random = new Random();
    private final ExamGenerationValidator examGenerationValidator = new ExamGenerationValidator();

    /**
     * Generates an exam for the provided input values.
     *
     * @param generateExamValues title, type, target points and selected chapters for the new exam
     * @return generated exam containing the selected subtasks and one random variant per subtask
     * @throws ExamGenerationException if the input is invalid or no valid exam can be composed
     */
    public GeneratedExam generateExam(GenerateExamValues generateExamValues) {
        examGenerationValidator.validateGenerateExamValues(generateExamValues);

        List<CandidateTask> selectedTasks = selectTasksForRequestedPoints(generateExamValues);
        List<GeneratedChapter> generatedChapters = createGeneratedChapters(selectedTasks, generateExamValues.selectedChapters());
        if (generatedChapters.isEmpty()) {
            throw new ExamGenerationException(ExamGenerationException.Reason.NO_GENERATABLE_SUBTASKS);
        }

        return new GeneratedExam(
                generateExamValues.examTitle().trim(),
                generateExamValues.targetPoints(),
                generatedChapters
        );
    }

    /**
     * Calculates the total point values that can be generated with an exact third
     * of the points from each difficulty.
     *
     * @param selectedChapters chapters selected by the user
     * @return sorted reachable total point values
     */
    public List<Double> calculateAvailableTotalPoints(List<Chapter> selectedChapters) {
        return calculateAvailableTotalPoints(selectedChapters, ExamType.defaultType());
    }

    /**
     * Calculates the total point values that can be generated for the requested exam type.
     *
     * @param selectedChapters chapters selected by the user
     * @param examType requested exam type
     * @return sorted reachable total point values
     */
    public List<Double> calculateAvailableTotalPoints(List<Chapter> selectedChapters, ExamType examType) {
        if (selectedChapters == null || selectedChapters.isEmpty()) {
            return List.of();
        }

        List<CandidateTask> candidateTasks = collectCandidateTasksWithoutValidation(selectedChapters, examType);
        Map<SubtaskDifficulty, List<CandidateTask>> tasksByDifficulty = groupTasksByDifficulty(candidateTasks);
        Set<Integer> commonThirdValues = null;

        for (SubtaskDifficulty difficulty : SubtaskDifficulty.values()) {
            Set<Integer> reachablePoints = calculateReachablePointValues(tasksByDifficulty.getOrDefault(difficulty, List.of()));
            reachablePoints.remove(0);
            if (commonThirdValues == null) {
                commonThirdValues = new HashSet<>(reachablePoints);
            } else {
                commonThirdValues.retainAll(reachablePoints);
            }
        }

        if (commonThirdValues == null || commonThirdValues.isEmpty()) {
            return List.of();
        }

        return commonThirdValues.stream()
                .sorted()
                .map(thirdHalfPoints -> Points.fromHalfPoints(thirdHalfPoints * SubtaskDifficulty.values().length))
                .toList();
    }

    /**
     * Selects a valid set of subtasks whose total points match the requested target.
     *
     * @param generateExamValues validated generation input
     * @return ordered list of selected candidate tasks
     */
    private List<CandidateTask> selectTasksForRequestedPoints(GenerateExamValues generateExamValues) {
        List<CandidateTask> candidateTasks = collectCandidateTasks(
                generateExamValues.selectedChapters(),
                generateExamValues.examType()
        );
        List<CandidateTask> selectedTasks = selectBalancedTasks(candidateTasks, generateExamValues.targetPoints());

        sortTasksByChapterAndSubtaskOrder(selectedTasks, generateExamValues.selectedChapters());
        return selectedTasks;
    }

    /**
     * Selects subtasks so each difficulty contributes exactly one third of the target points.
     *
     * @param candidateTasks available tasks
     * @param targetPoints requested total points
     * @return combined selected tasks for all difficulties
     */
    private List<CandidateTask> selectBalancedTasks(List<CandidateTask> candidateTasks, double targetPoints) {
        int targetHalfPoints = Points.toHalfPoints(targetPoints);
        if (targetHalfPoints % SubtaskDifficulty.values().length != 0) {
            throw new ExamGenerationException(
                    ExamGenerationException.Reason.POINTS_NOT_REACHABLE,
                    targetPoints,
                    0,
                    calculateMaxBalancedTotalPoints(candidateTasks)
            );
        }

        int halfPointsPerDifficulty = targetHalfPoints / SubtaskDifficulty.values().length;
        Map<SubtaskDifficulty, List<CandidateTask>> tasksByDifficulty = groupTasksByDifficulty(candidateTasks);
        List<CandidateTask> selectedTasks = new ArrayList<>();

        for (SubtaskDifficulty difficulty : SubtaskDifficulty.values()) {
            Map<Integer, SelectionState> reachableSelections = calculateReachableSelections(
                    tasksByDifficulty.getOrDefault(difficulty, List.of()),
                    halfPointsPerDifficulty
            );
            selectedTasks.addAll(reconstructSelectedTasks(reachableSelections, halfPointsPerDifficulty));
        }

        return selectedTasks;
    }

    /**
     * Creates a lookup map for the order of chapters selected by the user.
     *
     * @param chapters selected chapters in UI order
     * @return map of chapter id to chapter position
     */
    private Map<Integer, Integer> buildChapterOrder(List<Chapter> chapters) {
        Map<Integer, Integer> chapterOrder = new HashMap<>();
        for (int chapterIndex = 0; chapterIndex < chapters.size(); chapterIndex++) {
            chapterOrder.put(chapters.get(chapterIndex).getId(), chapterIndex);
        }
        return chapterOrder;
    }

    /**
     * Creates a lookup map for the order of subtasks inside the selected chapters.
     *
     * @param chapters selected chapters in UI order
     * @return map of subtask id to subtask position within its chapter
     */
    private Map<Integer, Integer> buildSubtaskOrder(List<Chapter> chapters) {
        Map<Integer, Integer> subtaskOrder = new HashMap<>();
        for (Chapter chapter : chapters) {
            List<Subtask> subtasks = chapter.getChildElements() == null ? List.of() : chapter.getChildElements();
            for (int subtaskIndex = 0; subtaskIndex < subtasks.size(); subtaskIndex++) {
                subtaskOrder.put(subtasks.get(subtaskIndex).getId(), subtaskIndex);
            }
        }
        return subtaskOrder;
    }

    /**
     * Collects all generatable subtasks from the selected chapters.
     *
     * @param chapters chapters selected by the user
     * @return all candidate tasks that have points and at least one variant
     * @throws ExamGenerationException if no generatable subtasks are available
     */
    private List<CandidateTask> collectCandidateTasks(List<Chapter> chapters, ExamType examType) {
        List<CandidateTask> candidateTasks = collectCandidateTasksWithoutValidation(chapters, examType);
        examGenerationValidator.validateCandidates(candidateTasks);
        return candidateTasks;
    }

    private List<CandidateTask> collectCandidateTasksWithoutValidation(List<Chapter> chapters, ExamType examType) {
        List<CandidateTask> candidateTasks = new ArrayList<>();
        for (Chapter chapter : chapters) {
            candidateTasks.addAll(collectCandidateTasksFromChapter(chapter, examType));
        }
        return candidateTasks;
    }

    private Map<SubtaskDifficulty, List<CandidateTask>> groupTasksByDifficulty(List<CandidateTask> candidateTasks) {
        Map<SubtaskDifficulty, List<CandidateTask>> tasksByDifficulty = new EnumMap<>(SubtaskDifficulty.class);
        for (CandidateTask candidateTask : candidateTasks) {
            SubtaskDifficulty difficulty = candidateTask.subtask().getDifficulty() == null
                    ? SubtaskDifficulty.MEDIUM
                    : candidateTask.subtask().getDifficulty();
            tasksByDifficulty
                    .computeIfAbsent(difficulty, ignored -> new ArrayList<>())
                    .add(candidateTask);
        }
        return tasksByDifficulty;
    }

    private Set<Integer> calculateReachablePointValues(List<CandidateTask> candidateTasks) {
        Set<Integer> reachablePoints = new TreeSet<>();
        reachablePoints.add(0);

        for (CandidateTask candidateTask : candidateTasks) {
            Set<Integer> extendedPoints = new TreeSet<>(reachablePoints);
            for (Integer currentPoints : reachablePoints) {
                extendedPoints.add(currentPoints + Points.toHalfPoints(candidateTask.subtask().getPoints()));
            }
            reachablePoints = extendedPoints;
        }

        return reachablePoints;
    }

    private double calculateMaxBalancedTotalPoints(List<CandidateTask> candidateTasks) {
        return calculateAvailableTotalPointsFromCandidates(candidateTasks).stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0);
    }

    private List<Double> calculateAvailableTotalPointsFromCandidates(List<CandidateTask> candidateTasks) {
        Map<SubtaskDifficulty, List<CandidateTask>> tasksByDifficulty = groupTasksByDifficulty(candidateTasks);
        Set<Integer> commonThirdValues = null;

        for (SubtaskDifficulty difficulty : SubtaskDifficulty.values()) {
            Set<Integer> reachablePoints = calculateReachablePointValues(tasksByDifficulty.getOrDefault(difficulty, List.of()));
            reachablePoints.remove(0);
            if (commonThirdValues == null) {
                commonThirdValues = new HashSet<>(reachablePoints);
            } else {
                commonThirdValues.retainAll(reachablePoints);
            }
        }

        if (commonThirdValues == null || commonThirdValues.isEmpty()) {
            return List.of();
        }

        return commonThirdValues.stream()
                .sorted()
                .map(thirdHalfPoints -> Points.fromHalfPoints(thirdHalfPoints * SubtaskDifficulty.values().length))
                .toList();
    }

    /**
     * Extracts all generatable subtasks of a single chapter.
     *
     * @param chapter source chapter
     * @return candidate tasks for the given chapter
     */
    private List<CandidateTask> collectCandidateTasksFromChapter(Chapter chapter, ExamType examType) {
        List<CandidateTask> candidateTasks = new ArrayList<>();
        List<Subtask> subtasks = chapter.getChildElements() == null ? List.of() : chapter.getChildElements();

        for (Subtask subtask : subtasks) {
            List<Variant> variants = getValidVariants(subtask);
            if (isGeneratableSubtask(subtask, variants, examType)) {
                candidateTasks.add(new CandidateTask(chapter, subtask, variants));
            }
        }
        return candidateTasks;
    }

    /**
     * Returns the non-null variants of the provided subtask.
     *
     * @param subtask subtask to inspect
     * @return list of valid variants
     */
    private List<Variant> getValidVariants(Subtask subtask) {
        if (subtask.getChildElements() == null) {
            return List.of();
        }

        return subtask.getChildElements().stream()
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Checks whether a subtask can be part of a generated exam.
     *
     * @param subtask subtask to validate
     * @param variants variants available for the subtask
     * @return {@code true} if the subtask has positive points, at least one variant and matches the exam type
     */
    private boolean isGeneratableSubtask(Subtask subtask, List<Variant> variants, ExamType examType) {
        return subtask.getPoints() > 0
                && Points.isHalfStep(subtask.getPoints())
                && !variants.isEmpty()
                && subtask.isEligibleForExamType(examType);
    }

    /**
     * Calculates all reachable point totals up to the requested target.
     *
     * @param candidateTasks available tasks to choose from
     * @param targetHalfPoints requested total number of points in half-point units
     * @return reachable point totals with the information required to rebuild the selection
     * @throws ExamGenerationException if the requested number of points cannot be reached
     */
    private Map<Integer, SelectionState> calculateReachableSelections(List<CandidateTask> candidateTasks, int targetHalfPoints) {
        List<CandidateTask> shuffledCandidates = new ArrayList<>(candidateTasks);
        Collections.shuffle(shuffledCandidates, random);

        Map<Integer, SelectionState> reachableSelections = new LinkedHashMap<>();
        reachableSelections.put(0, new SelectionState(null, null));

        for (CandidateTask candidate : shuffledCandidates) {
            reachableSelections = extendReachableSelections(reachableSelections, candidate, targetHalfPoints);
            if (reachableSelections.containsKey(targetHalfPoints)) {
                break;
            }
        }

        examGenerationValidator.validateReachableStates(reachableSelections, targetHalfPoints);
        return reachableSelections;
    }

    /**
     * Extends the currently reachable point totals with one additional candidate task.
     *
     * @param currentSelections currently reachable selections
     * @param candidate candidate task that may be added
     * @param targetHalfPoints requested total number of points in half-point units
     * @return updated selection map
     */
    private Map<Integer, SelectionState> extendReachableSelections(
            Map<Integer, SelectionState> currentSelections,
            CandidateTask candidate,
            int targetHalfPoints
    ) {
        Map<Integer, SelectionState> extendedSelections = new LinkedHashMap<>(currentSelections);

        for (Map.Entry<Integer, SelectionState> entry : currentSelections.entrySet()) {
            int nextHalfPoints = entry.getKey() + Points.toHalfPoints(candidate.subtask().getPoints());
            if (nextHalfPoints > targetHalfPoints || extendedSelections.containsKey(nextHalfPoints)) {
                continue;
            }
            extendedSelections.put(nextHalfPoints, new SelectionState(entry.getKey(), candidate));
        }

        return extendedSelections;
    }

    /**
     * Rebuilds the selected tasks from the final reachable-selection state.
     *
     * @param reachableSelections reachable point totals and their predecessor information
     * @param targetHalfPoints requested total number of points in half-point units
     * @return selected tasks in reconstruction order
     */
    private List<CandidateTask> reconstructSelectedTasks(
            Map<Integer, SelectionState> reachableSelections,
            int targetHalfPoints
    ) {
        List<CandidateTask> selectedTasks = new ArrayList<>();
        Integer currentHalfPoints = targetHalfPoints;

        while (currentHalfPoints != null && currentHalfPoints > 0) {
            SelectionState state = reachableSelections.get(currentHalfPoints);
            if (state == null || state.candidateTask() == null) {
                break;
            }
            selectedTasks.add(state.candidateTask());
            currentHalfPoints = state.previousHalfPoints();
        }

        Collections.reverse(selectedTasks);
        return selectedTasks;
    }

    /**
     * Sorts selected tasks according to the user's chapter and subtask order.
     *
     * @param selectedTasks tasks chosen for the generated exam
     * @param chapters chapters selected by the user
     */
    private void sortTasksByChapterAndSubtaskOrder(List<CandidateTask> selectedTasks, List<Chapter> chapters) {
        Map<Integer, Integer> chapterOrder = buildChapterOrder(chapters);
        Map<Integer, Integer> subtaskOrder = buildSubtaskOrder(chapters);

        selectedTasks.sort(Comparator
                .comparingInt((CandidateTask candidate) -> chapterOrder.getOrDefault(candidate.chapter().getId(), Integer.MAX_VALUE))
                .thenComparingInt(candidate -> subtaskOrder.getOrDefault(candidate.subtask().getId(), Integer.MAX_VALUE)));
    }

    /**
     * Creates the generated exam chapters in the order of the selected chapters.
     *
     * @param selectedTasks tasks chosen for the exam
     * @param selectedChapters chapters selected by the user
     * @return generated chapters containing the chosen subtasks
     */
    private List<GeneratedChapter> createGeneratedChapters(
            List<CandidateTask> selectedTasks,
            List<Chapter> selectedChapters
    ) {
        Map<Integer, List<GeneratedSubtask>> generatedSubtasksByChapter = groupGeneratedSubtasksByChapter(selectedTasks);
        List<GeneratedChapter> generatedChapters = new ArrayList<>();

        for (Chapter selectedChapter : selectedChapters) {
            List<GeneratedSubtask> chapterSubtasks = generatedSubtasksByChapter.get(selectedChapter.getId());
            if (chapterSubtasks != null && !chapterSubtasks.isEmpty()) {
                generatedChapters.add(new GeneratedChapter(selectedChapter, chapterSubtasks));
            }
        }

        return generatedChapters;
    }

    /**
     * Groups selected tasks by chapter and assigns one random variant to each subtask.
     *
     * @param selectedTasks tasks chosen for the exam
     * @return generated subtasks grouped by chapter id
     */
    private Map<Integer, List<GeneratedSubtask>> groupGeneratedSubtasksByChapter(List<CandidateTask> selectedTasks) {
        Map<Integer, List<GeneratedSubtask>> subtasksByChapter = new LinkedHashMap<>();
        for (CandidateTask selectedTask : selectedTasks) {
            Variant chosenVariant = pickRandomVariant(selectedTask.variants());
            subtasksByChapter
                    .computeIfAbsent(selectedTask.chapter().getId(), ignored -> new ArrayList<>())
                    .add(new GeneratedSubtask(selectedTask.subtask(), chosenVariant));
        }

        return subtasksByChapter;
    }

    /**
     * Selects one random variant from the available variants of a subtask.
     *
     * @param variants available variants for a subtask
     * @return randomly selected variant
     */
    private Variant pickRandomVariant(List<Variant> variants) {
        return variants.get(random.nextInt(variants.size()));
    }
}
