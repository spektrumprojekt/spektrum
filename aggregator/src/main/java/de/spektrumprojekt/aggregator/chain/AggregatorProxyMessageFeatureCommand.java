package de.spektrumprojekt.aggregator.chain;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.commons.chain.CommandException;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;

public class AggregatorProxyMessageFeatureCommand implements
        Command<AggregatorMessageContext> {

    private final Command<MessageFeatureContext> command;

    public AggregatorProxyMessageFeatureCommand(
            Command<MessageFeatureContext> command) {
        if (command == null) {
            throw new IllegalArgumentException("command cannot be null.");
        }

        this.command = command;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " command: "
                + command.getConfigurationDescription();
    }

    @Override
    public void process(AggregatorMessageContext context) throws CommandException {

        command.process(context.getMessageFeatureContext());

    }
}