package de.spektrumprojekt.i.evaluation.recallstats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import de.spektrumprojekt.i.evaluation.helper.SpektrumRating;
import de.spektrumprojekt.i.evaluation.runner.crossvalidation.CrossValidationPerUserRatingSplitter;
import de.spektrumprojekt.i.evaluation.runner.crossvalidation.CrossValidationRatingSplitter;

public class CrossValidationRatingSplitterTest {

    private List<SpektrumRating> generateRatings(int size) {

        List<SpektrumRating> ratings = new ArrayList<SpektrumRating>();

        for (int i = 0; i < size; i++) {
            SpektrumRating spektrumRating = new SpektrumRating();
            spektrumRating.setUserGlobalId(i % 7 + "");

            ratings.add(spektrumRating);
        }

        return ratings;
    }

    @Test
    public void test1000() {

        testSplitting(1000, 10, 100, 100, false);

    }

    @Test
    public void test1000User() {

        testSplitting(1000, 10, 105, 55, true);

    }

    @Test
    public void test1001() {
        testSplitting(1001, 10, 101, 92, false);

    }

    @Test
    public void test1001User() {
        testSplitting(1001, 10, 105, 56, true);

    }

    @Test
    public void test999() {
        testSplitting(999, 10, 100, 99, false);

    }

    @Test
    public void test999User() {
        testSplitting(999, 10, 105, 54, true);

    }

    private void testSplitting(int size, int n, int expectedPartionSize, int lastExpected,
            boolean beUserSpecific) {
        CrossValidationRatingSplitter splitter;

        if (beUserSpecific) {
            splitter = new CrossValidationPerUserRatingSplitter(generateRatings(size), n, false);
        } else {
            splitter = new CrossValidationRatingSplitter(
                    generateRatings(size), n, false);
        }

        for (int i = 0; i < n; i++) {
            Collection<SpektrumRating> test = new HashSet<SpektrumRating>(
                    splitter.getTestRatings(i));
            Collection<SpektrumRating> training = new HashSet<SpektrumRating>(
                    splitter.getTrainingRatings(i));

            if (i + 1 < n) {
                Assert.assertEquals(test.size(), expectedPartionSize, "Test Size");
                Assert.assertEquals(training.size(), size - expectedPartionSize, "Training Size");
            } else {
                Assert.assertEquals(test.size(), lastExpected, "Test Size");
                Assert.assertEquals(training.size(), size - lastExpected, "Training Size");
            }

            for (SpektrumRating t : test) {
                Assert.assertFalse(training.contains(t));
            }
            Assert.assertTrue(test.size() + training.size() == size);
        }
    }
}
