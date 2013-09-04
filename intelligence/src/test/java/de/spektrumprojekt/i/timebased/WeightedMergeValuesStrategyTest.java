package de.spektrumprojekt.i.timebased;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import de.spektrumprojekt.i.timebased.config.MergeValuesStrategy;
import de.spektrumprojekt.i.timebased.config.ShortTermMemoryConfiguration;

public class WeightedMergeValuesStrategyTest {

    private static final String nameUserModel1 = "1";
    private static final String nameUserModel2 = "2";

    @Test
    public void test() {
        ShortTermMemoryConfiguration configuration = new ShortTermMemoryConfiguration(null,
                MergeValuesStrategy.WEIGHTED, 0);
        configuration.putRatingWeight(nameUserModel1, 0.7f);
        configuration.putRatingWeight(nameUserModel2, 0.3f);
        WeightedMergeValuesStrategy strategy = new WeightedMergeValuesStrategy(configuration);
        Map<String, Float> values = new HashMap<String, Float>();
        values.put(nameUserModel1, 1f);
        values.put(nameUserModel2, 1f);
        Assert.assertEquals(1f, strategy.merge(values));
        values.put(nameUserModel1, 2f);
        values.put(nameUserModel2, 4f);
        Assert.assertEquals(2.6f, strategy.merge(values));
        values.put(nameUserModel1, 400f);
        values.put(nameUserModel2, 20f);
        Assert.assertEquals(286f, strategy.merge(values));

    }
}
