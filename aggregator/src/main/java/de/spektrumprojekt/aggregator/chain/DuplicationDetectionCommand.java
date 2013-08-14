package de.spektrumprojekt.aggregator.chain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.aggregator.duplicate.DuplicateDetection;
import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.commons.chain.CommandException;
import de.spektrumprojekt.datamodel.message.Message;

public class DuplicationDetectionCommand implements Command<AggregatorMessageContext> {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DuplicationDetectionCommand.class);

    private final DuplicateDetection duplicateDetection;

    public DuplicationDetectionCommand(DuplicateDetection duplicateDetection) {
        if (duplicateDetection == null) {
            throw new IllegalArgumentException("duplicateDetection cannot be null!");
        }
        this.duplicateDetection = duplicateDetection;

    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " duplicationDetection=" + duplicateDetection;
    }

    public DuplicateDetection getDuplicateDetection() {
        return duplicateDetection;
    }

    @Override
    public void process(AggregatorMessageContext context) throws CommandException {
        Message message = context.getMessage();

        if (duplicateDetection.isDuplicate(message)) {

            context.setDuplicate(true);

            LOGGER.trace("Message {} was a duplicate", message);
            throw new CommandException(false, "Message " + message.getGlobalId()
                    + " was a duplicate.");
        }
    }

}