package de.spektrumprojekt.i.scorer.feature;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public interface FeatureAggregator extends ConfigurationDescriptable {

    public float aggregate(FeatureAggregate featureAggregate);

}