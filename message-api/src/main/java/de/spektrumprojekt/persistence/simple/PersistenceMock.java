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

package de.spektrumprojekt.persistence.simple;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.duplicationdetection.HashWithDate;
import de.spektrumprojekt.datamodel.message.InteractionLevel;
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
import de.spektrumprojekt.datamodel.source.SourceStatus;
import de.spektrumprojekt.datamodel.subscription.Subscription;
import de.spektrumprojekt.datamodel.subscription.SubscriptionFilter;
import de.spektrumprojekt.datamodel.subscription.SubscriptionSourceStatus;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.datamodel.user.UserSimilarity;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.Statistics;
import de.spektrumprojekt.persistence.UserMessageScoreVisitor;

/**
 * The Dummy Persistence is for test purpose only. It just does nothing and returns null.
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class PersistenceMock implements Persistence {

    @Override
    public void close() {
    }

    @Override
    public Statistics computeStatistics() {
        return null;
    }

    @Override
    public void deleteAggregationSubscription(String subscriptionId) {
    }

    @Override
    public void deleteAndCreateUserSimilarities(Collection<UserSimilarity> values) {
    }

    @Override
    public void deleteHashWithDates(List<HashWithDate> hashesToDelete) {
    }

    @Override
    public void deleteSource(String sourceGlobalId) {
    }

    @Override
    public void deleteSubscription(String subscriptionGlobalId) {

    }

    @Override
    public Source findSource(String connectorType, Collection<Property> accessParameters) {
        return null;
    }

    @Override
    public List<SourceStatus> findSourceStatusByProperty(Property property) {
        return null;
    }

    @Override
    public Collection<MessageGroup> getAllMessageGroups() {
        return null;
    }

    @Override
    public Collection<Term> getAllTerms() {
        return null;
    }

    @Override
    public Map<UserModel, Collection<UserModelEntry>> getAllUserModelEntries(String userModelType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<User> getAllUsers() {
        return null;
    }

    @Override
    public List<HashWithDate> getHashsByGlobalSubscriptionId(String subscriptionGlobalId) {
        return null;
    }

    @Override
    public Message getMessageByGlobalId(String messageGlobalId) {
        return null;
    }

    @Override
    public MessageGroup getMessageGroupByGlobalId(String globalIdString) {
        return null;
    }

    @Override
    public MessageGroup getMessageGroupById(Long messageGroupId) {
        return null;
    }

    @Override
    public MessageRelation getMessageRelation(Message message) {
        return null;
    }

    @Override
    public List<Message> getMessages(MessageFilter messageFilter) {
        return null;
    }

    @Override
    public UserMessageScore getMessageScore(String userGlobalId, String messageGlobalId) {
        return null;
    }

    @Override
    public UserMessageScore getNthUserMessageScore(String userGlobalId, int n, Date firstDate,
            InteractionLevel[] interactionLevels) {
        return null;
    }

    @Override
    public int getNumberOfSubscriptionsBySourceGlobalId(String globalId) {

        return 0;
    }

    @Override
    public Collection<Observation> getObservations(String userGlobalId, String messageGlobalId,
            ObservationType observationType) {
        return null;
    }

    @Override
    public Term getOrCreateTerm(TermCategory termCategory, String name) {
        return null;
    }

    @Override
    public User getOrCreateUser(String userGlobalId) {
        return null;
    }

    @Override
    public UserModel getOrCreateUserModelByUser(String userGlobalId, String userModelType) {
        return null;
    }

    @Override
    public Source getSourceByGlobalId(String sourceGlobalId) {
        return null;
    }

    @Override
    public SourceStatus getSourceStatusBySourceGlobalId(String subscriptionId) {
        return null;
    }

    @Override
    public List<SourceStatus> getSourceStatusList() {
        return null;
    }

    @Override
    public Subscription getSubscriptionByGlobalId(String subscriptionGlobalId) {

        return null;
    }

    @Override
    public List<Subscription> getSubscriptions(SubscriptionFilter subscriptionFilter) {
        return null;
    }

    @Override
    public List<SubscriptionSourceStatus> getSubscriptionsWithSourceStatus(
            SubscriptionFilter subscriptionFilter) {
        return null;
    }

    @Override
    public TermFrequency getTermFrequency() {
        return null;
    }

    @Override
    public User getUserByGlobalId(String userGlobalId) {

        return null;
    }

    @Override
    public Collection<UserModelEntry> getUserModelEntries(UserModel userModel,
            Collection<String> termsToMatch, Collection<Long> messageGroupIdsToConsider,
            MatchMode matchMode, boolean useMGFreeTermValue) {
        return null;
    }

    @Override
    public Collection<UserModelEntry> getUserModelEntries(UserModel userModel,
            Collection<String> match, MatchMode endsWith) {

        return null;
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
        return null;
    }

    @Override
    public Collection<UserSimilarity> getUserSimilarities(String messageGroupGlobalId) {
        return null;
    }

    @Override
    public Collection<UserSimilarity> getUserSimilarities(String userGlobalId,
            Collection<String> users, String messageGroupGlobalId, double userSimilarityThreshold) {
        return null;
    }

    @Override
    public UserSimilarity getUserSimilarity(String userGlobalIdFrom, String userGlobalIdTo,
            String messageGroupGlobalId) {
        return null;
    }

    @Override
    public Collection<UserModel> getUsersWithUserModel(Collection<Term> arrayList,
            String userModelType, MatchMode matchMode) {
        return null;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void removeUserModelEntry(UserModel userModel, UserModelEntry userModelEntry) {
    }

    @Override
    public void resetTermCount() {

    }

    @Override
    public HashWithDate saveHashWithDate(HashWithDate hashWithDate) {
        return null;
    }

    @Override
    public Source saveSource(Source source) {
        return null;
    }

    @Override
    public SourceStatus saveSourceStatus(SourceStatus aggregationSubscription) {
        return null;
    }

    @Override
    public Message storeMessage(Message message) {
        return null;
    }

    @Override
    public MessageGroup storeMessageGroup(MessageGroup messageGroup) {
        return null;
    }

    @Override
    public void storeMessagePattern(String pattern, Message message) {
    }

    @Override
    public void storeMessageRelation(Message message, MessageRelation relatedMessages) {
    }

    @Override
    public void storeObservation(Observation observation) {
    }

    @Override
    public Collection<UserModelEntry> storeOrUpdateUserModelEntries(UserModel userModel,
            Collection<UserModelEntry> changedEntries) {
        return null;
    }

    @Override
    public Subscription storeSubscription(Subscription subscription) {
        return null;
    }

    @Override
    public void storeUserMessageScores(Collection<UserMessageScore> ranks) {
    }

    @Override
    public void storeUserSimilarity(UserSimilarity stat) {

    }

    @Override
    public void updateMessageRank(UserMessageScore rankToUpdate) {

    }

    @Override
    public Source updateSource(Source source) {
        return null;
    }

    @Override
    public void updateSourceStatus(SourceStatus aggregationStatus) {
    }

    @Override
    public Subscription updateSubscription(Subscription subscription) {
        return null;
    }

    @Override
    public void updateTermFrequency(TermFrequency termFrequency) {

    }

    @Override
    public void updateTerms(Collection<Term> termsChanged) {
    }

    @Override
    public void visitAllUserMessageScores(UserMessageScoreVisitor visitor, Date startDate,
            Date endDate)
            throws Exception {
    }

}
