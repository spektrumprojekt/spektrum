package de.spektrumprojekt.aggregator.chain;

import de.spektrumprojekt.datamodel.message.Message;

public class AggregatorMessageContext {

    private final Message message;

    private boolean duplicate;

    public AggregatorMessageContext(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("message cannot be null.");
        }
        this.message = message;
    }

    /**
     * 
     * @return never null
     */
    public Message getMessage() {
        return message;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

}