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

import java.util.Map;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.datamodel.message.MessageRelation.MessageRelationType;
import de.spektrumprojekt.i.datamodel.MessageFeature;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;

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
        if (!context.check(Feature.DISCUSSION_ROOT_FEATURE, 1)
                || !context.check(Feature.AUTHOR_FEATURE, 1)) {

            MessageFeature feature = new MessageFeature(getFeatureId());

            MessageRelation relation = context.getMessageRelation();
            if (relation != null
                    && MessageRelationType.DISCUSSION.equals(relation.getMessageRelationType())) {
                Map<String, Message> messages = messageFeatureContext.getMessagesOfRelation();
                for (Message message : messages.values()) {
                    if (context.getMessage().getGlobalId().equals(message.getGlobalId())) {
                        // TODO ignore message itself ?
                        continue;
                    }
                    if (context.getUserGlobalId().equals(message.getAuthorGlobalId())) {
                        // actually we could also count the participation
                        feature.setValue(1f);
                        break;
                    }
                }
            }

            context.addMessageFeature(feature);
        }

    }
}
