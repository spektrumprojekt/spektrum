package de.spektrumprojekt.aggregator.chain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.chain.CommandChain;

public class AggregatorChain extends CommandChain<AggregatorMessageContext> {

    /** The logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AggregatorChain.class);

    @Override
    public void process(AggregatorMessageContext context) {
        // TODO Auto-generated method stub
        super.process(context);
    }

}
