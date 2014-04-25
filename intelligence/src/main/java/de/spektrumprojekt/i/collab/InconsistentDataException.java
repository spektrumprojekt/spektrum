package de.spektrumprojekt.i.collab;

public class InconsistentDataException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public InconsistentDataException() {
        super();
    }

    public InconsistentDataException(String message) {
        super(message);
    }

    public InconsistentDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public InconsistentDataException(Throwable cause) {
        super(cause);
    }

}
