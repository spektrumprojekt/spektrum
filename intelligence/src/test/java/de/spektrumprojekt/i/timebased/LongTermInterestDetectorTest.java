package de.spektrumprojekt.i.timebased;

import org.junit.Assert;
import org.junit.Test;

import de.spektrumprojekt.i.timebased.config.PermanentLongTermInterestDetector;

public class LongTermInterestDetectorTest {

    @Test
    public void algotithmsTest() {
        PeriodicLongTermInterestDetector periodicLongTermInterestDetector = new PeriodicLongTermInterestDetector(
                0.5f, 2, 3);
        PermanentLongTermInterestDetector permanentLongTermInterestDetector = new PermanentLongTermInterestDetector(
                0.5f, 3, 0.6f);
        float[] testArray1 = new float[] { 1, 0, 0, 1, 0, 0, 1 };
        float[] testArray2 = new float[] { 1, 0, 1, 1, 0, 0, 1 };
        float[] testArray3 = new float[] { 1, 0, 0, 1, 0, 0.3f, 1 };
        float[] testArray4 = new float[] { 1, 1, 0, 1, 0, 0, 1 };
        float[] testArray5 = new float[] { 1, 1, 0, 0, 1, 1, 0, 0, 1 };

        Assert.assertTrue(periodicLongTermInterestDetector.isLongTermInterest(testArray1));
        Assert.assertFalse(periodicLongTermInterestDetector.isLongTermInterest(testArray2));
        Assert.assertTrue(periodicLongTermInterestDetector.isLongTermInterest(testArray3));
        Assert.assertFalse(periodicLongTermInterestDetector.isLongTermInterest(testArray4));
        Assert.assertTrue(periodicLongTermInterestDetector.isLongTermInterest(testArray5));

        Assert.assertFalse(permanentLongTermInterestDetector.isLongTermInterest(testArray1));
        Assert.assertTrue(permanentLongTermInterestDetector.isLongTermInterest(testArray2));
        Assert.assertFalse(permanentLongTermInterestDetector.isLongTermInterest(testArray3));
        Assert.assertTrue(permanentLongTermInterestDetector.isLongTermInterest(testArray4));
        Assert.assertFalse(permanentLongTermInterestDetector.isLongTermInterest(testArray5));

        permanentLongTermInterestDetector = new PermanentLongTermInterestDetector(0.5f, 4, 0.5f);
        Assert.assertFalse(permanentLongTermInterestDetector.isLongTermInterest(testArray1));
        Assert.assertFalse(permanentLongTermInterestDetector.isLongTermInterest(testArray2));
        Assert.assertFalse(permanentLongTermInterestDetector.isLongTermInterest(testArray3));
        Assert.assertFalse(permanentLongTermInterestDetector.isLongTermInterest(testArray4));
        Assert.assertTrue(permanentLongTermInterestDetector.isLongTermInterest(testArray5));
    }
}
