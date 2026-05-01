package service.exam.dto;


import lombok.Builder;
import models.Chapter;
import models.ExamType;

import java.util.List;

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

    public GenerateExamValues(String examTitle, double targetPoints, List<Chapter> selectedChapters) {
        this(examTitle, targetPoints, selectedChapters, ExamType.defaultType());
    }
}
