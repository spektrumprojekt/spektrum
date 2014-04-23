package de.spektrumprojekt.i.evaluation.runner.shorttermanalysis;

import java.io.File;
import java.util.Collection;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.testng.Assert;

import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.message.Term.TermCategory;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.i.evaluation.runner.shorttermanalysis.MessageTermProvider;
import de.spektrumprojekt.i.evaluation.runner.shorttermanalysis.ShortTermConfiguration;
import de.spektrumprojekt.i.evaluation.runner.shorttermanalysis.TermProvider.Entry;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.simple.SimplePersistence;

public class MessageTermProviderTest {

    private ShortTermConfiguration configuration;
    private Persistence persistence;

    @Before
    public void initialize() {
        persistence = new SimplePersistence();
        ;
        Date messageDate = new Date();
        Message message = new Message(MessageType.CONTENT, StatusType.OK, messageDate);
        MessagePart messagePart = new MessagePart(MimeType.TEXT_PLAIN, "123 456");
        messagePart.addScoredTerm(new ScoredTerm(new Term(TermCategory.TERM, "123"), 0.9f));
        messagePart.addScoredTerm(new ScoredTerm(new Term(TermCategory.TERM, "123"), 0.99f));
        messagePart.addScoredTerm(new ScoredTerm(new Term(TermCategory.TERM, "456"), 0.9f));
        messagePart.addScoredTerm(new ScoredTerm(new Term(TermCategory.TERM, "7^8"), 0.9f));
        message.addMessagePart(messagePart);
        message.addMessagePart(messagePart);
        persistence.storeMessage(message);
        message = new Message(MessageType.CONTENT, StatusType.OK, new Date(
                messageDate.getTime() + 2000));
        messagePart = new MessagePart(MimeType.TEXT_PLAIN, "123 456");
        messagePart.addScoredTerm(new ScoredTerm(new Term(TermCategory.TERM, "123"), 0.9f));
        messagePart.addScoredTerm(new ScoredTerm(new Term(TermCategory.TERM, "123"), 0.99f));
        messagePart.addScoredTerm(new ScoredTerm(new Term(TermCategory.TERM, "456"), 0.9f));
        messagePart.addScoredTerm(new ScoredTerm(new Term(TermCategory.TERM, "abc"), 0.2f));
        message.addMessagePart(messagePart);
        message.addMessagePart(messagePart);
        persistence.storeMessage(message);

        configuration = new ShortTermConfiguration();
        configuration.setFolderPath(System.getProperty("user.dir") + File.separator
                + "analysisTest");
        configuration.setStartDate(new Date(messageDate.getTime() - 1000));
        configuration.setEndDate(new Date(messageDate.getTime() + 5000));
        configuration.setBinSize(400);
        configuration.setHistoryLength(new int[] { 2, 4 });
        configuration.setPersistence(persistence);
    }

    @Test
    public void test() {
        MessageTermProvider provider = new MessageTermProvider(configuration.getPersistence(),
                configuration.getStartDate(), configuration.getEndDate());
        Collection<Entry> entries = provider.getTerms();
        Assert.assertEquals(entries.size(), 6);
        for (Entry entry : entries) {
            if (entry.getTerm().equals("123")) {
                Assert.assertEquals(entry.getScore(), 0.99f);
            }
            if (entry.getTerm().equals("456")) {
                Assert.assertEquals(entry.getScore(), 0.9f);
            }
            if (entry.getTerm().equals("7^8")) {
                Assert.assertEquals(entry.getScore(), 0.9f);
            }
            if (entry.getTerm().equals("abc")) {
                Assert.assertEquals(entry.getScore(), 0.2f);
            }
        }
    }
}
