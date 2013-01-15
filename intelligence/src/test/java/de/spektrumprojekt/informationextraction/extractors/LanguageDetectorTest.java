package de.spektrumprojekt.informationextraction.extractors;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;
import de.spektrumprojekt.informationextraction.extractors.LanguageDetectorCommand;
import de.spektrumprojekt.persistence.simple.PersistenceMock;

public class LanguageDetectorTest {

    @Test
    public void testLanguageDetection() {
        LanguageDetectorCommand languageExtractor = new LanguageDetectorCommand("en", Arrays.asList("de", "en", "fr"));

        Message message = new Message(MessageType.CONTENT, StatusType.OK, new Date());
        MessagePart messagePart = new MessagePart(MimeType.TEXT_PLAIN, "das ist ein deutscher Text");
        message.addMessagePart(messagePart);
        InformationExtractionContext context = new InformationExtractionContext(new PersistenceMock(), message,
                messagePart);
        context.setCleanText("das ist ein deutscher Text");
        languageExtractor.process(context);
        assertEquals("de", message.getPropertiesAsMap().get(LanguageDetectorCommand.LANGUAGE).getPropertyValue());

        message = new Message(MessageType.CONTENT, StatusType.OK, new Date());
        messagePart = new MessagePart(MimeType.TEXT_PLAIN, "this is a text in english");
        message.addMessagePart(messagePart);
        context = new InformationExtractionContext(new PersistenceMock(), message, messagePart);
        context.setCleanText("this is a text in english");
        languageExtractor.process(context);
        assertEquals("en", message.getPropertiesAsMap().get(LanguageDetectorCommand.LANGUAGE).getPropertyValue());

    }

}
