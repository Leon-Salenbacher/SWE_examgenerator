package exceptions;

public class XmlStorageException extends RuntimeException {
    public XmlStorageException(String message) {
        super(message);
    }
    public XmlStorageException(String message, Throwable cause){
        super(message,cause);
    }
}
