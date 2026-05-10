package service.exam.dto;

import lombok.Builder;
import models.Chapter;

import java.util.List;

/**
 * Generated chapter section containing only the subtasks selected for an exam.
 *
 * @param chapter source chapter from the repository
 * @param subtasks selected generated subtasks belonging to the chapter
 */
@Builder
public record GeneratedChapter(
        Chapter chapter,
        List<GeneratedSubtask> subtasks
) {
}
