package de.spektrumprojekt.i.evaluation.runner.shorttermanalysis;

import java.io.File;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.message.Term.TermCategory;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.i.evaluation.runner.shorttermanalysis.ShortTermAnalysis;
import de.spektrumprojekt.i.evaluation.runner.shorttermanalysis.ShortTermConfiguration;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.simple.SimplePersistence;

public class ShortTermAnalysisTest {

    private ShortTermConfiguration configuration;
    private Persistence persistence;

    @Test
    public void continueFromFile() {
        ShortTermAnalysis analysis = new ShortTermAnalysis(configuration);
        analysis.doAnalysis();
        analysis.doEnergyAnalysisOnly();

    }

    @Before
    public void initialize() {
        persistence = new SimplePersistence();
        Date messageDate = new Date();
        Message message = new Message(MessageType.CONTENT, StatusType.OK, messageDate);
        MessagePart messagePart = new MessagePart(MimeType.TEXT_PLAIN, "123 456");
        messagePart.addScoredTerm(new ScoredTerm(new Term(TermCategory.TERM, "123"), 0.9f));
        messagePart.addScoredTerm(new ScoredTerm(new Term(TermCategory.TERM, "456"), 0.9f));
        messagePart.addScoredTerm(new ScoredTerm(new Term(TermCategory.TERM, "7^8"), 0.9f));
        message.addMessagePart(messagePart);
        persistence.storeMessage(message);

        configuration = new ShortTermConfiguration();
        configuration.setFolderPath(System.getProperty("user.dir") + File.separator
                + "analysisTest");
        configuration.setStartDate(new Date(messageDate.getTime() - 1000));
        configuration.setEndDate(new Date(messageDate.getTime() + 1000));
        configuration.setBinSize(400);
        configuration.setHistoryLength(new int[] { 2, 4 });
        configuration.setPersistence(persistence);
    }

    @Test
    public void test() {
        ShortTermAnalysis analysis = new ShortTermAnalysis(configuration);
        analysis.doAnalysis();
    }
}
