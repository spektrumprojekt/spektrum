package de.spektrumprojekt.datamodel.observation;

import junit.framework.Assert;

import org.junit.Test;

public class InterestTest {

    @Test
    public void testSplit() {

        Assert.assertEquals(Interest.NONE, Interest.match(-1f));
        Assert.assertEquals(Interest.NONE, Interest.match(-0.5f));
        Assert.assertEquals(Interest.NONE, Interest.match(0));
        Assert.assertEquals(Interest.NONE, Interest.match(0.05f));
        Assert.assertEquals(Interest.NONE, Interest.match(0.1f));
        Assert.assertEquals(Interest.NONE, Interest.match(0.124f));
        Assert.assertEquals(Interest.LOW, Interest.match(0.125f));
        Assert.assertEquals(Interest.LOW, Interest.match(0.15f));
        Assert.assertEquals(Interest.LOW, Interest.match(0.2f));
        Assert.assertEquals(Interest.LOW, Interest.match(0.25f));
        Assert.assertEquals(Interest.LOW, Interest.match(0.3f));
        Assert.assertEquals(Interest.LOW, Interest.match(0.35f));
        Assert.assertEquals(Interest.LOW, Interest.match(0.374f));
        Assert.assertEquals(Interest.NORMAL, Interest.match(0.375f));
        Assert.assertEquals(Interest.NORMAL, Interest.match(0.4f));
        Assert.assertEquals(Interest.NORMAL, Interest.match(0.45f));
        Assert.assertEquals(Interest.NORMAL, Interest.match(0.5f));
        Assert.assertEquals(Interest.NORMAL, Interest.match(0.55f));
        Assert.assertEquals(Interest.NORMAL, Interest.match(0.6f));
        Assert.assertEquals(Interest.NORMAL, Interest.match(0.624f));
        Assert.assertEquals(Interest.HIGH, Interest.match(0.625f));
        Assert.assertEquals(Interest.HIGH, Interest.match(0.65f));
        Assert.assertEquals(Interest.HIGH, Interest.match(0.7f));
        Assert.assertEquals(Interest.HIGH, Interest.match(0.75f));
        Assert.assertEquals(Interest.HIGH, Interest.match(0.8f));
        Assert.assertEquals(Interest.HIGH, Interest.match(0.85f));
        Assert.assertEquals(Interest.HIGH, Interest.match(0.874f));
        Assert.assertEquals(Interest.EXTREME, Interest.match(0.875f));
        Assert.assertEquals(Interest.EXTREME, Interest.match(0.9f));
        Assert.assertEquals(Interest.EXTREME, Interest.match(0.95f));
        Assert.assertEquals(Interest.EXTREME, Interest.match(1.0f));
        Assert.assertEquals(Interest.EXTREME, Interest.match(1.05f));
        Assert.assertEquals(Interest.EXTREME, Interest.match(10f));

    }
}
