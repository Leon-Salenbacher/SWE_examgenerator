package service.exam.dto;

import lombok.Builder;
import models.Subtask;
import models.Variant;

@Builder
public record GeneratedSubtask(
        Subtask subtask,
        Variant variant
) {
}
