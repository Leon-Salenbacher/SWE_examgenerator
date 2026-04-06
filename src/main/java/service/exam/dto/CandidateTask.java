package service.exam.dto;

import lombok.Builder;
import models.Chapter;
import models.Subtask;
import models.Variant;

import java.util.List;

@Builder
public record CandidateTask(
        Chapter chapter,
        Subtask subtask,
        List<Variant> variants
) {
}
