package de.spektrumprojekt.i.timebased;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.message.Term.TermCategory;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.datamodel.user.UserModelEntryTimeBin;

public class BinAggregatedUserModelEntryDecoratorTest {

    @Test
    public void test() throws InterruptedException {
        for (int binsToAggregate = 1; binsToAggregate < 50; binsToAggregate++) {
            User user = new User("testUser");
            UserModel userModel = new UserModel(user, "testUserModel");
            Term term = new Term(TermCategory.TERM, "testTerm");
            ScoredTerm scoredTerm = new ScoredTerm(term, 9f);
            UserModelEntry entry = new UserModelEntry(userModel, scoredTerm);
            List<Long> estimatadStartTimes = new LinkedList<Long>();
            List<Long> estimatadTimesNotStarting = new LinkedList<Long>();
            for (int binNumber = 0; binNumber < 100; binNumber++) {
                UserModelEntryTimeBin timeBin = new UserModelEntryTimeBin(new Date().getTime());
                timeBin.setScoreCount(1);
                timeBin.setScoreSum(1);
                entry.addTimeBinEntry(timeBin);
                if (100 % binsToAggregate == binNumber % binsToAggregate) {
                    estimatadStartTimes.add(timeBin.getTimeBinStart());
                } else {
                    estimatadTimesNotStarting.add(timeBin.getTimeBinStart());
                }
                Thread.sleep(1);
            }
            BinAggregatedUserModelEntryDecorator aggregatedEntry = new BinAggregatedUserModelEntryDecorator(
                    binsToAggregate);
            aggregatedEntry.setEntry(entry);
            Assert.assertEquals(100 / binsToAggregate, aggregatedEntry.getTimeBinEntries().size());
            Assert.assertEquals(aggregatedEntry.getTimeBinEntries().size(),
                    estimatadStartTimes.size());
            for (Long startTime : estimatadStartTimes) {
                UserModelEntryTimeBin timeBin = aggregatedEntry
                        .getUserModelEntryTimeBinByStartTime(startTime);
                Assert.assertNotNull(timeBin);
                Assert.assertEquals((float) binsToAggregate, timeBin.getScoreCount());
                Assert.assertEquals((float) binsToAggregate, timeBin.getScoreSum());
            }
            for (Long startTime : estimatadTimesNotStarting) {
                Assert.assertNull(aggregatedEntry.getUserModelEntryTimeBinByStartTime(startTime));
            }
            long lastStartTime = 0;
            for (UserModelEntryTimeBin timeBin : aggregatedEntry.getTimeBinEntries()) {
                long currentStartTime = timeBin.getTimeBinStart();
                Assert.assertTrue(currentStartTime > lastStartTime);
                lastStartTime = currentStartTime;
            }
        }
    }
}
