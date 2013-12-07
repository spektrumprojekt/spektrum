package de.spektrumprojekt.i.similarity;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.message.Term.TermCategory;

public class SetSimilarityTest {

    private Term[] t;

    private final static double E = 0.01d;

    @Before
    public void setup() {

        Term t1 = new Term(TermCategory.TERM, "test1");
        t1.setId(1l);
        Term t2 = new Term(TermCategory.TERM, "test2");
        t2.setId(2l);
        Term t3 = new Term(TermCategory.TERM, "test3");
        t3.setId(3l);
        Term t4 = new Term(TermCategory.TERM, "test4");
        t4.setId(4l);
        t = new Term[] {
                t1, t2, t3, t4
        };
    }

    @Test
    public void testDice() {

        SetSimilarity setSimilarity;
        setSimilarity = new DiceSetSimilarity();

        // boundaries
        Assert.assertEquals(1f,
                setSimilarity.computeSimilarity(Arrays.asList(t[0], t[1], t[2]),
                        Arrays.asList(t[0], t[1], t[2])), E);
        Assert.assertEquals(0f,
                setSimilarity.computeSimilarity(Arrays.asList(t[0], t[1], t[2]),
                        Arrays.asList(t[3])), E);
        Assert.assertEquals(0f,
                setSimilarity.computeSimilarity(Arrays.asList(t[0]),
                        new ArrayList<Term>()), E);

        // some vals
        Assert.assertEquals(2 / 5f,
                setSimilarity.computeSimilarity(
                        Arrays.asList(t[0], t[1], t[2]),
                        Arrays.asList(t[1], t[3])),
                E);
        Assert.assertEquals(2 / 4f,
                setSimilarity.computeSimilarity(
                        Arrays.asList(t[1], t[2]),
                        Arrays.asList(t[1], t[3])),
                E);
        Assert.assertEquals(0 / 4f,
                setSimilarity.computeSimilarity(
                        Arrays.asList(t[2]),
                        Arrays.asList(t[1], t[3], t[0])),
                E);
        Assert.assertEquals(6 / 7f,
                setSimilarity.computeSimilarity(
                        Arrays.asList(t[0], t[1], t[2], t[3]),
                        Arrays.asList(t[0], t[1], t[3])),
                E);
    }

    @Test
    public void testJaccard() {
        SetSimilarity setSimilarity;
        setSimilarity = new JaccardSetSimialrity();

        Assert.assertEquals(1f,
                setSimilarity.computeSimilarity(Arrays.asList(t[0], t[1], t[2]),
                        Arrays.asList(t[0], t[1], t[2])), E);
        Assert.assertEquals(0f,
                setSimilarity.computeSimilarity(Arrays.asList(t[0], t[1], t[2]),
                        Arrays.asList(t[3])), E);
        Assert.assertEquals(0f,
                setSimilarity.computeSimilarity(Arrays.asList(t[0]),
                        new ArrayList<Term>()), E);

        // some vals
        Assert.assertEquals(1 / 4f,
                setSimilarity.computeSimilarity(
                        Arrays.asList(t[0], t[1], t[2]),
                        Arrays.asList(t[1], t[3])),
                E);
        Assert.assertEquals(1 / 3f,
                setSimilarity.computeSimilarity(
                        Arrays.asList(t[1], t[2]),
                        Arrays.asList(t[1], t[3])),
                E);
        Assert.assertEquals(0f,
                setSimilarity.computeSimilarity(
                        Arrays.asList(t[2]),
                        Arrays.asList(t[1], t[3], t[0])),
                E);
    }
}
