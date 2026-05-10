package exceptions;

/**
 * Domain exception used when an exam cannot be generated from the selected input.
 *
 * <p>The reason enum gives the UI a stable value that can be translated into a
 * user-facing validation or generation error message.</p>
 */
public class ExamGenerationException extends RuntimeException {
    private final Reason reason;
    private final double requestedPoints;
    private final double closestReachablePoints;
    private final double maxReachablePoints;

    /**
     * Creates an exception for errors that do not need point diagnostics.
     *
     * @param reason machine-readable generation failure reason
     */
    public ExamGenerationException(Reason reason) {
        this(reason, 0, 0, 0);
    }

    /**
     * Creates an exception for errors that should include reachable point details.
     *
     * @param reason machine-readable generation failure reason
     * @param requestedPoints point value requested by the user
     * @param closestReachablePoints closest reachable point value
     * @param maxReachablePoints maximum reachable point value
     */
    public ExamGenerationException(Reason reason, double requestedPoints, double closestReachablePoints, double maxReachablePoints) {
        super(reason.name());
        this.reason = reason;
        this.requestedPoints = requestedPoints;
        this.closestReachablePoints = closestReachablePoints;
        this.maxReachablePoints = maxReachablePoints;
    }

    /**
     * @return machine-readable reason for the generation failure
     */
    public Reason getReason() {
        return reason;
    }

    /**
     * @return point value requested by the user, or {@code 0} when not applicable
     */
    public double getRequestedPoints() {
        return requestedPoints;
    }

    /**
     * @return closest reachable point value, or {@code 0} when not applicable
     */
    public double getClosestReachablePoints() {
        return closestReachablePoints;
    }

    /**
     * @return maximum reachable point value, or {@code 0} when not applicable
     */
    public double getMaxReachablePoints() {
        return maxReachablePoints;
    }

    /**
     * Stable failure categories used by services and controllers.
     */
    public enum Reason {
        INVALID_TITLE,
        INVALID_POINTS,
        EMPTY_SELECTION,
        NO_GENERATABLE_SUBTASKS,
        POINTS_NOT_REACHABLE
    }
}
