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
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.i.learner.Interest;
import de.spektrumprojekt.i.learner.LearningMessage;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.i.ranker.chain.features.Feature;

/**
 * A command that will create learning message based on the ranked message
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class InvokeLearnerCommand implements Command<UserSpecificMessageFeatureContext> {

    private final Communicator communicator;

    private final boolean learnLowInterest;

    /**
     * 
     * @param persistence
     *            the persistence to use
     */
    public InvokeLearnerCommand(Communicator communicator, boolean learnLowInterest) {
        this.learnLowInterest = learnLowInterest;
        this.communicator = communicator;
    }

    private Interest generateInterest(UserSpecificMessageFeatureContext context) {
        Interest value = null;
        if (context.check(Feature.AUTHOR_FEATURE, 1)) {
            value = Interest.EXTREME;
        } else if (context.check(Feature.MENTION_FEATURE, 1)) {
            value = Interest.HIGH;
        } else if (context.check(Feature.DISCUSSION_PARTICIPATION_FEATURE, 1)) {
            value = Interest.HIGH;
        } else if (context.check(Feature.DISCUSSION_MENTION_FEATURE, 1)) {
            value = Interest.HIGH;
        } else if (learnLowInterest && !context.check(Feature.DISCUSSION_ROOT_FEATURE, 1)) {
            value = Interest.LOW;
        }
        return value;
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

        Interest interest = generateInterest(context);

        if (interest != null) {
            LearningMessage learningMessage = new LearningMessage(context.getMessage(),
                    context.getUserGlobalId(), interest);

            // this.learner.deliverMessage(learningMessage);
            communicator.sendMessage(learningMessage);

        }

    }
}
