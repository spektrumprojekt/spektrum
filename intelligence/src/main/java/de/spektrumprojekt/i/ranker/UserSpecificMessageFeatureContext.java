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

package de.spektrumprojekt.i.ranker;

import java.util.Map;

import de.spektrumprojekt.datamodel.message.InteractionLevel;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageRank;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.i.datamodel.MessageFeature;
import de.spektrumprojekt.persistence.Persistence;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class UserSpecificMessageFeatureContext extends MessageFeatureContext {

    /**
     * Copy the features of the given context to the returned one
     * 
     * @param copyContext
     *            the context to copy from
     * @param userGlobalId
     *            the global id
     * @return the fresh create context
     */
    public static UserSpecificMessageFeatureContext createAndCopy(
            MessageFeatureContext copyContext, String userGlobalId) {

        UserSpecificMessageFeatureContext context = new UserSpecificMessageFeatureContext(
                copyContext.getPersistence(), userGlobalId, copyContext.getMessage(),
                copyContext.getMessageRelation());
        for (MessageFeature mf : copyContext.getFeatures().values()) {
            context.addMessageFeature(mf);
        }

        return context;
    }

    private InteractionLevel interactionLevel;

    private MessageRank messageRank;

    private final String userGlobalId;

    private Map<Term, UserModelEntry> matchingUserModelEntries;

    /**
     * 
     * @param persistence
     *            the persistence
     * @param userGlobalId
     *            the id of the user for this context
     * @param message
     *            the message of this context
     * @param relation
     *            the relation of this context
     */
    public UserSpecificMessageFeatureContext(Persistence persistence, String userGlobalId,
            Message message,
            MessageRelation relation) {
        super(persistence, message, relation);
        if (userGlobalId == null) {
            throw new IllegalArgumentException("userGlobalId cannot be null");
        }
        this.userGlobalId = userGlobalId;
    }

    public InteractionLevel getInteractionLevel() {
        return interactionLevel;
    }

    /**
     * 
     * @return a map of user model entries. all user model entries have a term that is part of the
     *         message.
     */
    public Map<Term, UserModelEntry> getMatchingUserModelEntries() {
        return matchingUserModelEntries;
    }

    /**
     * 
     * @return the rank (null if not computed or uncomputeable)
     */
    public MessageRank getMessageRank() {
        return messageRank;
    }

    /**
     * 
     * @return the id of the user
     */
    public String getUserGlobalId() {
        return userGlobalId;
    }

    public void setInteractionLevel(InteractionLevel interactionLevel) {
        this.interactionLevel = interactionLevel;
    }

    public void setMatchingUserModelEntries(Map<Term, UserModelEntry> entries) {
        this.matchingUserModelEntries = entries;
    }

    /**
     * 
     * @param messageRank
     *            the message rank
     */
    public void setMessageRank(MessageRank messageRank) {
        this.messageRank = messageRank;
    }
}
