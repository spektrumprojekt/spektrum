package de.spektrumprojekt.i.ranker;

import java.util.HashMap;
import java.util.Map;

import de.spektrumprojekt.i.datamodel.MessageFeature;
import de.spektrumprojekt.i.ranker.chain.features.Feature;

public class FeatureContext {
    private final Map<Feature, MessageFeature> features = new HashMap<Feature, MessageFeature>();

    /**
     * 
     * @param feature
     *            add the message feature
     */
    public void addMessageFeature(MessageFeature feature) {
        this.features.put(feature.getFeatureId(), feature);
    }

    /**
     * Check if the context contains a the given feature, and if so check if it has a minimum value
     * ((greater or equal) as provided.
     * 
     * @param feature
     *            the feature to get
     * @param minValue
     *            the minimum value to reach (0.5 means >= 0.5)
     * @return true if the feature has a score of minValue or bigger
     */
    public boolean check(Feature feature, float minValue) {
        MessageFeature messageFeature = this.getFeature(feature);
        return messageFeature != null && messageFeature.getValue() >= minValue;
    }

    /**
     * 
     * @param feature
     *            the feature to get
     * @return the according message feature or null
     */
    public MessageFeature getFeature(Feature feature) {
        return this.features.get(feature);
    }

    /**
     * 
     * @return the features (only for this context, not the user specific ones)
     */
    public Map<Feature, MessageFeature> getFeatures() {
        return features;
    }

    /**
     * 
     * @param feature
     * @return the value of the feature or 0 if not existing
     */
    public float getFeatureValue(Feature feature) {
        MessageFeature messageFeature = this.getFeature(feature);
        return messageFeature == null ? 0 : messageFeature.getValue();
    }

}
