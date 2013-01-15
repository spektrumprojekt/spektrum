package de.spektrumprojekt.informationextraction.extractors;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;
import de.spektrumprojekt.informationextraction.extractors.UrlExtractorCommand;
import de.spektrumprojekt.persistence.simple.PersistenceMock;

public class UrlExtractorTest {

    private static final String TEXT = "Arsenal confirm signing of Spain international Santi Cazorla http://gu.com/p/39hfg/tw via @guardian_sport";

    @Test
    public void testUrlExtractor() {
        UrlExtractorCommand extractor = new UrlExtractorCommand();

        Message message = new Message(MessageType.CONTENT, StatusType.OK, new Date());
        MessagePart messagePart = new MessagePart(MimeType.TEXT_PLAIN, TEXT);
        message.addMessagePart(messagePart);
        InformationExtractionContext context = new InformationExtractionContext(new PersistenceMock(), message,
                messagePart);
        extractor.process(context);

        String extractedUrl = message.getPropertiesAsMap().get(UrlExtractorCommand.EXTRACTED_URL).getPropertyValue();
        assertEquals("http://gu.com/p/39hfg/tw", extractedUrl);
    }

}
