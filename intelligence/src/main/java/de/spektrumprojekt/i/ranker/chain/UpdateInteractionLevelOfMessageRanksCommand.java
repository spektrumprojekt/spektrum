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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.InteractionLevel;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageRank;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.i.ranker.chain.features.Feature;
import de.spektrumprojekt.persistence.Persistence;

/**
 * A command to store the message ranks
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class UpdateInteractionLevelOfMessageRanksCommand implements
        Command<UserSpecificMessageFeatureContext> {

    private final Persistence persistence;

    /**
     * 
     * @param persistence
     *            the persistence to use
     */
    public UpdateInteractionLevelOfMessageRanksCommand(Persistence persistence) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null");
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
     * 
     * @param message
     *            the message to get the parents of.
     * @return the message that are in the direct reply structure of the message. the root message
     *         will be first one. the message itself is not included
     */
    private List<Message> getParentMessages(Message message) {
        List<Message> parents = new ArrayList<Message>();
        Message parentMessage = message;
        while (parentMessage != null) {
            Property parentProp = MessageHelper.getParentMessage(parentMessage);
            if (parentProp != null) {
                String parentMessageId = parentProp.getPropertyValue();
                parentMessage = this.persistence.getMessageByGlobalId(parentMessageId);
                if (parentMessage != null) {
                    if (parents.contains(parentMessage)) {
                        throw new IllegalStateException(parentMessageId
                                + " is already in parents list. " + parents);
                    }
                    parents.add(parentMessage);
                }
            } else {
                parentMessage = null;
            }
        }
        Collections.reverse(parents);
        return parents;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(UserSpecificMessageFeatureContext context) {
        if (context.isNoRankingOnlyLearning()) {
            return;
        }

        List<Message> parentMessages = getParentMessages(context.getMessage());

        if (context.check(Feature.AUTHOR_FEATURE, 1)) {

            for (Message message : parentMessages) {
                MessageRank parentRank = this.persistence.getMessageRank(
                        context.getUserGlobalId(), message.getGlobalId());
                if (parentRank != null) {
                    parentRank.setInteractionLevel(InteractionLevel.DIRECT);
                    context.addRankToUpdate(parentRank);
                }
            }
        }

    }
}
