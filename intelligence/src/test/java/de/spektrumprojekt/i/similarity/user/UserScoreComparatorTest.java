package de.spektrumprojekt.i.similarity.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

/**
 * 
 * Test the user score comparator
 * 
 */
public class UserScoreComparatorTest {

    @Test
    public void testSort() {

        List<UserScore> scores = new ArrayList<UserScore>();

        for (int i = 0; i < 100; i++) {
            UserScore us = new UserScore(i + "", i / 100d);
            scores.add(us);
        }

        Collections.shuffle(scores);

        Collections.sort(scores, UserScoreComparator.INSTANCE);

        UserScore last = null;
        for (UserScore us : scores) {
            if (last != null) {
                // comparator sorts highest first (descending)
                Assert.assertTrue("Last score: " + last.getScore() + " Current: " + us.getScore(),
                        last.getScore() >= us.getScore());
            }
            last = us;
        }

    }

}
