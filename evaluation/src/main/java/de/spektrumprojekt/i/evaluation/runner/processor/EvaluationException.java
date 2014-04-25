package de.spektrumprojekt.i.evaluation.runner.processor;

public class EvaluationException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public EvaluationException() {
        super();
    }

    public EvaluationException(String message) {
        super(message);
    }

    public EvaluationException(String message, Throwable cause) {
        super(message, cause);
    }

    public EvaluationException(Throwable cause) {
        super(cause);
    }

}
