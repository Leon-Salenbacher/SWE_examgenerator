package service.exam.dto;

public record SelectionState(
        Integer previousHalfPoints,
        CandidateTask candidateTask
) {
}
