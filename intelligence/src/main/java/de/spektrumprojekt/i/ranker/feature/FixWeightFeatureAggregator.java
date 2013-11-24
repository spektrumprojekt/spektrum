package de.spektrumprojekt.i.ranker.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.spektrumprojekt.datamodel.observation.Interest;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class FixWeightFeatureAggregator implements FeatureAggregator {

    public static Map<Feature, Float> getFixedDefaults4Learning(boolean learnLowInterest) {

        Map<Feature, Float> featureWeights = new HashMap<Feature, Float>();

        featureWeights.put(Feature.AUTHOR_FEATURE, Interest.EXTREME.getScore());
        featureWeights.put(Feature.MENTION_FEATURE, Interest.HIGH.getScore());
        featureWeights.put(Feature.LIKE_FEATURE, Interest.HIGH.getScore());
        featureWeights.put(Feature.DISCUSSION_PARTICIPATION_FEATURE, Interest.HIGH.getScore());
        featureWeights.put(Feature.DISCUSSION_MENTION_FEATURE, Interest.HIGH.getScore());

        if (learnLowInterest) {
            featureWeights
                    .put(Feature.DISCUSSION_NO_PARTICIPATION_FEATURE, Interest.LOW.getScore());
            featureWeights.put(Feature.DISCUSSION_NO_MENTION_FEATURE, Interest.LOW.getScore());
        }

        return featureWeights;
    }

    public static Map<Feature, Float> getFixedDefaults4Scoring(boolean onlyUseContentMatchFeature) {

        Map<Feature, Float> featureWeights = new HashMap<Feature, Float>();

        if (!onlyUseContentMatchFeature) {
            featureWeights.put(Feature.AUTHOR_FEATURE, 1f);
            featureWeights.put(Feature.MENTION_FEATURE, 0.95f);
            featureWeights.put(Feature.DISCUSSION_PARTICIPATION_FEATURE, 0.9f);
            featureWeights.put(Feature.DISCUSSION_MENTION_FEATURE, 0.8f);
        }
        featureWeights.put(Feature.CONTENT_MATCH_FEATURE, 1f);

        return featureWeights;
    }

    private final Map<Feature, Float> featureWeights = new HashMap<Feature, Float>();

    public FixWeightFeatureAggregator(Map<Feature, Float> featureWeights) {
        this.featureWeights.putAll(featureWeights);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.spektrumprojekt.i.ranker.feature.FeatureAggregator#aggregate(de.spektrumprojekt.i.ranker
     * .feature.FeatureAggregate)
     */
    @Override
    public float aggregate(FeatureAggregate featureAggregate) {

        float max = 0;
        for (Entry<Feature, Float> featureWeight : featureWeights.entrySet()) {
            float weight = featureWeight.getValue().floatValue();
            float value = featureAggregate.getFeatureValue(featureWeight.getKey());
            float score = weight * value;
            max = Math.max(score, max);
        }
        return Math.min(max, 1);
    }

    @Override
    public String getConfigurationDescription() {
        return this.toString();
    }

    public Map<Feature, Float> getFeatureWeights() {
        return featureWeights;
    }

    @Override
    public String toString() {
        return "FeatureAggregator [featureWeights=" + featureWeights + "]";
    }
}