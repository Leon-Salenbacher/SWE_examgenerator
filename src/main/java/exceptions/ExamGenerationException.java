package exceptions;

public class ExamGenerationException extends RuntimeException {
    private final Reason reason;
    private final double requestedPoints;
    private final double closestReachablePoints;
    private final double maxReachablePoints;

    public ExamGenerationException(Reason reason) {
        this(reason, 0, 0, 0);
    }

    public ExamGenerationException(Reason reason, double requestedPoints, double closestReachablePoints, double maxReachablePoints) {
        super(reason.name());
        this.reason = reason;
        this.requestedPoints = requestedPoints;
        this.closestReachablePoints = closestReachablePoints;
        this.maxReachablePoints = maxReachablePoints;
    }

    public Reason getReason() {
        return reason;
    }

    public double getRequestedPoints() {
        return requestedPoints;
    }

    public double getClosestReachablePoints() {
        return closestReachablePoints;
    }

    public double getMaxReachablePoints() {
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
