package de.spektrumprojekt.i.timebased;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

public class MaxMergeValuesStrategyTest {

    private static final String nameUserModel1 = "1";
    private static final String nameUserModel2 = "2";

    @Test
    public void test() {
        MaxMergeValuesStrategy strategy = new MaxMergeValuesStrategy();
        Map<String, Float> values = new HashMap<String, Float>();
        values.put(nameUserModel1, 1f);
        values.put(nameUserModel2, 1f);
        Assert.assertEquals(1f, strategy.merge(values));
        values.put(nameUserModel1, 2f);
        values.put(nameUserModel2, 4f);
        Assert.assertEquals(4f, strategy.merge(values));
        values.put(nameUserModel1, 400f);
        values.put(nameUserModel2, 20f);
        Assert.assertEquals(400f, strategy.merge(values));

    }
}
