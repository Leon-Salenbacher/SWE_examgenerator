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
import validation.exam.ExamGenerationValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
     * Calculates all total point values that can be generated from the selected chapters.
     * Generated exams prefer the closest possible balance across difficulties, but no
     * longer require an exact third from each difficulty.
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
        Set<Integer> reachablePoints = calculateReachablePointValues(candidateTasks);
        reachablePoints.remove(0);

        return reachablePoints.stream()
                .sorted()
                .map(Points::fromHalfPoints)
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
     * Selects subtasks whose total points match the target and whose difficulty
     * distribution is as close as possible to one third each.
     *
     * @param candidateTasks available tasks
     * @param targetPoints requested total points
     * @return combined selected tasks for all difficulties
     */
    private List<CandidateTask> selectBalancedTasks(List<CandidateTask> candidateTasks, double targetPoints) {
        int targetHalfPoints = Points.toHalfPoints(targetPoints);
        List<Map<DifficultyPointTotals, BalancedSelectionState>> statesByTotal =
                calculateBalancedSelections(candidateTasks, targetHalfPoints);
        Map<DifficultyPointTotals, BalancedSelectionState> targetSelections = statesByTotal.get(targetHalfPoints);
        if (targetSelections.isEmpty()) {
            throwPointsNotReachable(candidateTasks, targetHalfPoints);
        }

        DifficultyPointTotals bestTotals = targetSelections.keySet().stream()
                .min(Comparator
                        .comparingLong((DifficultyPointTotals totals) -> totals.squaredDistanceFromEqualShare(targetHalfPoints))
                        .thenComparingInt(DifficultyPointTotals::range))
                .orElseThrow();

        return reconstructSelectedTasks(statesByTotal, targetHalfPoints, bestTotals);
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
     * Calculates all reachable difficulty distributions up to the requested target.
     *
     * @param candidateTasks available tasks to choose from
     * @param targetHalfPoints requested total number of points in half-point units
     * @return selection states grouped by total point value
     */
    private List<Map<DifficultyPointTotals, BalancedSelectionState>> calculateBalancedSelections(
            List<CandidateTask> candidateTasks,
            int targetHalfPoints
    ) {
        List<CandidateTask> shuffledCandidates = new ArrayList<>(candidateTasks);
        Collections.shuffle(shuffledCandidates, random);

        List<Map<DifficultyPointTotals, BalancedSelectionState>> statesByTotal = new ArrayList<>();
        for (int halfPoints = 0; halfPoints <= targetHalfPoints; halfPoints++) {
            statesByTotal.add(new LinkedHashMap<>());
        }
        statesByTotal.get(0).put(DifficultyPointTotals.zero(), new BalancedSelectionState(null, null));

        for (CandidateTask candidate : shuffledCandidates) {
            int candidateHalfPoints = Points.toHalfPoints(candidate.subtask().getPoints());
            SubtaskDifficulty difficulty = difficultyOf(candidate);
            for (int currentHalfPoints = targetHalfPoints - candidateHalfPoints; currentHalfPoints >= 0; currentHalfPoints--) {
                Map<DifficultyPointTotals, BalancedSelectionState> currentStates = statesByTotal.get(currentHalfPoints);
                if (currentStates.isEmpty()) {
                    continue;
                }

                List<DifficultyPointTotals> currentTotals = new ArrayList<>(currentStates.keySet());
                for (DifficultyPointTotals currentTotal : currentTotals) {
                    DifficultyPointTotals nextTotal = currentTotal.add(difficulty, candidateHalfPoints);
                    statesByTotal
                            .get(currentHalfPoints + candidateHalfPoints)
                            .putIfAbsent(nextTotal, new BalancedSelectionState(currentTotal, candidate));
                }
            }
        }

        return statesByTotal;
    }

    private SubtaskDifficulty difficultyOf(CandidateTask candidateTask) {
        return candidateTask.subtask().getDifficulty() == null
                ? SubtaskDifficulty.MEDIUM
                : candidateTask.subtask().getDifficulty();
    }

    private void throwPointsNotReachable(List<CandidateTask> candidateTasks, int targetHalfPoints) {
        Set<Integer> reachablePoints = calculateReachablePointValues(candidateTasks);
        reachablePoints.remove(0);

        int closestReachableHalfPoints = reachablePoints.stream()
                .filter(points -> points <= targetHalfPoints)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
        int maxReachableHalfPoints = reachablePoints.stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);

        throw new ExamGenerationException(
                ExamGenerationException.Reason.POINTS_NOT_REACHABLE,
                Points.fromHalfPoints(targetHalfPoints),
                Points.fromHalfPoints(closestReachableHalfPoints),
                Points.fromHalfPoints(maxReachableHalfPoints)
        );
    }

    /**
     * Rebuilds the best balanced selected tasks from the final reachable-selection state.
     *
     * @param statesByTotal reachable difficulty distributions and predecessor information
     * @param targetHalfPoints requested total number of points in half-point units
     * @param targetTotals chosen difficulty totals for the requested target
     * @return selected tasks in reconstruction order
     */
    private List<CandidateTask> reconstructSelectedTasks(
            List<Map<DifficultyPointTotals, BalancedSelectionState>> statesByTotal,
            int targetHalfPoints,
            DifficultyPointTotals targetTotals
    ) {
        List<CandidateTask> selectedTasks = new ArrayList<>();
        int currentHalfPoints = targetHalfPoints;
        DifficultyPointTotals currentTotals = targetTotals;

        while (currentHalfPoints > 0) {
            BalancedSelectionState state = statesByTotal.get(currentHalfPoints).get(currentTotals);
            if (state == null || state.candidateTask() == null) {
                throw new IllegalStateException("Could not reconstruct generated exam selection.");
            }
            selectedTasks.add(state.candidateTask());
            currentHalfPoints -= Points.toHalfPoints(state.candidateTask().subtask().getPoints());
            currentTotals = state.previousTotals();
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

    private record BalancedSelectionState(
            DifficultyPointTotals previousTotals,
            CandidateTask candidateTask
    ) {
    }

    private record DifficultyPointTotals(
            int easyHalfPoints,
            int mediumHalfPoints,
            int hardHalfPoints
    ) {
        private static DifficultyPointTotals zero() {
            return new DifficultyPointTotals(0, 0, 0);
        }

        private DifficultyPointTotals add(SubtaskDifficulty difficulty, int halfPoints) {
            return switch (difficulty) {
                case EASY -> new DifficultyPointTotals(easyHalfPoints + halfPoints, mediumHalfPoints, hardHalfPoints);
                case MEDIUM -> new DifficultyPointTotals(easyHalfPoints, mediumHalfPoints + halfPoints, hardHalfPoints);
                case HARD -> new DifficultyPointTotals(easyHalfPoints, mediumHalfPoints, hardHalfPoints + halfPoints);
            };
        }

        private long squaredDistanceFromEqualShare(int targetHalfPoints) {
            return squaredDeviation(easyHalfPoints, targetHalfPoints)
                    + squaredDeviation(mediumHalfPoints, targetHalfPoints)
                    + squaredDeviation(hardHalfPoints, targetHalfPoints);
        }

        private long squaredDeviation(int difficultyHalfPoints, int targetHalfPoints) {
            long scaledDeviation = (long) difficultyHalfPoints * SubtaskDifficulty.values().length - targetHalfPoints;
            return scaledDeviation * scaledDeviation;
        }

        private int range() {
            int max = Math.max(easyHalfPoints, Math.max(mediumHalfPoints, hardHalfPoints));
            int min = Math.min(easyHalfPoints, Math.min(mediumHalfPoints, hardHalfPoints));
            return max - min;
        }
    }
}
