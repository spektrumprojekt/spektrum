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
import de.spektrumprojekt.datamodel.message.InteractionLevel;
import de.spektrumprojekt.datamodel.message.MessageRank;
import de.spektrumprojekt.i.datamodel.MessageFeature;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.i.ranker.chain.features.Feature;

/**
 * Command for computing the message rank out of the features
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class ComputeMessageRankCommand implements Command<UserSpecificMessageFeatureContext> {

    private final boolean onlyUseContentMatchFeature;

    private final float nonParticipationFactor;

    public ComputeMessageRankCommand(boolean onlyUseContentMatchFeature,
            float nonParticipationFactor) {
        this.onlyUseContentMatchFeature = onlyUseContentMatchFeature;
        this.nonParticipationFactor = nonParticipationFactor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " onlyUseContentMatchFeature: "
                + onlyUseContentMatchFeature + " nonParticipationFactor: " + nonParticipationFactor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(UserSpecificMessageFeatureContext context) {

        MessageRank messageRank = new MessageRank(context.getMessage().getGlobalId(),
                context.getUserGlobalId());
        context.setMessageRank(messageRank);
        messageRank.setInteractionLevel(context.getInteractionLevel());

        if (context.check(Feature.AUTHOR_FEATURE, 1)) {
            messageRank.setAuthor(true);
        }

        // TODO use two ranks, one indicating how much the user is interested into the content of
        // the message,
        // and one indicating how much the user is interested in the particular message. e.g. the
        // content is
        // of interest for the user if he is author but he is not interested in the message, since
        // he wrote it?
        // => no not yet. assume the author is always interested, specially for discussions?
        boolean isNonParticipatingAnswer = false;
        if (!onlyUseContentMatchFeature) {
            boolean interaction = false;
            if (context.check(Feature.AUTHOR_FEATURE, 1)) {
                messageRank.setRank(1f);
            } else if (context.check(Feature.MENTION_FEATURE, 1)) {
                messageRank.setRank(0.95f);
            } else if (context.check(Feature.LIKE_FEATURE, 1)) {
                messageRank.setRank(0.95f);
            } else if (context.check(Feature.DISCUSSION_PARTICIPATION_FEATURE, 1)) {
                messageRank.setRank(0.9f);
            } else if (context.check(Feature.DISCUSSION_MENTION_FEATURE, 1)) {
                messageRank.setRank(0.8f);
            } else if (!context.check(Feature.DISCUSSION_ROOT_FEATURE, 1)) {
                isNonParticipatingAnswer = true;
            }
            if (interaction && context.getInteractionLevel().equals(InteractionLevel.NONE)) {
                throw new IllegalStateException(
                        "We have an interaction but InteractionLevel is none! context: " + context);
            }
        }
        MessageFeature termMatch = context.getFeature(Feature.CONTENT_MATCH_FEATURE);
        if (termMatch != null && termMatch.getValue() > messageRank.getRank()) {

            float value = termMatch.getValue();
            if (isNonParticipatingAnswer) {
                value = value * nonParticipationFactor;
            }
            messageRank.setRank(value);
        }

    }
}
