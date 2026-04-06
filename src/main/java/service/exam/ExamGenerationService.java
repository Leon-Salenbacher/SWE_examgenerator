package service.exam;

import exceptions.ExamGenerationException;
import models.Chapter;
import models.Subtask;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class ExamGenerationService {

    private final Random random = new Random();
    private final ExamGenerationValidator examGenerationValidator = new ExamGenerationValidator();

    public GeneratedExam generateExam(GenerateExamValues generateExamValues) {
        examGenerationValidator.validateGenerateExamValues(generateExamValues);

        List<CandidateTask> selectedTasks = this.selectTasks(generateExamValues);

        List<GeneratedChapter> generatedChapters = buildGeneratedChapters(selectedTasks, generateExamValues.selectedChapters());
        if (generatedChapters.isEmpty()) {
            throw new ExamGenerationException(ExamGenerationException.Reason.NO_GENERATABLE_SUBTASKS);
        }

        return new GeneratedExam(
                generateExamValues.examTitle().trim(),
                generateExamValues.targetPoints(),
                generatedChapters
        );
    }

    private List<CandidateTask> selectTasks(GenerateExamValues generateExamValues) {

        List<CandidateTask> candidates = getCandidates(generateExamValues.selectedChapters());

        Map<Integer, SelectionState> reachableStates = buildReachableStates(candidates, generateExamValues.targetPoints());
        List<CandidateTask> selectedTasks = reconstructSelection(reachableStates, generateExamValues.targetPoints());
        sortSelectedTasks(selectedTasks, generateExamValues.selectedChapters());

        return selectedTasks;
    }

    private Map<Integer, Integer> buildChapterOrder(List<Chapter> chapters) {
        Map<Integer, Integer> chapterOrder = new HashMap<>();
        for (int chapterIndex = 0; chapterIndex < chapters.size(); chapterIndex++) {
            chapterOrder.put(chapters.get(chapterIndex).getId(), chapterIndex);
        }
        return chapterOrder;
    }

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

    private List<CandidateTask> getCandidates(List<Chapter> chapters) {
        List<CandidateTask> candidates = new ArrayList<>();
        for (Chapter chapter : chapters) {
            candidates.addAll(getCandidateTasksFromChapter(chapter));
        }

        examGenerationValidator.validateCandidates(candidates);
        return candidates;
    }

    private List<CandidateTask> getCandidateTasksFromChapter(Chapter chapter) {
        List<CandidateTask> candidateTasks = new ArrayList<>();
        List<Subtask> subtasks = chapter.getChildElements() == null ? List.of() : chapter.getChildElements();

        for (Subtask subtask : subtasks) {
            List<Variant> variants = subtask.getChildElements() == null
                    ? List.of()
                    : subtask.getChildElements().stream().filter(Objects::nonNull).toList();
            if (subtask.getPoints() > 0 && !variants.isEmpty()) {
                candidateTasks.add(new CandidateTask(chapter, subtask, variants));
            }
        }
        return candidateTasks;
    }

    private Map<Integer, SelectionState> buildReachableStates(List<CandidateTask> candidates, int targetPoints) {
        List<CandidateTask> shuffledCandidates = new ArrayList<>(candidates);
        Collections.shuffle(shuffledCandidates, random);

        Map<Integer, SelectionState> reachableStates = new LinkedHashMap<>();
        reachableStates.put(0, new SelectionState(null, null));

        for (CandidateTask candidate : shuffledCandidates) {
            Map<Integer, SelectionState> nextStates = new LinkedHashMap<>(reachableStates);
            for (Map.Entry<Integer, SelectionState> entry : reachableStates.entrySet()) {
                int nextPoints = entry.getKey() + candidate.subtask().getPoints();
                if (nextPoints > targetPoints || nextStates.containsKey(nextPoints)) {
                    continue;
                }
                nextStates.put(nextPoints, new SelectionState(entry.getKey(), candidate));
            }
            reachableStates = nextStates;
            if (reachableStates.containsKey(targetPoints)) {
                break;
            }
        }

        this.examGenerationValidator.validateReachableStates(reachableStates, targetPoints);
        return reachableStates;
    }


    private List<CandidateTask> reconstructSelection(Map<Integer, SelectionState> reachableStates, int targetPoints) {
        List<CandidateTask> selectedTasks = new ArrayList<>();
        Integer currentPoints = targetPoints;

        while (currentPoints != null && currentPoints > 0) {
            SelectionState state = reachableStates.get(currentPoints);
            if (state == null || state.candidateTask() == null) {
                break;
            }
            selectedTasks.add(state.candidateTask());
            currentPoints = state.previousPoints();
        }

        Collections.reverse(selectedTasks);
        return selectedTasks;
    }

    private void sortSelectedTasks(List<CandidateTask> selectedTasks, List<Chapter> chapters) {
        Map<Integer, Integer> chapterOrder = buildChapterOrder(chapters);
        Map<Integer, Integer> subtaskOrder = buildSubtaskOrder(chapters);

        selectedTasks.sort(Comparator
                .comparingInt((CandidateTask candidate) -> chapterOrder.getOrDefault(candidate.chapter().getId(), Integer.MAX_VALUE))
                .thenComparingInt(candidate -> subtaskOrder.getOrDefault(candidate.subtask().getId(), Integer.MAX_VALUE)));
    }

    private List<GeneratedChapter> buildGeneratedChapters(List<CandidateTask> selectedTasks, List<Chapter> selectedChapters) {
        Map<Integer, List<GeneratedSubtask>> subtasksByChapter = new LinkedHashMap<>();
        for (CandidateTask selectedTask : selectedTasks) {
            Variant chosenVariant = pickRandomVariant(selectedTask.variants());
            subtasksByChapter
                    .computeIfAbsent(selectedTask.chapter().getId(), ignored -> new ArrayList<>())
                    .add(new GeneratedSubtask(selectedTask.subtask(), chosenVariant));
        }

        List<GeneratedChapter> generatedChapters = new ArrayList<>();
        for (Chapter selectedChapter : selectedChapters) {
            List<GeneratedSubtask> chapterSubtasks = subtasksByChapter.get(selectedChapter.getId());
            if (chapterSubtasks != null && !chapterSubtasks.isEmpty()) {
                generatedChapters.add(new GeneratedChapter(selectedChapter, chapterSubtasks));
            }
        }
        return generatedChapters;
    }

    private Variant pickRandomVariant(List<Variant> variants) {
        return variants.get(random.nextInt(variants.size()));
    }
}

