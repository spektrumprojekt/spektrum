package de.spektrumprojekt.i.timebased;

import java.util.Map;

import de.spektrumprojekt.i.timebased.config.ShortTermMemoryConfiguration;

public class WeightedMergeValuesStrategy implements MergeValuesStrategy {

    private final Map<String, Float> raitingWeights;
    private final boolean balanceMisingUserModelWeights;

    public WeightedMergeValuesStrategy(ShortTermMemoryConfiguration configuration) {
        super();
        this.balanceMisingUserModelWeights = configuration.isBalanceMisingUserModelWeights();
        this.raitingWeights = configuration.getRaitingWeights();
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
            result += raitingWeights.get(key) * values.get(key);
        }
        return result * missingModelFactor;
    }

}
