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
import java.util.List;
import java.util.Map;

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
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub

    }

    @Override
    public SubscriptionStatus getAggregationSubscription(String subscriptionId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SubscriptionStatus> getAggregationSubscriptions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<MessageGroup> getAllMessageGroups() {
        return null;
    }

    @Override
    public Collection<User> getAllUsers() {
        return null;
    }

    @Override
    public List<HashWithDate> getHashsByGlobalSubscriptionId(String subscriptionGlobalId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Message getMessageByGlobalId(String messageGlobalId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MessageGroup getMessageGroupByGlobalId(String globalIdString) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MessageRank getMessageRank(String userGlobalId, String messageGlobalId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Message> getMessagesSince(String topicId, Date fromDate) {
        return null;
    }

    @Override
    public Term getOrCreateTerm(TermCategory termCategory, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public User getOrCreateUser(String userGlobalId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UserModel getOrCreateUserModelByUser(String userGlobalId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Term, UserModelEntry> getUserModelEntriesForTerms(UserModel userModel,
            Collection<Term> terms) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<UserSimilarity> getUserSimilarities(String userGlobalId,
            Collection<String> users, String messageGroupGlobalId, double userSimilarityThreshold) {
        return null;
    }

    @Override
    public Collection<UserModel> getUsersWithUserModel(Collection<Term> arrayList) {
        return null;
    }

    @Override
    public void initialize() {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeUserModelEntry(UserModel userModel, UserModelEntry userModelEntry) {
        // TODO Auto-generated method stub

    }

    @Override
    public SubscriptionStatus saveAggregationSubscription(SubscriptionStatus aggregationSubscription) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HashWithDate saveHashWithDate(HashWithDate hashWithDate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Message storeMessage(Message message) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MessageGroup storeMessageGroup(MessageGroup messageGroup) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void storeMessageRanks(Collection<MessageRank> ranks) {
        // TODO Auto-generated method stub

    }

    @Override
    public void storeMessageRelation(Message message, MessageRelation relatedMessages) {
        // TODO Auto-generated method stub

    }

    @Override
    public Collection<UserModelEntry> storeOrUpdateUserModelEntries(UserModel userModel,
            Collection<UserModelEntry> changedEntries) {
        return null;
    }

    @Override
    public void updateAggregationSubscription(SubscriptionStatus aggregationStatus) {

    }

}