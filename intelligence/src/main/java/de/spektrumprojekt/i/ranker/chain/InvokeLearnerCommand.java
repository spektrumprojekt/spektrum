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

import java.util.Collection;
import java.util.HashSet;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.observation.Interest;
import de.spektrumprojekt.datamodel.observation.Observation;
import de.spektrumprojekt.datamodel.observation.ObservationPriority;
import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.datamodel.MessageFeature;
import de.spektrumprojekt.i.learner.LearningMessage;
import de.spektrumprojekt.i.ranker.RankerConfiguration;
import de.spektrumprojekt.i.ranker.RankerConfigurationFlag;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.i.ranker.feature.Feature;
import de.spektrumprojekt.persistence.Persistence;

/**
 * A command that will create learning message based on the ranked message
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class InvokeLearnerCommand implements Command<UserSpecificMessageFeatureContext> {

    private final Communicator communicator;
    private final Persistence persistence;

    private final boolean learnFromParent;
    private final boolean learnFromParents;
    private final boolean learnLowInterest;
    private final boolean learnFromDiscussions;
    private final boolean learnFromEveryMessage;
    private final int minimumCleanTextLength;

    /**
     * 
     * @param persistence
     *            the persistence to use
     */
    public InvokeLearnerCommand(
            Persistence persistence,
            Communicator communicator,
            boolean learnLowInterest,
            boolean learnFromParent,
            boolean learnFromParents,
            boolean learnFromDiscussions,
            boolean learnFromEveryMessage,
            int minimumCleanTextLength) {
        this.learnLowInterest = learnLowInterest;
        this.communicator = communicator;
        this.persistence = persistence;

        this.learnFromParent = learnFromParent;
        this.learnFromParents = learnFromParents;
        this.learnFromDiscussions = learnFromDiscussions;
        this.learnFromEveryMessage = learnFromEveryMessage;
        this.minimumCleanTextLength = minimumCleanTextLength;
    }

    /**
     * 
     * @param persistence
     *            the persistence to use
     */
    public InvokeLearnerCommand(Persistence persistence, Communicator communicator,
            RankerConfiguration rankerConfiguration) {
        this(
                persistence,
                communicator,
                rankerConfiguration.hasFlag(RankerConfigurationFlag.LEARN_NEGATIVE),
                rankerConfiguration
                        .hasFlag(RankerConfigurationFlag.DISCUSSION_PARTICIPATION_LEARN_FROM_PARENT_MESSAGE),
                rankerConfiguration
                        .hasFlag(RankerConfigurationFlag.DISCUSSION_PARTICIPATION_LEARN_FROM_ALL_PARENT_MESSAGES),
                !rankerConfiguration
                        .hasFlag(RankerConfigurationFlag.DO_NOT_LEARN_FROM_DISCUSSION_PARTICIPATION),
                rankerConfiguration
                        .hasFlag(RankerConfigurationFlag.LEARN_FROM_EVERY_MESSAGE),
                rankerConfiguration.getMinimumCleanTextLengthForInvokingLearner());
    }

    private Interest generateInterest(UserSpecificMessageFeatureContext context) {
        Interest value = null;
        MessageFeature cleanedText = context.getFeature(Feature.CLEANED_TEXT_LENGTH_FEATURE);
        if (cleanedText.getValue() < minimumCleanTextLength) {
            return null;
        }
        if (learnFromEveryMessage) {
            value = Interest.EXTREME;
            return value;
        }
        if (context.check(Feature.AUTHOR_FEATURE, 1)) {
            value = Interest.EXTREME;
        } else if (context.check(Feature.MENTION_FEATURE, 1)) {
            value = Interest.HIGH;
        } else if (context.check(Feature.LIKE_FEATURE, 1)) {
            value = Interest.HIGH;
        } else if (learnFromDiscussions
                && context.check(Feature.DISCUSSION_PARTICIPATION_FEATURE, 1)) {
            value = Interest.HIGH;
        } else if (learnFromDiscussions && context.check(Feature.DISCUSSION_MENTION_FEATURE, 1)) {
            value = Interest.HIGH;
        } else if (learnLowInterest && !context.check(Feature.MESSAGE_ROOT_FEATURE, 1)) {
            if (context.getMessageRank().getScore() < 0.5f) {
                value = Interest.NORMAL;
            } else if (context.getMessageRank().getScore() < 0.25f) {
                value = Interest.LOW;
            } else if (context.getMessageRank().getScore() < 0.1f) {
                value = Interest.NONE;
            }
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName()
                + " learnFromParent=" + learnFromParent
                + " learnFromParents=" + learnFromParents
                + " learnLowInterest=" + learnLowInterest
                + " learnFromDiscussions=" + learnFromDiscussions
                + " minimumCleanTextLength=" + minimumCleanTextLength;

    }

    private void learnFromParentMessage(UserSpecificMessageFeatureContext context, Message message,
            Collection<String> messagesLearnedSoFar) {
        Property parentProperty = MessageHelper.getParentMessage(message);
        if (parentProperty != null) {
            String parentMessageGlobalId = parentProperty.getPropertyValue();
            Message parentMessage = this.persistence
                    .getMessageByGlobalId(parentMessageGlobalId);

            if (parentMessage != null) {
                if (messagesLearnedSoFar.contains(parentMessage.getGlobalId())) {
                    throw new IllegalStateException(
                            "Trying to learn again from the same parent message. "
                                    + "Seems to be an invalid message setup. parentMessage="
                                    + parentMessage.getGlobalId()
                                    + " messagesLearnedSoFar="
                                    + messagesLearnedSoFar);
                }
                learnMessage(context,
                        parentMessage,
                        ObservationPriority.SECOND_LEVEL_FEATURE_INFERRED,
                        Interest.HIGH);
                messagesLearnedSoFar.add(parentMessage.getGlobalId());

                if (learnFromParents) {
                    learnFromParentMessage(context, parentMessage, messagesLearnedSoFar);
                }
            }

        }
    }

    private void learnMessage(UserSpecificMessageFeatureContext context, Message message,
            ObservationPriority priority,
            Interest interest) {
        Observation observation = new Observation(
                context.getUserGlobalId(),
                message.getGlobalId(),
                ObservationType.MESSAGE,
                priority,
                null,
                message.getPublicationDate(),
                interest);

        LearningMessage learningMessage = new LearningMessage(observation,
                context.getMessageRelation());

        communicator.sendMessage(learningMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(UserSpecificMessageFeatureContext context) {

        Interest interest = generateInterest(context);

        Message message = context.getMessage();
        if (interest != null) {

            learnMessage(context,
                    message,
                    ObservationPriority.FIRST_LEVEL_FEATURE_INFERRED,
                    interest);

        }

        if (context.check(Feature.DISCUSSION_PARTICIPATION_FEATURE, 1)) {

            if (learnFromParent) {
                Collection<String> messagesLearnedSoFar = new HashSet<String>();
                messagesLearnedSoFar.add(message.getGlobalId());
                learnFromParentMessage(context, message, messagesLearnedSoFar);
            }
        }

    }
}
