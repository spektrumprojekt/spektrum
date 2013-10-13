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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.spektrumprojekt.configuration.Configuration;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.duplicationdetection.HashWithDate;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageFilter;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.MessageRank;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.message.Term.TermCategory;
import de.spektrumprojekt.datamodel.message.TermFrequency;
import de.spektrumprojekt.datamodel.observation.Observation;
import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.datamodel.source.Source;
import de.spektrumprojekt.datamodel.source.SourceStatus;
import de.spektrumprojekt.datamodel.subscription.Subscription;
import de.spektrumprojekt.datamodel.subscription.SubscriptionFilter;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.datamodel.user.UserSimilarity;
import de.spektrumprojekt.exceptions.SubscriptionNotFoundException;
import de.spektrumprojekt.persistence.MessageRankVisitor;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.Statistics;
import de.spektrumprojekt.persistence.jpa.impl.DuplicationDetectionPersistence;
import de.spektrumprojekt.persistence.jpa.impl.MessagePersistence;
import de.spektrumprojekt.persistence.jpa.impl.SourcePersistence;
import de.spektrumprojekt.persistence.jpa.impl.SourceStatusPersistence;
import de.spektrumprojekt.persistence.jpa.impl.SubscriptionPersistence;
import de.spektrumprojekt.persistence.jpa.impl.UserPersistence;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class JPAPersistence implements Persistence {

    private DuplicationDetectionPersistence duplicationDetectionPersistence;
    private MessagePersistence messagePersistence;
    private SourcePersistence sourcePersistence;
    private SourceStatusPersistence sourceStatusPersistence;
    private SubscriptionPersistence subscriptionPersistence;
    private UserPersistence userPersistence;

    private final JPAConfiguration jpaConfiguration;

    public JPAPersistence(Configuration configuration) {
        this(new JPAConfiguration(configuration));
    }

    public JPAPersistence(JPAConfiguration jpaConfiguration) {
        this.jpaConfiguration = jpaConfiguration;
    }

    @Override
    public void close() {
        if (this.duplicationDetectionPersistence != null) {
            this.duplicationDetectionPersistence.shutdown();
        }
        if (this.messagePersistence != null) {
            this.messagePersistence.shutdown();
        }
        if (this.sourceStatusPersistence != null) {
            this.sourceStatusPersistence.shutdown();
        }
        if (this.sourcePersistence != null) {
            this.sourcePersistence.shutdown();
        }
        if (this.subscriptionPersistence != null) {
            this.subscriptionPersistence.shutdown();
        }
        if (this.userPersistence != null) {
            this.userPersistence.shutdown();
        }

    }

    @Override
    public Statistics computeStatistics() {
        return this.messagePersistence.computeStatistics();
    }

    @Override
    public void deleteAggregationSubscription(String subscriptionId) {
        sourceStatusPersistence.deleteSourceStatus(subscriptionId);

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
    public void deleteSource(String sourceGlobalId) {
        this.sourcePersistence.deleteSource(sourceGlobalId);
    }

    @Override
    public void deleteSubscription(String subscriptionGlobalId) {

        this.subscriptionPersistence.deleteSubscription(subscriptionGlobalId);
    }

    @Override
    public Source findSource(String connectorType, Collection<Property> accessParameters) {
        return this.sourcePersistence.findSource(connectorType, accessParameters);
    }

    @Override
    public Collection<MessageGroup> getAllMessageGroups() {
        return this.messagePersistence.getAllMessageGroups();
    }

    @Override
    public Collection<Term> getAllTerms() {
        return this.messagePersistence.getAllTerms();
    }

    @Override
    public Map<UserModel, Collection<UserModelEntry>> getAllUserModelEntries(String userModelType) {
        // TODO Auto-generated method stub
        throw new RuntimeException("Not implemented yet.");
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
    public MessageRelation getMessageRelation(Message message) {
        return messagePersistence.getMessageRelation(message);
    }

    @Override
    public List<Message> getMessages(MessageFilter messageFilter) {
        return this.messagePersistence.getMessages(messageFilter);
    }

    @Override
    public int getNumberOfSubscriptionsBySourceGlobalId(String sourceGlobalId) {
        return this.subscriptionPersistence
                .getNumberOfSubscriptionsBySourceGlobalId(sourceGlobalId);
    }

    @Override
    public Collection<Observation> getObservations(String userGlobalId, String messageGlobalId,
            ObservationType observationType) {
        return this.messagePersistence.getObservations(userGlobalId, messageGlobalId,
                observationType);
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
    public UserModel getOrCreateUserModelByUser(String userGlobalId, String userModelType) {
        return this.userPersistence.getOrCreateUserModelByUser(userGlobalId, userModelType);
    }

    @Override
    public Source getSourceByGlobalId(String sourceGlobalId) {
        return this.sourcePersistence.getSourceByGlobalId(sourceGlobalId);
    }

    @Override
    public SourceStatus getSourceStatusBySourceGlobalId(String subscriptionId) {
        return sourceStatusPersistence.getSourceStatusBySourceGlobalId(subscriptionId);
    }

    @Override
    public List<SourceStatus> getSourceStatusList() {
        return sourceStatusPersistence.getSourceStatusList();
    }

    @Override
    public Subscription getSubscriptionByGlobalId(String subscriptionGlobalId)
            throws SubscriptionNotFoundException {
        return this.subscriptionPersistence.getSubscriptionByGlobalId(subscriptionGlobalId);
    }

    @Override
    public List<Subscription> getSubscriptions(SubscriptionFilter subscriptionFilter) {
        return this.subscriptionPersistence.getSubscriptions(subscriptionFilter);
    }

    @Override
    public TermFrequency getTermFrequency() {
        return this.messagePersistence.getTermFrequency();
    }

    @Override
    public Map<String, String> getUserModelEntriesCountDescription() {
        Map<String, String> countDesc = new HashMap<String, String>();
        countDesc.put("N/A", "N/A");
        return countDesc;
    }

    @Override
    public Map<Term, UserModelEntry> getUserModelEntriesForTerms(UserModel userModel,
            Collection<Term> terms) {
        return userPersistence.getUserModelEntriesForTerms(userModel, terms);
    }

    @Override
    public Collection<UserSimilarity> getUserSimilarities(String messageGroupGlobalId) {
        return this.userPersistence.getUserSimilarities(messageGroupGlobalId);
    }

    @Override
    public Collection<UserSimilarity> getUserSimilarities(String userGlobalId,
            Collection<String> users, String messageGroupGlobalId, double userSimilarityThreshold) {
        return this.userPersistence.getUserSimilarities(userGlobalId, users, messageGroupGlobalId,
                userSimilarityThreshold);
    }

    @Override
    public UserSimilarity getUserSimilarity(String userGlobalIdFrom, String userGlobalIdTo,
            String messageGroupGlobalId) {

        return this.userPersistence.getUserSimilarity(userGlobalIdFrom, userGlobalIdTo,
                messageGroupGlobalId);
    }

    @Override
    public Collection<UserModel> getUsersWithUserModel(Collection<Term> terms, String userModelType) {
        return this.userPersistence.getUsersWithUserModel(terms, userModelType);
    }

    @Override
    public void initialize() {
        this.duplicationDetectionPersistence = new DuplicationDetectionPersistence(jpaConfiguration);
        this.messagePersistence = new MessagePersistence(jpaConfiguration);
        this.sourcePersistence = new SourcePersistence(
                jpaConfiguration);
        this.sourceStatusPersistence = new SourceStatusPersistence(
                jpaConfiguration);
        this.subscriptionPersistence = new SubscriptionPersistence(
                jpaConfiguration);
        this.userPersistence = new UserPersistence(jpaConfiguration);

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
    public HashWithDate saveHashWithDate(HashWithDate hashWithDate) {
        return duplicationDetectionPersistence.saveHashWithDate(hashWithDate);
    }

    @Override
    public Source saveSource(Source source) {
        return this.sourcePersistence.saveSource(source);
    }

    @Override
    public SourceStatus saveSourceStatus(SourceStatus aggregationSubscription) {
        return sourceStatusPersistence
                .storeSourceStatus(aggregationSubscription);
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
    public void storeMessagePattern(String pattern, Message message) {
        messagePersistence.storeMessagePattern(pattern, message);
    }

    @Override
    public void storeMessageRanks(Collection<MessageRank> ranks) {
        messagePersistence.storeMessageRanks(ranks);
    }

    @Override
    public void storeMessageRelation(Message message, MessageRelation relatedMessages) {
        messagePersistence.storeMessageRelation(message, relatedMessages);
    }

    @Override
    public void storeObservation(Observation observation) {
        this.messagePersistence.storeObservation(observation);
    }

    @Override
    public Collection<UserModelEntry> storeOrUpdateUserModelEntries(UserModel userModel,
            Collection<UserModelEntry> changedEntries) {
        return userPersistence.storeOrUpdateUserModelEntries(userModel, changedEntries);
    }

    @Override
    public Subscription storeSubscription(Subscription subscription) {

        return this.subscriptionPersistence.storeSubscription(subscription);
    }

    @Override
    public void storeUserSimilarity(UserSimilarity stat) {
        this.userPersistence.storeUserSimilarity(stat);
    }

    @Override
    public void updateMessageRank(MessageRank rankToUpdate) {
        this.messagePersistence.updateMessageRank(rankToUpdate);

    }

    @Override
    public Source updateSource(Source source) {
        return this.sourcePersistence.updateSource(source);
    }

    @Override
    public void updateSourceStatus(SourceStatus aggregationStatus) {
        sourceStatusPersistence.updateSourceStatus(aggregationStatus);
    }

    @Override
    public Subscription updateSubscription(Subscription subscription)
            throws SubscriptionNotFoundException {
        return this.subscriptionPersistence.updateSubscription(subscription);
    }

    @Override
    public void updateTermFrequency(TermFrequency termFrequency) {
        this.messagePersistence.updateTermFrequency(termFrequency);
    }

    @Override
    public void updateTerms(Collection<Term> termsChanged) {
        this.messagePersistence.updateTerms(termsChanged);
    }

    @Override
    public void visitAllMessageRanks(MessageRankVisitor visitor, Date startDate, Date endDate) {
        this.messagePersistence.visitAllMessageRanks(visitor, startDate, endDate);

    }

}
