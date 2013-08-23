package de.spektrumprojekt.i.timebased;

import java.util.HashMap;
import java.util.Map;

public class WeightedMergeValuesStrategy implements MergeValuesStrategy {

    private boolean balanceMisingUserModelWeights;

    private final Map<String, Float> raitingWeights = new HashMap<String, Float>();

    public Float getRaitingWeight(Object key) {
        return raitingWeights.get(key);
    }

    public Map<String, Float> getRaitingWeights() {
        return raitingWeights;
    }

    public boolean isBalanceMisingUserModelWeights() {
        return balanceMisingUserModelWeights;
    }

    @Override
    public float merge(Map<String, Float> values) {
        float missingModelFactor = 1;
        if (balanceMisingUserModelWeights) {
            if (!(values.size() == raitingWeights.size())) {
                for (String userModelType : values.keySet()) {
                    missingModelFactor += raitingWeights.get(userModelType);
                }
                missingModelFactor = 1 / missingModelFactor;
            }
        }
        float result = 0;
        for (String key : values.keySet()) {
            result += getRaitingWeight(key) * values.get(key);
        }
        return result * missingModelFactor;
    }

    public Float putRatingWeight(String key, Float value) {
        return raitingWeights.put(key, value);
    }

    public void setBalanceMisingUserModelWeights(boolean balanceMisingUserModelWeights) {
        this.balanceMisingUserModelWeights = balanceMisingUserModelWeights;
    }
}
