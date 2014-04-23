package de.spektrumprojekt.i.evaluation.runner.shorttermanalysis;

import java.util.Date;
import java.util.LinkedList;

import junit.framework.Assert;

import org.junit.Test;

import de.spektrumprojekt.i.evaluation.runner.shorttermanalysis.ShortTermAnalysisHelper;

public class ShortTermAnalysisHelperTest {

    private final LinkedList<Date> dates = new LinkedList<Date>();

    @Test
    public void testGetIndex() {
        dates.clear();
        Date testDateMinus1 = null;
        Date testDate0 = null;
        Date testDate15 = null;
        Date testDate14 = null;
        Date testDate16 = null;
        for (int i = 0; i < 100; i++) {
            Date date = new Date();
            dates.addLast(date);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (i == 0) {
                testDateMinus1 = new Date(date.getTime() - 1);
                testDate0 = new Date(date.getTime());
            }
            if (i == 15) {
                testDate15 = new Date(date.getTime());
                testDate14 = new Date(date.getTime() - 1);
                testDate16 = new Date(date.getTime() + 15);
            }
        }
        Assert.assertEquals(-1, ShortTermAnalysisHelper.getIndex(dates, testDateMinus1));
        Assert.assertEquals(0, ShortTermAnalysisHelper.getIndex(dates, testDate0));
        Assert.assertEquals(14, ShortTermAnalysisHelper.getIndex(dates, testDate14));
        Assert.assertEquals(15, ShortTermAnalysisHelper.getIndex(dates, testDate15));
        Assert.assertEquals(16, ShortTermAnalysisHelper.getIndex(dates, testDate16));
    }

    @Test
    public void testSort() {
        dates.clear();
        Date testDate0 = null;
        Date testDate14 = null;
        Date testDate16 = null;
        for (int i = 0; i < 100; i++) {
            Date date = new Date();
            dates.addFirst(date);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (i == 0) {
                testDate0 = new Date(date.getTime());
            }
            if (i == 14) {
                testDate14 = new Date(date.getTime());
            }
            if (i == 16) {
                testDate16 = new Date(date.getTime());
            }
        }
        ShortTermAnalysisHelper.sort(dates);
        Assert.assertEquals(testDate0, dates.get(0));
        Assert.assertEquals(testDate14, dates.get(14));
        Assert.assertEquals(testDate16, dates.get(16));
        Assert.assertEquals(14, ShortTermAnalysisHelper.getIndex(dates, testDate14));
    }

}
