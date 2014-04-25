package de.spektrumprojekt.i.evaluation.runner.shorttermanalysis;

import org.junit.Assert;
import org.junit.Test;

import de.spektrumprojekt.i.evaluation.runner.shorttermanalysis.TermRecord;

public class TermRecordTest {

    @Test
    public void testInitialisation() {
        String term = "test";
        TermRecord record = new TermRecord(term, 3);
        Assert.assertEquals(0, record.getEntry(2), 0);
    }

}
