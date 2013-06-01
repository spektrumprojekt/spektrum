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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.i.datamodel.MessageFeature;
import de.spektrumprojekt.i.ranker.chain.features.Feature;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;
import de.spektrumprojekt.persistence.Persistence;

/**
 * The context for the ranker chain, it contains the message and a set of
 * {@link UserSpecificMessageFeatureContext} holding the user specific features
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class MessageFeatureContext extends FeatureContext {

    private final Message message;

    private final MessageRelation messageRelation;

    private final Collection<InformationExtractionContext> informationExtractionContexts =
            new HashSet<InformationExtractionContext>();
    private final Map<String, UserSpecificMessageFeatureContext> userContexts =
            new HashMap<String, UserSpecificMessageFeatureContext>();

    private transient Map<String, Message> relatedMessage = null;
    private final Persistence persistence;

    private Collection<String> userGlobalIdsToProcess;

    private boolean noRankingOnlyLearning;

    /**
     * 
     * @param persistence
     *            the persistence is used to resolve the message of the relation if necessary
     * @param message
     *            the message
     * @param relation
     *            the relation
     */
    public MessageFeatureContext(Persistence persistence, Message message, MessageRelation relation) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null");
        }
        if (message == null) {
            throw new IllegalArgumentException("message cannot be null");
        }

        this.persistence = persistence;
        this.message = message;
        this.messageRelation = relation;

    }

    /**
     * A {@link InformationExtractionContexts} is the context of the message per message part.
     * 
     * @param informationExtractionContexts
     *            add a context
     */
    public void addInformationExtractionContexts(
            InformationExtractionContext informationExtractionContexts) {
        this.informationExtractionContexts.add(informationExtractionContexts);
    }

    /**
     * A {@link UserSpecificMessageFeatureContext} is the context of the a message per user.
     * 
     * @param userContext
     *            add a context
     */
    public void addUserContext(UserSpecificMessageFeatureContext userContext) {
        this.userContexts.put(userContext.getUserGlobalId(), userContext);
    }

    public Map<Feature, MessageFeature> getFeaturesForUser(String userGlobalId) {

        Map<Feature, MessageFeature> features = Collections.emptyMap();

        for (UserSpecificMessageFeatureContext context : this.getUserContexts()) {
            if (context.getUserGlobalId().equals(userGlobalId)) {
                features = context.getFeatures();
                break;
            }
        }
        return Collections.unmodifiableMap(features);
    }

    /**
     * 
     * @return the contexts of the information extraction
     */
    public Collection<InformationExtractionContext> getInformationExtractionContexts() {
        return informationExtractionContexts;
    }

    /**
     * 
     * @return the message of this context
     */
    public Message getMessage() {
        return message;
    }

    /**
     * 
     * @return the relation
     */
    public MessageRelation getMessageRelation() {
        return messageRelation;
    }

    /**
     * Get the message defines in the relation (mapping message global id to message). on the first
     * call the persistence will be used to load the messages. If the message is not available (e.g.
     * has not been transformed) the value is null.
     * 
     * @return the related messages
     */
    public Map<String, Message> getMessagesOfRelation() {
        if (relatedMessage == null && this.messageRelation != null) {
            resolveMessagesOfMessageRelation();
        }
        return this.relatedMessage;
    }

    /**
     * 
     * @return the persistence
     */
    public Persistence getPersistence() {
        return persistence;
    }

    /**
     * 
     * @param userGlobalId
     *            the user global id to get the context of
     * @return the context (null if not existing)
     */
    public UserSpecificMessageFeatureContext getUserContext(String userGlobalId) {
        return this.userContexts.get(userGlobalId);
    }

    /**
     * The {@link UserSpecificMessageFeatureContext} are the contexts per user and per message
     * (since a rank is computed per message per user).
     * 
     * @return all user contexts
     */
    public Collection<UserSpecificMessageFeatureContext> getUserContexts() {
        return userContexts.values();
    }

    public Collection<String> getUserGlobalIdsToProcess() {
        return userGlobalIdsToProcess;
    }

    public boolean isNoRankingOnlyLearning() {
        return noRankingOnlyLearning;
    }

    /**
     * Resolve the message of the relation
     */
    private synchronized void resolveMessagesOfMessageRelation() {

        if (relatedMessage != null) {
            return;
        }

        relatedMessage = new HashMap<String, Message>();
        if (this.messageRelation.getRelatedMessageGlobalIds() != null) {
            for (String messageGlobalId : this.messageRelation
                    .getRelatedMessageGlobalIds()) {
                Message message;
                if (this.message.getGlobalId().equals(messageGlobalId)) {
                    message = this.message;
                }
                message = persistence.getMessageByGlobalId(messageGlobalId);
                if (message != null) {
                    relatedMessage.put(messageGlobalId, message);
                }
            }
        }
    }

    public void setNoRankingOnlyLearning(boolean noRankingOnlyLearning) {
        this.noRankingOnlyLearning = noRankingOnlyLearning;
    }

    public void setUserGlobalIdsToProcess(Collection<String> userGlobalIdsToProcess) {
        this.userGlobalIdsToProcess = userGlobalIdsToProcess;
    }

    @Override
    public String toString() {
        final int maxLen = 5;
        return "MessageFeatureContext [message="
                + message
                + ", messageRelation="
                + messageRelation
                + ", informationExtractionContexts="
                + (informationExtractionContexts != null ? toString(informationExtractionContexts,
                        maxLen) : null)
                + ", userContexts="
                + (userContexts != null ? toString(userContexts.entrySet(), maxLen) : null)
                + ", persistence="
                + persistence
                + ", userGlobalIdsToProcess="
                + (userGlobalIdsToProcess != null ? toString(userGlobalIdsToProcess, maxLen) : null)
                + ", noRankingOnlyLearning=" + noRankingOnlyLearning + ", getFeatures()="
                + (getFeatures() != null ? toString(getFeatures().entrySet(), maxLen) : null) + "]";
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
