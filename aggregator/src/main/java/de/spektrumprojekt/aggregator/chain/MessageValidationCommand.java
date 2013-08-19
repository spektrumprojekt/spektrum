package de.spektrumprojekt.aggregator.chain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.chain.CommandException;
import de.spektrumprojekt.datamodel.message.Message;

public class MessageValidationCommand implements
        de.spektrumprojekt.commons.chain.Command<AggregatorMessageContext> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(MessageValidationCommand.class);

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void process(AggregatorMessageContext context) throws CommandException {
        Message message = context.getMessage();

        if (message.getSourceGlobalId() == null
                || message.getSourceGlobalId().isEmpty()) {
            LOGGER.warn(
                    "Message {} has no subscription ID specified, this means it cannot be relayed.",
                    message);
            throw new CommandException(false, "Message " + message.getGlobalId()
                    + " has no subscription ID specified, this means it cannot be relayed.");

        }
    }
}