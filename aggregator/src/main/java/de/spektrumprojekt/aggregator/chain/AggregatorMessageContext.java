package de.spektrumprojekt.aggregator.chain;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;
import de.spektrumprojekt.persistence.Persistence;

public class AggregatorMessageContext {

    private final MessageFeatureContext messageFeatureContext;

    private boolean duplicate;

    public AggregatorMessageContext(Persistence persistence, Message message) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }

        // TODO what to with the mr ?
        MessageRelation messageRelation = null;
        this.messageFeatureContext = new MessageFeatureContext(persistence,
                message, messageRelation);
    }

    /**
     * 
     * @return never null
     */
    public Message getMessage() {
        return this.messageFeatureContext.getMessage();
    }

    /**
     * 
     * @return never null
     */
    public MessageFeatureContext getMessageFeatureContext() {
        return messageFeatureContext;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

}