package de.spektrumprojekt.i.ranker.feature;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;

public interface FeatureAggregator extends ConfigurationDescriptable {

    public float aggregate(FeatureAggregate featureAggregate);

}