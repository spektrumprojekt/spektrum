package de.spektrumprojekt.i.evaluation.runner.shorttermanalysis;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
import de.spektrumprojekt.i.evaluation.runner.shorttermanalysis.GainTopFilter;
import de.spektrumprojekt.i.evaluation.runner.shorttermanalysis.NutritionHistory;
import de.spektrumprojekt.i.evaluation.runner.shorttermanalysis.ShortTermAnalysisHelper;
import de.spektrumprojekt.i.evaluation.runner.shorttermanalysis.ShortTermConfiguration;
import de.spektrumprojekt.i.evaluation.runner.shorttermanalysis.TermHistory;
import de.spektrumprojekt.i.evaluation.runner.shorttermanalysis.TermRecord;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.simple.SimplePersistence;

public class GainTopFilterTest {

    private ShortTermConfiguration configuration;
    private Persistence persistence;
    private final List<Date> startTimes = new LinkedList<Date>();
    private NutritionHistory history;

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
        configuration.setStartDate(new Date(0));
        configuration.setEndDate(new Date(1000));
        configuration.setBinSize(250);
        configuration.setPersistence(persistence);
        configuration.setTopCount(100);

        ShortTermAnalysisHelper.extractBinStarttimes(configuration, startTimes);

        history = new NutritionHistory(startTimes);
        for (int i = 1; i < 1000; i++) {
            TermRecord record = new TermRecord(String.valueOf(i), 100);
            history.put(record);
            if ((449 < i && i < 500)) {
                for (int j = 0; j < 100; j++) {
                    record.setEntry(j, j * (j + i - 450));
                }
            } else if ((649 < i && i < 700)) {
                for (int j = 0; j < 100; j++) {
                    record.setEntry(j, j * (j + i - 650) + 1);
                }

            } else {
                for (int j = 0; j < 100; j++) {
                    record.setEntry(j, j);
                }
            }
        }
    }

    @Test
    public void test() {

        GainTopFilter filter = new GainTopFilter(history, configuration);
        TermHistory termHistory = filter.analyse();
        int i = 0;
        int j = 1;
        Assert.assertEquals(100, termHistory.getRecords().size());
        for (TermRecord record : termHistory.getRecords()) {
            i++;
            if (i % 2 == 0) {
                Assert.assertEquals(record.getTerm(), String.valueOf(500 - j));
                j++;
            } else {
                Assert.assertEquals(record.getTerm(), String.valueOf(700 - j));
            }
        }
    }

    public void test2() {
        GainTopFilter filter = new GainTopFilter(history, configuration);
        TermRecord record = new TermRecord("1", 13);
        record.setEntries(new Double[] { 7.75, 2.75, 1.5, 0.0, 1.2000000029802322,
                15.476190522313118, 7.6500000059604645, 0.3333333432674408, 4.483333349227905,
                1.8333333432674408, 0.5, 1.0, 4.033333346247673 });
        Assert.assertEquals(filter.calculateScore(record), 15.476190522313118, 0);
        record = new TermRecord("1", 13);
        record.setEntries(new Double[] { 7.41071429848671, 5.0, 5.0, 0.0, 2.333333343267441,
                10.976190492510796, 4.708333343267441, 2.333333343267441, 6.333333343267441,
                0.8666666746139526, 1.2000000029802322, 1.0, 7.241666682995856 });
        Assert.assertEquals(filter.calculateScore(record), 10.976190492510796, 0);
    }
}
