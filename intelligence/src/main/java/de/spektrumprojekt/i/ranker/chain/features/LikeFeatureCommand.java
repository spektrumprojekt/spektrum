package de.spektrumprojekt.i.ranker.chain.features;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.datamodel.MessageFeature;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;

/**
 * Command for computing the Like Feature
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class LikeFeatureCommand implements Command<UserSpecificMessageFeatureContext> {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    /**
     * 
     * @return the feature
     */
    public Feature getFeatureId() {
        return Feature.MENTION_FEATURE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(UserSpecificMessageFeatureContext context) {
        MessageFeature feature = new MessageFeature(getFeatureId());

        if (MessageHelper.isLikedByUser(context.getMessage(),
                context.getUserGlobalId())) {
            feature.setValue(1f);
        }

        context.addMessageFeature(feature);
    }
}
