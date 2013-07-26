package de.spektrumprojekt.aggregator.chain;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.chain.CommandException;
import de.spektrumprojekt.datamodel.message.Message;

public class PublicationDateFilterCommand implements
        de.spektrumprojekt.commons.chain.Command<AggregatorMessageContext> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(PublicationDateFilterCommand.class);

    private final Date minimumPublicationDate;

    public PublicationDateFilterCommand(Date minimumPublicationDate) {

        this.minimumPublicationDate = minimumPublicationDate;
    }

    /**
     * 
     * @param message
     * @return if the date of the message is fine and should be processed further
     */
    protected boolean checkMinimumPublicationDate(Message message) {
        if (minimumPublicationDate != null
                && minimumPublicationDate.after(message.getPublicationDate())) {
            return false;
        }
        return true;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void process(AggregatorMessageContext context) throws CommandException {

        if (!checkMinimumPublicationDate(context.getMessage())) {
            LOGGER.debug(
                    "Message {} was before minimum publication date {} and was skipped. duplicate",
                    context.getMessage(), minimumPublicationDate);
            throw new CommandException(false, "Message " + context.getMessage().getGlobalId()
                    + " was before minimum publication date " + minimumPublicationDate
                    + " and was skipped. duplicate");

        }
    }
}