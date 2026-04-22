package service.exam.dto;

import lombok.Builder;
import models.Chapter;

import java.util.List;

@Builder
public record GeneratedChapter(
        Chapter chapter,
        List<GeneratedSubtask> subtasks
) {
}
