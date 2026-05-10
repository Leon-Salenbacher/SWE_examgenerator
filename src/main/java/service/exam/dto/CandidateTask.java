package service.exam.dto;

import lombok.Builder;
import models.Chapter;
import models.Subtask;
import models.Variant;

import java.util.List;

/**
 * Internal generation candidate consisting of one subtask and its usable variants.
 *
 * @param chapter chapter that owns the candidate subtask
 * @param subtask subtask that can be selected for the generated exam
 * @param variants variants that are valid choices for the selected subtask
 */
@Builder
public record CandidateTask(
        Chapter chapter,
        Subtask subtask,
        List<Variant> variants
) {
}
