package de.spektrumprojekt.aggregator.chain;

import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.communication.transfer.MessageCommunicationMessage;
import de.spektrumprojekt.datamodel.message.Message;

public class SendAggregatorMessageCommand implements
        de.spektrumprojekt.commons.chain.Command<AggregatorMessageContext> {

    private final Communicator communicator;

    public SendAggregatorMessageCommand(Communicator communicator) {
        if (communicator == null) {
            throw new IllegalArgumentException("communuicator cannot be null!");
        }
        this.communicator = communicator;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void process(AggregatorMessageContext context) {
        Message message = context.getMessage();

        MessageCommunicationMessage mcm = new MessageCommunicationMessage(message);

        this.communicator.sendMessage(mcm);

    }

}