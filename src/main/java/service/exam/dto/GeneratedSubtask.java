package service.exam.dto;

import lombok.Builder;
import models.Subtask;
import models.Variant;

/**
 * Generated subtask pairing the selected task with one selected variant.
 *
 * @param subtask selected task metadata and point value
 * @param variant variant that contains the concrete question text
 */
@Builder
public record GeneratedSubtask(
        Subtask subtask,
        Variant variant
) {
}
