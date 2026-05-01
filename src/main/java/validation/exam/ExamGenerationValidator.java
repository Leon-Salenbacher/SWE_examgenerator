package validation.exam;

import exceptions.ExamGenerationException;
import models.Chapter;
import models.Points;
import service.exam.dto.CandidateTask;
import service.exam.dto.GenerateExamValues;

import java.util.List;

public class ExamGenerationValidator {

    /**
     * Validates the complete user input for exam generation.
     *
     * @param generateExamValues title, target points and selected chapters
     * @throws ExamGenerationException if one of the input values is invalid
     */
    public void validateGenerateExamValues(GenerateExamValues generateExamValues) throws ExamGenerationException {
        validateExamTitle(generateExamValues.examTitle());
        validateTargetPoints(generateExamValues.targetPoints());
        validateSelectedChapters(generateExamValues.selectedChapters());
    }

    /**
     * Validates that the exam title contains visible characters.
     *
     * @param examTitle title to validate
     * @throws ExamGenerationException if the title is missing or blank
     */
    private void validateExamTitle(String examTitle) throws ExamGenerationException {
        if (examTitle == null || examTitle.isBlank()) {
            throw new ExamGenerationException(ExamGenerationException.Reason.INVALID_TITLE);
        }
    }

    /**
     * Validates that the requested point total is positive.
     *
     * @param targetPoints requested point total
     * @throws ExamGenerationException if the value is not positive
     */
    private void validateTargetPoints(double targetPoints) throws ExamGenerationException {
        if (targetPoints <= 0) {
            throw new ExamGenerationException(ExamGenerationException.Reason.INVALID_POINTS);
        }
        if (!Points.isHalfStep(targetPoints)) {
            throw new ExamGenerationException(ExamGenerationException.Reason.INVALID_POINTS);
        }
    }

    /**
     * Validates that at least one chapter has been selected.
     *
     * @param selectedChapters selected chapters
     * @throws ExamGenerationException if no chapters were selected
     */
    private void validateSelectedChapters(List<Chapter> selectedChapters) throws ExamGenerationException {
        if (selectedChapters == null || selectedChapters.isEmpty()) {
            throw new ExamGenerationException(ExamGenerationException.Reason.EMPTY_SELECTION);
        }
    }

    /**
     * Validates that the selected chapters contain at least one generatable subtask.
     *
     * @param candidateTasks generatable subtasks collected from the selected chapters
     * @throws ExamGenerationException if no candidate tasks are available
     */
    public void validateCandidates(List<CandidateTask> candidateTasks) throws ExamGenerationException {
        if (candidateTasks.isEmpty()) {
            throw new ExamGenerationException(ExamGenerationException.Reason.NO_GENERATABLE_SUBTASKS);
        }
    }
}
