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

package de.spektrumprojekt.i.scorer.chain;

import java.util.Map;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.UserMessageScore;
import de.spektrumprojekt.i.scorer.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.i.scorer.feature.Feature;
import de.spektrumprojekt.i.scorer.feature.FeatureAggregator;
import de.spektrumprojekt.i.scorer.feature.FixWeightFeatureAggregator;

/**
 * Command for computing the message rank out of the features
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class ComputeMessageScoreCommand implements Command<UserSpecificMessageFeatureContext> {

    private final float nonParticipationFactor;

    private final FeatureAggregator featureAggregator;

    public ComputeMessageScoreCommand(Map<Feature, Float> featureWeights,
            float nonParticipationFactor) {
        this.nonParticipationFactor = nonParticipationFactor;
        this.featureAggregator = new FixWeightFeatureAggregator(featureWeights);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " nonParticipationFactor: "
                + nonParticipationFactor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(UserSpecificMessageFeatureContext context) {

        if (context.getFeatureAggregate() == null) {
            throw new IllegalStateException("featureAggregate must be computed before. context="
                    + context);
        }

        UserMessageScore messageScore = new UserMessageScore(context.getMessage().getGlobalId(),
                context.getUserGlobalId());
        context.setMessageRank(messageScore);
        messageScore.setInteractionLevel(context.getInteractionLevel());

        boolean isNonParticipatingAnswer = !context.check(Feature.MESSAGE_ROOT_FEATURE, 1);

        float score = this.featureAggregator.aggregate(context.getFeatureAggregate());

        if (isNonParticipatingAnswer) {
            score *= nonParticipationFactor;
        }
        messageScore.setScore(score);
        messageScore.setBasedOnAdaptedTerms(context.isMatchingUserModelEntriesContainsAdapted());

    }
}
