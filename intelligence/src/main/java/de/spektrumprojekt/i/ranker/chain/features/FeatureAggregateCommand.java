/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.spektrumprojekt.i.ranker.chain.features;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.InteractionLevel;
import de.spektrumprojekt.datamodel.user.UserSimilarity;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.i.ranker.feature.Feature;
import de.spektrumprojekt.i.ranker.feature.FeatureAggregate;
import de.spektrumprojekt.persistence.Persistence;

/**
 * Command feature to compute the feature aggregate
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class FeatureAggregateCommand implements Command<UserSpecificMessageFeatureContext> {

    private final Persistence persistence;

    public FeatureAggregateCommand(Persistence persistence) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence is null.");
        }
        this.persistence = persistence;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(UserSpecificMessageFeatureContext context) {

        String userGlobalId = context.getUserGlobalId();

        UserSimilarity simTo = persistence.getUserSimilarity(context.getMessage()
                .getAuthorGlobalId(), userGlobalId, context.getMessage().getMessageGroup()
                .getGlobalId());
        UserSimilarity simFrom = persistence.getUserSimilarity(userGlobalId, context.getMessage()
                .getAuthorGlobalId(), context.getMessage().getMessageGroup().getGlobalId());

        float to = simTo == null ? 0 : (float) simTo.getSimilarity();
        float from = simFrom == null ? 0 : (float) simFrom.getSimilarity();

        context.addMessageFeature(Feature.USER_TO_SIM_FEATURE, to);
        context.addMessageFeature(Feature.USER_TO_SIM_FEATURE, from);

        FeatureAggregate featureAggregate = new FeatureAggregate(context.getFeatures());

        featureAggregate.setInteractionLevel(context.getInteractionLevel());
        if (featureAggregate.getInteractionLevel() == null) {
            throw new IllegalArgumentException("interactionLevel cannot be null! context="
                    + context);
        }
        if (featureAggregate.getFeatureValue(Feature.AUTHOR_FEATURE) == 1f
                && !featureAggregate.getInteractionLevel().equals(InteractionLevel.DIRECT)) {
            throw new IllegalStateException("interactionLevel must be direct for authors! context="
                    + context);
        }

        context.setFeatureAggregate(featureAggregate);
    }
}
