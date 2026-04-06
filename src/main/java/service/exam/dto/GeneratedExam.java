package service.exam.dto;

import java.util.List;

public record GeneratedExam(
        String title,
        int totalPoints,
        List<GeneratedChapter> chapters
) {
}
