package exceptions;


/**
 * Errors that get thrown with in the {@link repository.Repository} structure.
 */
public class RepositoryException extends RuntimeException {
    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable cause){
        super(message, cause);
    }
}
