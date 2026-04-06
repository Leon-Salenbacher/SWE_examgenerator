package service.exam.dto;


import lombok.Builder;
import models.Chapter;

import java.util.List;

@Builder
public record GenerateExamValues(
        String examTitle,
        int targetPoints,
        List<Chapter> selectedChapters
) {
}
