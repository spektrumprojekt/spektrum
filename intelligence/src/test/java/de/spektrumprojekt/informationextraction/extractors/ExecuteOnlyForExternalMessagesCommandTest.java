package de.spektrumprojekt.informationextraction.extractors;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.commons.chain.CommandException;
import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;
import de.spektrumprojekt.persistence.simple.SimplePersistence;

public class ExecuteOnlyForExternalMessagesCommandTest {

    protected static final Long MESSAGE_ID_BEFORE = 1L;
    protected static final Long MESSAGE_ID_AFTER = 999L;
    private Command<InformationExtractionContext> command;
    private Message message;
    private InformationExtractionContext informationExtractionContext;

    @Before
    public void setup() {
        command = new ExecuteOnlyForExternalMessagesCommand(
                new Command<InformationExtractionContext>() {

                    @Override
                    public String getConfigurationDescription() {
                        return "";
                    }

                    @Override
                    public void process(InformationExtractionContext context) {
                        context.getMessage().setId(MESSAGE_ID_AFTER);
                    }
                });
        message = new Message(MessageType.CONTENT, StatusType.ERROR_AUTHENTICATION, new Date());
        message.setId(MESSAGE_ID_BEFORE);
        informationExtractionContext = new InformationExtractionContext(new SimplePersistence(),
                message, new MessagePart(MimeType.JSON, ""));
    }

    @Test
    public void testExternalMessage() throws CommandException {
        Assert.assertEquals(MESSAGE_ID_BEFORE, message.getId());
        message.addProperty(new Property(Property.PROPERTY_KEY_EXTERNAL,
                Property.PROPERTY_VALUE_EXTERNAL));
        command.process(informationExtractionContext);
        Assert.assertEquals(MESSAGE_ID_AFTER, message.getId());
        Assert.assertNotSame(MESSAGE_ID_BEFORE, message.getId());
    }

    @Test
    public void testInternalMessage() throws CommandException {
        Assert.assertEquals(MESSAGE_ID_BEFORE, message.getId());
        command.process(informationExtractionContext);
        Assert.assertEquals(MESSAGE_ID_BEFORE, message.getId());
        Assert.assertNotSame(MESSAGE_ID_AFTER, message.getId());
    }
}
