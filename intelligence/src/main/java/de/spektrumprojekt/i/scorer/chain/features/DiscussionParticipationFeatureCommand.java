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

package de.spektrumprojekt.i.scorer.chain.features;

import java.util.Map;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.datamodel.message.MessageRelation.MessageRelationType;
import de.spektrumprojekt.i.datamodel.MessageFeature;
import de.spektrumprojekt.i.scorer.MessageFeatureContext;
import de.spektrumprojekt.i.scorer.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.i.scorer.feature.Feature;

/**
 * Computes if the user participated in the discussion of the message
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class DiscussionParticipationFeatureCommand implements
        Command<UserSpecificMessageFeatureContext> {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    /**
     * 
     * @return the feature id
     */
    public Feature getFeatureId() {
        return Feature.DISCUSSION_PARTICIPATION_FEATURE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(UserSpecificMessageFeatureContext context) {

        MessageFeatureContext messageFeatureContext = context.getMessageFeatureContext();
        // dont evaluate if it is a root message or the user is the author
        if (!context.check(Feature.MESSAGE_ROOT_FEATURE, 1)
                || !context.check(Feature.AUTHOR_FEATURE, 1)) {

            MessageFeature partFeature = new MessageFeature(getFeatureId());
            MessageFeature noPartFeature = new MessageFeature(
                    Feature.DISCUSSION_NO_PARTICIPATION_FEATURE);
            noPartFeature.setValue(1f);

            MessageRelation relation = context.getMessageRelation();
            if (relation != null
                    && MessageRelationType.DISCUSSION.equals(relation.getMessageRelationType())) {
                Map<String, Message> messages = messageFeatureContext.getMessagesOfRelation();
                for (Message message : messages.values()) {
                    if (context.getMessage().getGlobalId().equals(message.getGlobalId())) {
                        // ignore message itself ?
                        continue;
                    }
                    if (context.getUserGlobalId().equals(message.getAuthorGlobalId())) {
                        // actually we could also count the participation
                        partFeature.setValue(1f);
                        noPartFeature.setValue(0f);
                        break;
                    }
                }
            }

            context.addMessageFeature(partFeature);
            context.addMessageFeature(noPartFeature);
        }

    }
}
