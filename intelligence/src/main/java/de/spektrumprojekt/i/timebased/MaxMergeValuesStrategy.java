package de.spektrumprojekt.i.timebased;

import java.util.Map;

public class MaxMergeValuesStrategy implements MergeValuesStrategy {

    @Override
    public float merge(Map<String, Float> values) {
        float max = 0;
        for (float value : values.values()) {
            max = Math.max(max, value);
        }
        return max;
    }
}
