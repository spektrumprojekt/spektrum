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

package de.spektrumprojekt.i.ranker.chain;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.user.UserSimilarity;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.i.ranker.feature.Feature;
import de.spektrumprojekt.persistence.Persistence;

/**
 * A command to store the message ranks
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class AdaptMessageRankByCMFOfSimilarUsersCommand implements Command<MessageFeatureContext> {

    private final Persistence persistence;

    // the minimum user similarity that must be fullfilled to be eglible to take the score of
    private final float minUserSimilarity;

    // the minimum score the cmf feature of a similar user must have such that it will be used.
    private final float minContentMessageScore;

    // the message rank threshold determines for which users the adaption will take place.
    private final float messageRankThreshold;

    private int adaptationCount;

    private float adaptationIncrease;

    /**
     * 
     * @param persistence
     *            the persistence to use
     * @param minUserSimilarity
     * @param minContentMessageScore
     * @param messageRankThreshold
     */
    public AdaptMessageRankByCMFOfSimilarUsersCommand(Persistence persistence,
            float messageRankThreshold, float minUserSimilarity, float minContentMessageScore) {

        this.persistence = persistence;
        this.minContentMessageScore = minContentMessageScore;
        this.minUserSimilarity = minUserSimilarity;
        this.messageRankThreshold = messageRankThreshold;
    }

    public int getAdaptationCount() {
        return adaptationCount;
    }

    public float getAdaptationIncrease() {
        return adaptationIncrease;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName()
                + " messageRankThreshold:" + messageRankThreshold
                + " minUserSimilarity:" + minUserSimilarity
                + " minContentMessageScore:" + minContentMessageScore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(MessageFeatureContext context) {
        if (context.isNoRankingOnlyLearning()) {
            return;
        }
        String messageGroupGlobalId = context.getMessage().getMessageGroup() == null ? null
                : context.getMessage().getMessageGroup().getGlobalId();
        for (UserSpecificMessageFeatureContext contextForAdaptation : context.getUserContexts()) {
            if (contextForAdaptation.getMessageRank().getScore() <= this.messageRankThreshold) {

                float newRank = 0;

                for (UserSpecificMessageFeatureContext userContext : context.getUserContexts()) {

                    float cmfRank = userContext.getFeatureValue(Feature.CONTENT_MATCH_FEATURE);

                    if (cmfRank > this.minContentMessageScore && cmfRank > newRank) {

                        UserSimilarity userSimilarity = this.persistence.getUserSimilarity(
                                contextForAdaptation.getUserGlobalId(),
                                userContext.getUserGlobalId(), messageGroupGlobalId);

                        if (userSimilarity != null
                                && userSimilarity.getSimilarity() >= this.minUserSimilarity) {

                            newRank = Math.max(newRank, userContext.getMessageRank().getScore());
                        }

                    }
                }

                float diff = newRank - contextForAdaptation.getMessageRank().getScore();

                if (newRank > 0 && diff <= 0) {

                    adaptationIncrease += diff;

                    contextForAdaptation.getMessageRank().setScore(newRank);
                    adaptationCount++;

                }

            }

        }
    }
}
