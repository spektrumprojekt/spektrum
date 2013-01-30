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

package de.spektrumprojekt.persistence.jpa;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.spektrumprojekt.configuration.Configuration;
import de.spektrumprojekt.datamodel.duplicationdetection.HashWithDate;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.MessageRank;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.message.Term.TermCategory;
import de.spektrumprojekt.datamodel.subscription.SubscriptionStatus;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.datamodel.user.UserSimilarity;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.Statistics;
import de.spektrumprojekt.persistence.jpa.impl.AggregationSubscriptionPersistence;
import de.spektrumprojekt.persistence.jpa.impl.DuplicationDetectionPersistence;
import de.spektrumprojekt.persistence.jpa.impl.MessagePersistence;
import de.spektrumprojekt.persistence.jpa.impl.UserPersistence;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class JPAPersistence implements Persistence {

    private AggregationSubscriptionPersistence aggregationSubscriptionPersistence;
    private UserPersistence userPersistence;
    private MessagePersistence messagePersistence;
    private DuplicationDetectionPersistence duplicationDetectionPersistence;

    private JPAConfiguration jpaConfiguration;

    public JPAPersistence(Configuration configuration) {
        this(new JPAConfiguration(configuration));
    }

    public JPAPersistence(JPAConfiguration jpaConfiguration) {
        this.jpaConfiguration = jpaConfiguration;
    }

    @Override
    public void close() {
        if (this.aggregationSubscriptionPersistence != null) {
            this.aggregationSubscriptionPersistence.shutdown();
        }
    }

    @Override
    public Statistics computeStatistics() {
        return this.messagePersistence.computeStatistics();
    }

    @Override
    public void deleteAggregationSubscription(String subscriptionId) {
        aggregationSubscriptionPersistence.deleteAggregationSubscription(subscriptionId);

    }

    @Override
    public void deleteAndCreateUserSimilarities(Collection<UserSimilarity> userSimilarities) {
        this.userPersistence.deleteAndCreateUserSimilarities(userSimilarities);
    }

    @Override
    public void deleteHashWithDates(List<HashWithDate> hashesToDelete) {
        duplicationDetectionPersistence.deleteHashWithDates(hashesToDelete);
    }

    @Override
    public SubscriptionStatus getAggregationSubscription(String subscriptionId) {
        return aggregationSubscriptionPersistence.getAggregationSubscription(subscriptionId);
    }

    @Override
    public List<SubscriptionStatus> getAggregationSubscriptions() {
        return aggregationSubscriptionPersistence.getAggregationSubscriptions();
    }

    @Override
    public Collection<MessageGroup> getAllMessageGroups() {
        return this.messagePersistence.getAllMessageGroups();
    }

    @Override
    public Collection<User> getAllUsers() {
        return this.userPersistence.getAllUsers();
    }

    @Override
    public List<HashWithDate> getHashsByGlobalSubscriptionId(String subscriptionGlobalId) {

        return duplicationDetectionPersistence.getHashsByGlobalSubscriptionId(subscriptionGlobalId);
    }

    @Override
    public Message getMessageByGlobalId(String messageGlobalId) {
        return messagePersistence.getMessageByGlobalId(messageGlobalId);
    }

    @Override
    public MessageGroup getMessageGroupByGlobalId(String messageGroupGlobalId) {
        return messagePersistence.getMessageGroupByGlobalId(messageGroupGlobalId);
    }

    @Override
    public MessageRank getMessageRank(String userGlobalId, String messageGlobalId) {
        return this.messagePersistence.getMessageRank(userGlobalId, messageGlobalId);
    }

    @Override
    public Collection<Message> getMessagesSince(Date fromDate) {
        return this.messagePersistence.getMessagesSince(fromDate);
    }

    @Override
    public List<Message> getMessagesSince(String topicId, Date fromDate) {
        return this.messagePersistence.getMessagesSince(topicId, fromDate);
    }

    @Override
    public Term getOrCreateTerm(TermCategory termCategory, String name) {
        return this.messagePersistence.getOrCreateTerm(termCategory, name);
    }

    @Override
    public User getOrCreateUser(String userGlobalId) {
        return this.userPersistence.getOrCreateUser(userGlobalId);
    }

    @Override
    public UserModel getOrCreateUserModelByUser(String userGlobalId) {
        return this.userPersistence.getOrCreateUserModelByUser(userGlobalId);
    }

    @Override
    public Map<Term, UserModelEntry> getUserModelEntriesForTerms(UserModel userModel,
            Collection<Term> terms) {
        return userPersistence.getUserModelEntriesForTerms(userModel, terms);
    }

    @Override
    public Collection<UserSimilarity> getUserSimilarities(String userGlobalId,
            Collection<String> users, String messageGroupGlobalId, double userSimilarityThreshold) {
        return this.userPersistence.getUserSimilarities(userGlobalId, users, messageGroupGlobalId,
                userSimilarityThreshold);
    }

    @Override
    public Collection<UserModel> getUsersWithUserModel(Collection<Term> terms) {
        return this.userPersistence.getUsersWithUserModel(terms);
    }

    @Override
    public void initialize() {
        this.aggregationSubscriptionPersistence = new AggregationSubscriptionPersistence(
                jpaConfiguration);
        this.userPersistence = new UserPersistence(jpaConfiguration);
        this.messagePersistence = new MessagePersistence(jpaConfiguration);
        this.duplicationDetectionPersistence = new DuplicationDetectionPersistence(jpaConfiguration);
    }

    @Override
    public void removeUserModelEntry(UserModel userModel, UserModelEntry userModelEntry) {
        this.userPersistence.removeUserModelEntry(userModel, userModelEntry);
    }

    @Override
    public void resetTermCount() {
        this.messagePersistence.resetTermCount();
    }

    @Override
    public SubscriptionStatus saveAggregationSubscription(SubscriptionStatus aggregationSubscription) {
        return aggregationSubscriptionPersistence
                .saveAggregationSubscription(aggregationSubscription);
    }

    @Override
    public HashWithDate saveHashWithDate(HashWithDate hashWithDate) {
        return duplicationDetectionPersistence.saveHashWithDate(hashWithDate);
    }

    @Override
    public Message storeMessage(Message message) {
        return messagePersistence.storeMessage(message);
    }

    @Override
    public MessageGroup storeMessageGroup(MessageGroup messageGroup) {
        return messagePersistence.storeMessageGroup(messageGroup);
    }

    @Override
    public void storeMessageRanks(Collection<MessageRank> ranks) {
        messagePersistence.storeMessageRanks(ranks);
    }

    @Override
    public void storeMessageRelation(Message message,
            MessageRelation relatedMessages) {
        messagePersistence.storeMessageRelation(message, relatedMessages);
    }

    @Override
    public Collection<UserModelEntry> storeOrUpdateUserModelEntries(UserModel userModel,
            Collection<UserModelEntry> changedEntries) {
        return userPersistence.storeOrUpdateUserModelEntries(userModel, changedEntries);
    }

    @Override
    public void updateAggregationSubscription(SubscriptionStatus aggregationStatus) {
        aggregationSubscriptionPersistence.updateAggregationSubscription(aggregationStatus);
    }

    @Override
    public void updateTerms(Collection<Term> termsChanged) {
        this.messagePersistence.updateTerms(termsChanged);
    }

    @Override
    public void storeMessagePattern(String pattern, Message message) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Collection<Message> getMessagesForPattern(String pattern) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

}
