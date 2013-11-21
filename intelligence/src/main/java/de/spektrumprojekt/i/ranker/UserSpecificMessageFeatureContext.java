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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import de.spektrumprojekt.datamodel.message.InteractionLevel;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.message.UserMessageScore;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.i.datamodel.MessageFeature;
import de.spektrumprojekt.i.ranker.feature.Feature;
import de.spektrumprojekt.i.ranker.feature.FeatureAggregate;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class UserSpecificMessageFeatureContext extends FeatureContext {

    private final MessageFeatureContext messageFeatureContext;

    private FeatureAggregate featureAggregate;

    private UserMessageScore messageRank;

    private InteractionLevel interactionLevel;

    private final Collection<UserMessageScore> ranksToUpdate = new HashSet<UserMessageScore>();

    private final String userGlobalId;

    // first key is the user model type
    private Map<String, Map<Term, UserModelEntry>> matchingUserModelEntries;

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
    public UserSpecificMessageFeatureContext(String userGlobalId,
            MessageFeatureContext messageFeatureContext) {
        if (userGlobalId == null) {
            throw new IllegalArgumentException("userGlobalId cannot be null");
        }
        if (messageFeatureContext == null) {
            throw new IllegalArgumentException("messageFeatureContext cannot be null");
        }
        this.messageFeatureContext = messageFeatureContext;
        this.userGlobalId = userGlobalId;

        for (MessageFeature mf : messageFeatureContext.getFeatures().values()) {
            this.addMessageFeature(mf);
        }
    }

    public void addMessageFeature(Feature featureId, float featureValue) {
        MessageFeature messageFeature = new MessageFeature(featureId);
        messageFeature.setMessageGlobalId(this.getMessageFeatureContext().getMessage()
                .getGlobalId());
        messageFeature.setMessageGlobalId(this.userGlobalId);
        messageFeature.setValue(featureValue);

        this.addMessageFeature(messageFeature);
    }

    public void addRankToUpdate(UserMessageScore messageRank) {
        this.ranksToUpdate.add(messageRank);

    }

    public FeatureAggregate getFeatureAggregate() {
        return featureAggregate;
    }

    public InteractionLevel getInteractionLevel() {
        return interactionLevel;
    }

    /**
     * 
     * @return a map of user model types to user model entries. all user model entries have a term
     *         that is part of the message.
     */
    public Map<String, Map<Term, UserModelEntry>> getMatchingUserModelEntries() {
        return matchingUserModelEntries;
    }

    public Message getMessage() {
        return this.messageFeatureContext.getMessage();
    }

    public MessageFeatureContext getMessageFeatureContext() {
        return messageFeatureContext;
    }

    /**
     * 
     * @return the rank (null if not computed or uncomputeable)
     */
    public UserMessageScore getMessageScore() {
        return messageRank;
    }

    public MessageRelation getMessageRelation() {
        return this.messageFeatureContext.getMessageRelation();
    }

    public Collection<UserMessageScore> getRanksToUpdate() {
        return ranksToUpdate;
    }

    /**
     * 
     * @return the id of the user
     */
    public String getUserGlobalId() {
        return userGlobalId;
    }

    public void setFeatureAggregate(FeatureAggregate featureAggregate) {
        this.featureAggregate = featureAggregate;
    }

    public void setInteractionLevel(InteractionLevel interactionLevel) {
        this.interactionLevel = interactionLevel;
    }

    public void setMatchingUserModelEntries(Map<String, Map<Term, UserModelEntry>> entries) {
        this.matchingUserModelEntries = entries;
    }

    /**
     * 
     * @param messageRank
     *            the message rank
     */
    public void setMessageRank(UserMessageScore messageRank) {
        this.messageRank = messageRank;
    }

    @Override
    public String toString() {
        final int maxLen = 5;
        return "UserSpecificMessageFeatureContext [messageRank="
                + messageRank
                + ", interactionLevel="
                + interactionLevel
                + ", ranksToUpdate="
                + (ranksToUpdate != null ? toString(ranksToUpdate, maxLen) : null)
                + ", userGlobalId="
                + userGlobalId
                + ", matchingUserModelEntries="
                + (matchingUserModelEntries != null ? toString(matchingUserModelEntries.entrySet(),
                        maxLen) : null) + ", toString()=" + super.toString() + "]";
    }

    private String toString(Collection<?> collection, int maxLen) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int i = 0;
        for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(iterator.next());
        }
        builder.append("]");
        return builder.toString();
    }
}
