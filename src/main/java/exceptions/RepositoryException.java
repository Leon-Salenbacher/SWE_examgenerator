package exceptions;


/**
 * Errors that get thrown with in the {@link repository.Repository} structure.
 */
public class RepositoryException extends RuntimeException {
    /**
     * Creates a repository exception with a user-readable message.
     *
     * @param message description of the repository failure
     */
    public RepositoryException(String message) {
        super(message);
    }

    /**
     * Creates a repository exception with the original lower-level cause.
     *
     * @param message description of the repository failure
     * @param cause original failure cause
     */
    public RepositoryException(String message, Throwable cause){
        super(message, cause);
    }
}
