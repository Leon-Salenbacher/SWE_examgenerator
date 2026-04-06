package validation.exam;

import exceptions.ExamGenerationException;
import models.Chapter;
import service.exam.dto.CandidateTask;
import service.exam.dto.GenerateExamValues;
import service.exam.dto.SelectionState;

import java.util.List;
import java.util.Map;

public class ExamGenerationValidator {

    public void validateGenerateExamValues(GenerateExamValues generateExamValues) throws ExamGenerationException {
        validateExamTitle(generateExamValues.examTitle());
        validateTargetPoints(generateExamValues.targetPoints());
        validateSelectedChapters(generateExamValues.selectedChapters());
    }

    private void validateExamTitle(String examTitle) throws ExamGenerationException {
        if (examTitle == null || examTitle.isBlank()) {
            throw new ExamGenerationException(ExamGenerationException.Reason.INVALID_TITLE);
        }
    }

    private void validateTargetPoints(int targetPoints) throws ExamGenerationException {
        if (targetPoints <= 0) {
            throw new ExamGenerationException(ExamGenerationException.Reason.INVALID_POINTS);
        }
    }

    private void validateSelectedChapters(List<Chapter> selectedChapters) throws ExamGenerationException {
        if (selectedChapters == null || selectedChapters.isEmpty()) {
            throw new ExamGenerationException(ExamGenerationException.Reason.EMPTY_SELECTION);
        }
    }

    public void validateCandidates(List<CandidateTask> candidateTasks)
        throws ExamGenerationException {
        if(candidateTasks.isEmpty()){
            throw new ExamGenerationException(ExamGenerationException.Reason.NO_GENERATABLE_SUBTASKS);
        }
    }

    public void validateReachableStates(
            Map<Integer, SelectionState> reachableStates,
            int targetPoints
    ) throws ExamGenerationException {
        if(reachableStates.containsKey(targetPoints)){
            return;
        }

        int maxReachable = reachableStates
                .keySet().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
        int closetReachable = reachableStates
                .keySet().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
        //TODO same variables?

        throw new ExamGenerationException(
                ExamGenerationException.Reason.POINTS_NOT_REACHABLE,
                targetPoints,
                closetReachable,
                maxReachable
        );
    }
}
