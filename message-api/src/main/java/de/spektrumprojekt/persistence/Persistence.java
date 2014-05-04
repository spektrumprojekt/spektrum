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

package de.spektrumprojekt.persistence;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.duplicationdetection.HashWithDate;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageFilter;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.message.Term.TermCategory;
import de.spektrumprojekt.datamodel.message.TermFrequency;
import de.spektrumprojekt.datamodel.message.UserMessageScore;
import de.spektrumprojekt.datamodel.observation.Observation;
import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.datamodel.source.Source;
import de.spektrumprojekt.datamodel.source.SourceNotFoundException;
import de.spektrumprojekt.datamodel.source.SourceStatus;
import de.spektrumprojekt.datamodel.subscription.Subscription;
import de.spektrumprojekt.datamodel.subscription.SubscriptionFilter;
import de.spektrumprojekt.datamodel.subscription.SubscriptionSourceStatus;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.datamodel.user.UserSimilarity;
import de.spektrumprojekt.exceptions.SubscriptionNotFoundException;

/**
 * The main interface for accessing the persistence.
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public interface Persistence {

    public static enum MatchMode {
        EXACT,
        STARTS_WITH,
        ENDS_WITH;

        public boolean matches(String base, String toMatch) {
            switch (this) {
            case EXACT:
                return StringUtils.equals(base, toMatch);
            case ENDS_WITH:
                return base.endsWith(toMatch);
            case STARTS_WITH:
                return base.startsWith(toMatch);
            default:
                return StringUtils.equals(base, toMatch);
            }
        }
    }

    void close();

    Statistics computeStatistics();

    void deleteAggregationSubscription(String subscriptionId);

    void deleteAndCreateUserSimilarities(Collection<UserSimilarity> values);

    void deleteHashWithDates(List<HashWithDate> hashesToDelete);

    /**
     * Delete the source including the source status
     * 
     * @param sourceGlobalId
     */
    void deleteSource(String sourceGlobalId);

    void deleteSubscription(String subscriptionGlobalId);

    Source findSource(String connectorType, Collection<Property> accessParameters);

    List<SourceStatus> findSourceStatusByProperty(Property property);

    Collection<MessageGroup> getAllMessageGroups();

    Collection<Term> getAllTerms();

    /**
     * returns all userModelEntries of this type of usermodel
     * 
     * @param userModel
     * @return
     */
    Map<UserModel, Collection<UserModelEntry>> getAllUserModelEntries(String userModelType);

    Collection<User> getAllUsers();

    List<HashWithDate> getHashsByGlobalSubscriptionId(String subscriptionGlobalId);

    /**
     * 
     * @param messageGlobalId
     *            the message global id
     * @return the message for the id or null if not existing
     */
    Message getMessageByGlobalId(String messageGlobalId);

    /**
     * Get message groups for global id
     * 
     * @param globalIdString
     *            global id
     * @return the message group
     */
    MessageGroup getMessageGroupByGlobalId(String globalIdString);

    MessageGroup getMessageGroupById(Long messageGroupId);

    MessageRelation getMessageRelation(Message message);

    List<Message> getMessages(MessageFilter messageFilter);

    UserMessageScore getMessageScore(String userGlobalId, String messageGlobalId);

    /**
     * Return the user message score that is at the n-th position if only messages are considered
     * that written after firstDate and that belong to the user, all sorted by the score descending.
     * 
     * If the returned score (if any) is used for filtering it will return at maximum n Elements for
     * messages after the given date (and no new message is added in between).
     * 
     * @param userGlobalId
     * @param n
     * @param firstDate
     * @return the user message score at the n-th position fulfilling the constraints.
     */
    UserMessageScore getNthUserMessageScore(String userGlobalId, int n,
            final Date firstDate);

    int getNumberOfSubscriptionsBySourceGlobalId(String globalId);

    Collection<Observation> getObservations(String userGlobalId, String messageGlobalId,
            ObservationType observationType);

    /**
     * Get the term for the name
     * 
     * @param termCategory
     *            the type of term
     * 
     * @param name
     *            the name of the term (be case insensitive)
     * @return the term
     */
    Term getOrCreateTerm(TermCategory termCategory, String name);

    User getOrCreateUser(String userGlobalId);

    /**
     * 
     * @param userGlobalId
     *            the users id
     * @return the user model
     */
    UserModel getOrCreateUserModelByUser(String userGlobalId, String userModelType);

    Source getSourceByGlobalId(String sourceGlobalId) throws SourceNotFoundException;

    SourceStatus getSourceStatusBySourceGlobalId(String sourceGlobalId);

    List<SourceStatus> getSourceStatusList();

    Subscription getSubscriptionByGlobalId(String subscriptionGlobalId)
            throws SubscriptionNotFoundException;

    List<Subscription> getSubscriptions(SubscriptionFilter subscriptionFilter);

    List<SubscriptionSourceStatus> getSubscriptionsWithSourceStatus(
            SubscriptionFilter subscriptionFilter);

    TermFrequency getTermFrequency();

    User getUserByGlobalId(String userGlobalId);

    Collection<UserModelEntry> getUserModelEntries(
            UserModel userModel,
            Collection<String> termsToMatch,
            Collection<Long> messageGroupIdsToConsider,
            MatchMode matchMode,
            boolean useMGFreeTermValue);

    Collection<UserModelEntry> getUserModelEntries(UserModel userModel,
            Collection<String> termsToMatch, MatchMode matchMode);

    Map<String, String> getUserModelEntriesCountDescription();

    /**
     * 
     * @param userModel
     *            the user model
     * @param terms
     *            the terms
     * @return a map of the terms mapped to the entry of the user model. if there is no entry yet,
     *         than it will not be in the map.
     */
    Map<Term, UserModelEntry> getUserModelEntriesForTerms(UserModel userModel,
            Collection<Term> terms);

    Collection<UserSimilarity> getUserSimilarities(String messageGroupGlobalId);

    Collection<UserSimilarity> getUserSimilarities(String userGlobalId, Collection<String> users,
            String messageGroupGlobalId, double userSimilarityThreshold);

    UserSimilarity getUserSimilarity(String userGlobalIdFrom, String userGlobalIdTo,
            String messageGroupGlobalId);

    Collection<UserModel> getUsersWithUserModel(Collection<Term> terms, String userModelType,
            MatchMode matchMode);

    void initialize();

    void removeUserModelEntry(UserModel userModel, UserModelEntry userModelEntry);

    void resetTermCount();

    HashWithDate saveHashWithDate(HashWithDate hashWithDate);

    Source saveSource(Source source);

    SourceStatus saveSourceStatus(SourceStatus sourceStatus);

    /**
     * 
     * @param message
     *            the message to store
     * @return the stored message
     */
    Message storeMessage(Message message);

    /**
     * Stores the message group
     * 
     * @param messageGroup
     *            the message group
     * @return the stored message group
     */
    MessageGroup storeMessageGroup(MessageGroup messageGroup);

    void storeMessagePattern(String pattern, Message message);

    /**
     * Stores the message relation
     * 
     * TODO do we need this ?
     * 
     * @param message
     *            the personalization message
     * @param relatedMessages
     *            the relation
     */

    void storeMessageRelation(Message message, MessageRelation relatedMessages);

    void storeObservation(Observation observation);

    /**
     * Store(create) or update the changed user model
     * 
     * @param userModel
     *            the user model
     * @param changedEntries
     *            the changed entries
     */
    Collection<UserModelEntry> storeOrUpdateUserModelEntries(UserModel userModel,
            Collection<UserModelEntry> changedEntries);

    Subscription storeSubscription(Subscription subscription);

    /**
     * 
     * @param userMessageScores
     *            store the ranks
     */
    void storeUserMessageScores(Collection<UserMessageScore> userMessageScores);

    void storeUserSimilarity(UserSimilarity stat);

    void updateMessageRank(UserMessageScore rankToUpdate);

    Source updateSource(Source source) throws SourceNotFoundException;

    void updateSourceStatus(SourceStatus sourceStatus);

    Subscription updateSubscription(Subscription subscription) throws SubscriptionNotFoundException;

    void updateTermFrequency(TermFrequency termFrequency);

    void updateTerms(Collection<Term> termsChanged);

    void visitAllUserMessageScores(UserMessageScoreVisitor visitor, Date startDate, Date endDate)
            throws Exception;

}
