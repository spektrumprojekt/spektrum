package de.spektrumprojekt.i.ranker.feature;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.spektrumprojekt.datamodel.message.InteractionLevel;
import de.spektrumprojekt.i.datamodel.MessageFeature;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class FeatureAggregate {

    public static int getLength() {
        return Feature.ALL_FEATURES.size() + 1;
    }

    public static String getSimpleFeaturesHeader() {
        return StringUtils.join(new String[] {
                Feature.toStringHeader(),
                "InteractionLevel"
        }, " ");
    }

    private InteractionLevel interactionLevel;

    private final Map<Feature, MessageFeature> features = new HashMap<Feature, MessageFeature>();

    public FeatureAggregate() {
    }

    public FeatureAggregate(Map<Feature, MessageFeature> features) {
        this.features.putAll(features);
    }

    public FeatureAggregate(String[] stats) {
        if (stats.length < getLength()) {

            throw new IllegalArgumentException("minLength is " + getLength() + " but only got "
                    + stats.length + " stats=" + StringUtils.join(stats, " "));

        }
        int index = parseFeatures(stats);
        interactionLevel = InteractionLevel.fromNumberValue(Integer.parseInt(stats[index++]));
        if (interactionLevel == null) {
            throw new IllegalArgumentException("Error reading rank. Invalid interactionLevel: "
                    + stats[index - 1] + " Line was: " + stats);
        }
    }

    public float getFeatureValue(Feature feature) {
        MessageFeature messageFeature = this.features.get(feature);
        if (messageFeature == null) {
            return 0;
        }
        return messageFeature.getValue();
    }

    public InteractionLevel getInteractionLevel() {
        return interactionLevel;
    }

    public MessageFeature getMessageFeature(Feature feature) {
        return this.features.get(feature);
    }

    private int parseFeatures(String[] stats) {
        int index = 0;
        for (Feature feature : Feature.ALL_FEATURES) {
            float num = Float.parseFloat(stats[index++]);
            MessageFeature mf = new MessageFeature(feature);
            mf.setValue(num);
            this.features.put(feature, mf);

        }
        return index;
    }

    public void setInteractionLevel(InteractionLevel interactionLevel) {
        this.interactionLevel = interactionLevel;
    }

    public String toParseableString() {
        String featuresStr = Feature.toString(features, " ", false);
        return StringUtils.join(
                new String[] {
                        featuresStr,
                        "" + interactionLevel.getNumberValue(),
                }, " ");
    }

    @Override
    public String toString() {
        return this.toParseableString();
    }

}
