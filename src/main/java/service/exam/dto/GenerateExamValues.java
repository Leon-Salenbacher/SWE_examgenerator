package service.exam.dto;


import lombok.Builder;
import models.Chapter;
import models.ExamType;

import java.util.List;

/**
 * User input required to generate an exam.
 *
 * @param examTitle exam title entered in the generation dialog
 * @param targetPoints target point total chosen from reachable point values
 * @param selectedChapters chapters selected for generation in output order
 * @param examType type of exam to generate
 */
@Builder
public record GenerateExamValues(
        String examTitle,
        double targetPoints,
        List<Chapter> selectedChapters,
        ExamType examType
) {
    public GenerateExamValues {
        if (examType == null) {
            examType = ExamType.defaultType();
        }
    }

    /**
     * Backwards-compatible constructor that generates a regular exam.
     *
     * @param examTitle exam title
     * @param targetPoints requested point total
     * @param selectedChapters selected chapters
     */
    public GenerateExamValues(String examTitle, double targetPoints, List<Chapter> selectedChapters) {
        this(examTitle, targetPoints, selectedChapters, ExamType.defaultType());
    }
}
