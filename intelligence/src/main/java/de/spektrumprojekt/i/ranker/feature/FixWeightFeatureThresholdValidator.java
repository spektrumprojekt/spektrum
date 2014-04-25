package de.spektrumprojekt.i.ranker.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;

/**
 * Checks the features of a message against some fixed threshold. If all threshold are fullfilled
 * for a message #check returns null.
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class FixWeightFeatureThresholdValidator implements ConfigurationDescriptable {

    private final Map<Feature, Float> featureLimits = new HashMap<Feature, Float>();

    public FixWeightFeatureThresholdValidator(Map<Feature, Float> featureLimits) {
        this.featureLimits.putAll(featureLimits);
    }

    public boolean validate(FeatureAggregate featureAggregate) {

        for (Entry<Feature, Float> featureLimit : featureLimits.entrySet()) {
            float limit = featureLimit.getValue().floatValue();
            float value = featureAggregate.getFeatureValue(featureLimit.getKey());
            if (value < limit) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getConfigurationDescription() {
        return this.toString();
    }

    public Map<Feature, Float> getFeatureLimits() {
        return featureLimits;
    }

    @Override
    public String toString() {
        return "FixWeightFeatureThreshold [featureLimits=" + featureLimits + "]";
    }
}