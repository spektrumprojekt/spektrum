package de.spektrumprojekt.aggregator.chain;

import de.spektrumprojekt.commons.chain.CommandChain;
import de.spektrumprojekt.persistence.Persistence;

public class AggregatorChain {

    private final Persistence persistence;

    private final CommandChain<AggregatorMessageContext> newMessageChain = new CommandChain<AggregatorMessageContext>();

    private final CommandChain<AggregatorMessageContext> AddMessageToSubscriptionChain = new CommandChain<AggregatorMessageContext>();

    public AggregatorChain(Persistence persistence) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        this.persistence = persistence;
    }

    /**
     * The command chain to be used to (re)send a message already in the system to a (new)
     * subscription
     * 
     * @return
     */

    public CommandChain<AggregatorMessageContext> getAddMessageToSubscriptionChain() {
        return AddMessageToSubscriptionChain;
    }

    /**
     * The command chain to be used for a completely new message
     * 
     * @return
     */
    public CommandChain<AggregatorMessageContext> getNewMessageChain() {
        return newMessageChain;
    }

    public Persistence getPersistence() {
        return persistence;
    }

}
