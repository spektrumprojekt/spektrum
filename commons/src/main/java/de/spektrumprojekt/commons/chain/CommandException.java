package de.spektrumprojekt.commons.chain;

public class CommandException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final boolean continueChain;

    public CommandException(boolean continueChain, String message) {
        super(message);
        this.continueChain = continueChain;
    }

    public CommandException(boolean continueChain, String message, Throwable th) {
        super(message, th);
        this.continueChain = continueChain;
    }

    public boolean isContinueChain() {
        return continueChain;
    }
}