package service.exam.dto;

public record SelectionState(
        Integer previousPoints,
        CandidateTask candidateTask
) {
}
