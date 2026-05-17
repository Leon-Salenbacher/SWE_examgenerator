package exceptions;

/**
 * Runtime exception for XML persistence, import and export failures.
 */
public class XmlStorageException extends RuntimeException {
    /**
     * Creates a storage exception with a user-readable message.
     *
     * @param message description of the failed storage operation
     */
    public XmlStorageException(String message) {
        super(message);
    }

    /**
     * Creates a storage exception with the original low-level cause.
     *
     * @param message description of the failed storage operation
     * @param cause original exception raised by the XML or file API
     */
    public XmlStorageException(String message, Throwable cause){
        super(message,cause);
    }
}
