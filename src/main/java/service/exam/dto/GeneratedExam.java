package service.exam.dto;

import java.util.List;

/**
 * Complete generated exam data passed from the generation service to PDF export.
 *
 * @param title display title of the generated exam
 * @param totalPoints requested total point value
 * @param chapters generated chapter sections in output order
 */
public record GeneratedExam(
        String title,
        double totalPoints,
        List<GeneratedChapter> chapters
) {
}
