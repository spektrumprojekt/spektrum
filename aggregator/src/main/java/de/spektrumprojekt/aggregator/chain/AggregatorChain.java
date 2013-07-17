package de.spektrumprojekt.aggregator.chain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.chain.CommandChain;
import de.spektrumprojekt.persistence.Persistence;

public class AggregatorChain extends CommandChain<AggregatorMessageContext> {

    /** The logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AggregatorChain.class);

    private final Persistence persistence;

    public AggregatorChain(Persistence persistence) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        this.persistence = persistence;
    }

    public Persistence getPersistence() {
        return persistence;
    }

}
