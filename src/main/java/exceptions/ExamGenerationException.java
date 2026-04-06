package exceptions;

public class ExamGenerationException extends RuntimeException {
    private final Reason reason;
    private final int requestedPoints;
    private final int closestReachablePoints;
    private final int maxReachablePoints;

    public ExamGenerationException(Reason reason) {
        this(reason, 0, 0, 0);
    }

    public ExamGenerationException(Reason reason, int requestedPoints, int closestReachablePoints, int maxReachablePoints) {
        super(reason.name());
        this.reason = reason;
        this.requestedPoints = requestedPoints;
        this.closestReachablePoints = closestReachablePoints;
        this.maxReachablePoints = maxReachablePoints;
    }

    public Reason getReason() {
        return reason;
    }

    public int getRequestedPoints() {
        return requestedPoints;
    }

    public int getClosestReachablePoints() {
        return closestReachablePoints;
    }

    public int getMaxReachablePoints() {
        return maxReachablePoints;
    }

    public enum Reason {
        INVALID_TITLE,
        INVALID_POINTS,
        EMPTY_SELECTION,
        NO_GENERATABLE_SUBTASKS,
        POINTS_NOT_REACHABLE
    }
}
