package de.spektrumprojekt.informationextraction.extractors;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Date;

import org.junit.Test;

import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;
import de.spektrumprojekt.persistence.simple.SimplePersistence;

public class StemmedTokenExtractorTest {

    private static final String TEXT = "Die vom Verein für Internet-Benutzer Österreichs gestartete Bürgerinitiative hat bereits 4.471 Unterschriften auf Papier gesammelt und ans Parlament übermittelt. Nun muss sich der Nationalratsausschuss für Petitionen und Bürgerinitiativen damit befassen.";

    @Test
    public void testStemmedTokenExtractor() {
        StemmedTokenExtractorCommand stemmedTokenExtractor = new StemmedTokenExtractorCommand(
                false, false, 0);

        Message message = new Message(MessageType.CONTENT, StatusType.OK, new Date());
        MessagePart messagePart = new MessagePart(MimeType.TEXT_PLAIN, TEXT);
        message.addMessagePart(messagePart);
        message.addProperty(new Property(LanguageDetectorCommand.LANGUAGE, "de"));

        InformationExtractionContext context = new InformationExtractionContext(
                new SimplePersistence(), message, messagePart);
        context.setCleanText(TEXT);
        stemmedTokenExtractor.process(context);

        Collection<ScoredTerm> scoredTerms = messagePart.getScoredTerms();
        assertEquals(16, scoredTerms.size());
    }

}
