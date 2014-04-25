package de.spektrumprojekt.commons.chain;

public class CommandException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final boolean rethrow;
    private final boolean continueChain;

    public CommandException(boolean continueChain, String message) {
        super(message);
        this.continueChain = continueChain;
        this.rethrow = false;
    }

    public CommandException(boolean continueChain, String message, Throwable th) {
        super(message, th);
        this.continueChain = continueChain;
        this.rethrow = false;
    }

    public CommandException(String message) {
        super(message);
        this.continueChain = false;
        this.rethrow = true;
    }

    public CommandException(String message, Throwable th) {
        super(message, th);
        this.continueChain = false;
        this.rethrow = true;
    }

    public boolean isContinueChain() {
        return continueChain;
    }

    public boolean isRethrow() {
        return rethrow;
    }

}