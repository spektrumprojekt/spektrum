package de.spektrumprojekt.i.similarity.messagegroup;

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
public class MessageGroupSimilarityComparatorTest {

    @Test
    public void testSort() {

        List<MessageGroupSimilarity> sims = new ArrayList<MessageGroupSimilarity>();

        for (int i = 0; i < 100; i++) {
            MessageGroupSimilarity us = new MessageGroupSimilarity((long) i, (long) i + 1);
            sims.add(us);
        }

        Collections.shuffle(sims);

        Collections.sort(sims, MessageGroupSimilarityComparator.INSTANCE);

        MessageGroupSimilarity last = null;
        for (MessageGroupSimilarity sim : sims) {
            if (last != null) {
                // comparator sorts highest first (descending)
                Assert.assertTrue("Last score: " + last.getSim() + " Current: " + sim.getSim(),
                        last.getSim() >= sim.getSim());
            }
            last = sim;
        }

    }

}
