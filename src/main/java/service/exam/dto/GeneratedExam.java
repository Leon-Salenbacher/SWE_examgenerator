package service.exam.dto;

import java.util.List;

public record GeneratedExam(
        String title,
        double totalPoints,
        List<GeneratedChapter> chapters
) {
}
