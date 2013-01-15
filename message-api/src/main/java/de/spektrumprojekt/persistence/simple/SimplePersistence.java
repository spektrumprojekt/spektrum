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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
 * This persistence maintains the data completly in memory and may (not yet implemented) load and
 * store it into seralizable files.
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class SimplePersistence implements Persistence {

    private IdGenerator idGenerator;

    private final Map<String, User> users = new HashMap<String, User>();

    private final Map<User, UserModelHolder> userModelHolders = new HashMap<User, UserModelHolder>();

    private final Map<String, Message> messages = new HashMap<String, Message>();

    // key is global id of message
    private final Map<String, MessageRelation> messageRelations = new HashMap<String, MessageRelation>();

    // key is global id of message group
    private final Map<String, MessageGroup> messageGroups = new HashMap<String, MessageGroup>();

    private final Map<UserMessageIdentifier, MessageRank> messageRanks = new HashMap<UserMessageIdentifier, MessageRank>();

    private final Map<String, Term> termsTerms = new HashMap<String, Term>();

    private final Map<String, Term> keyPhraseTerms = new HashMap<String, Term>();

    private final Collection<UserSimilarity> userSimilarities = new HashSet<UserSimilarity>();

    public void clearMessageRanks() {
        this.messageRanks.clear();
    }

    public void clearMessages() {
        this.messages.clear();
    }

    @Override
    public void close() {
        // TODO serialize?
    }

    @Override
    public Statistics computeStatistics() {
        Statistics statistics = new Statistics();

        statistics.setMessageCount(this.messages.size());
        statistics.setMessageRankCount(this.messageRanks.size());

        statistics.setScoredTermCount(-1);
        statistics.setSubscriptionCount(0);

        statistics.setTermCount(this.termsTerms.size() + this.keyPhraseTerms.size());
        statistics.setUserCount(this.users.size());
        statistics.setUserModelCount(this.userModelHolders.size());
        statistics.setUserModelEntryCount(-1);
        statistics.setUserModelEntryTimeBinCount(-1);

        return statistics;
    }

    @Override
    public void deleteAggregationSubscription(String subscriptionId) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void deleteAndCreateUserSimilarities(Collection<UserSimilarity> values) {
        this.userSimilarities.clear();
        this.userSimilarities.addAll(values);
    }

    @Override
    public void deleteHashWithDates(List<HashWithDate> hashesToDelete) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    public String dumpUserModelSizes() {
        String result = "\n";
        for (Entry<User, UserModelHolder> entry : this.userModelHolders.entrySet()) {
            result += entry.getKey().getGlobalId() + " "
                    + entry.getValue().getUserModelEntries().size() + "\n";
        }
        return result;
    }

    @Override
    public SubscriptionStatus getAggregationSubscription(String subscriptionId) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public List<SubscriptionStatus> getAggregationSubscriptions() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Collection<MessageGroup> getAllMessageGroups() {
        return this.messageGroups.values();
    }

    @Override
    public Collection<User> getAllUsers() {
        return this.users.values();
    }

    @Override
    public List<HashWithDate> getHashsByGlobalSubscriptionId(String subscriptionGlobalId) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Message getMessageByGlobalId(String messageGlobalId) {
        return this.messages.get(messageGlobalId);
    }

    @Override
    public MessageGroup getMessageGroupByGlobalId(String messageGroupGlobalId) {
        return this.messageGroups.get(messageGroupGlobalId);
    }

    @Override
    public MessageRank getMessageRank(String userGlobalId, String messageGlobalId) {

        return this.messageRanks.get(new UserMessageIdentifier(userGlobalId, messageGlobalId));
    }

    public Collection<Message> getMessages() {
        return this.messages.values();
    }

    @Override
    public Collection<Message> getMessagesSince(String messageGroupGlobalId, Date fromDate) {
        Collection<Message> filteredMessages = new HashSet<Message>();
        for (Message message : this.messages.values()) {
            if (messageGroupGlobalId != null
                    && !messageGroupGlobalId.equals(message.getMessageGroup().getGlobalId())) {
                continue;
            }

            if (message.getPublicationDate().after(fromDate)) {
                filteredMessages.add(message);
            }
        }
        return filteredMessages;
    }

    @Override
    public Term getOrCreateTerm(TermCategory termCategory, String name) {
        Map<String, Term> terms;
        switch (termCategory) {
        case KEYPHRASE:
            terms = keyPhraseTerms;
            break;
        case TERM:
            terms = termsTerms;
            break;
        default:
            throw new IllegalArgumentException("termCategory is not known: " + termCategory);
        }
        Term term = terms.get(name);
        if (term == null) {
            term = new Term(termCategory, name);
            term.setId(idGenerator.getNextTermId());
            terms.put(term.getValue(), term);
        }
        return term;
    }

    @Override
    public User getOrCreateUser(String userGlobalId) {
        User user = users.get(userGlobalId);
        if (user == null) {
            user = new User(userGlobalId);
            users.put(userGlobalId, user);
            user.setId(idGenerator.getNextUserId());
        }
        return user;
    }

    @Override
    public UserModel getOrCreateUserModelByUser(String userGlobalId) {
        User user = getOrCreateUser(userGlobalId);
        UserModelHolder userModelHolder = userModelHolders.get(user);
        if (userModelHolder == null) {
            UserModel userModel = new UserModel(user);
            userModel.setId(idGenerator.getNextUserModelId());
            userModelHolder = new UserModelHolder(userModel);
            userModelHolders.put(user, userModelHolder);

        }
        return userModelHolder.getUserModel();
    }

    @Override
    public Map<Term, UserModelEntry> getUserModelEntriesForTerms(UserModel userModel,
            Collection<Term> terms) {
        Map<Term, UserModelEntry> entries = new HashMap<Term, UserModelEntry>();
        UserModelHolder userModelHolder = getUserModelHolder(userModel);
        for (Term t : terms) {
            UserModelEntry entry = userModelHolder.getUserModelEntry(t);
            if (entry != null) {
                entries.put(t, entry);
            }
        }
        return entries;
    }

    public UserModelHolder getUserModelHolder(User user) {
        return this.userModelHolders.get(user);
    }

    private UserModelHolder getUserModelHolder(UserModel userModel) {
        UserModelHolder userModelHolder = this.userModelHolders.get(userModel.getUser());
        if (userModelHolder == null) {
            throw new IllegalStateException(
                    "the userModel has not been created within this persistence. userModel="
                            + userModel);
        }
        return userModelHolder;
    }

    public Map<User, UserModelHolder> getUserModelHolders() {
        return userModelHolders;
    }

    @Override
    public Collection<UserSimilarity> getUserSimilarities(String userGlobalId,
            Collection<String> users, String messageGroupGlobalId, double userSimilarityThreshold) {
        Collection<UserSimilarity> sims = new HashSet<UserSimilarity>();
        for (UserSimilarity similarity : this.userSimilarities) {
            if (similarity.getUserGlobalIdFrom().equals(userGlobalId)
                    && users.contains(similarity.getUserGlobalIdTo())
                    && (messageGroupGlobalId == null
                            && similarity.getMessageGroupGlobalId() == null || similarity
                            .getMessageGroupGlobalId().equals(messageGroupGlobalId))
                    && similarity.getSimilarity() >= userSimilarityThreshold) {
                sims.add(similarity);
            }
        }
        return sims;
    }

    @Override
    public Collection<UserModel> getUsersWithUserModel(Collection<Term> terms) {
        Collection<UserModel> userModels = new HashSet<UserModel>();
        userModels: for (UserModelHolder holder : this.userModelHolders.values()) {
            for (Term term : terms) {
                if (holder.getUserModelEntry(term) != null) {
                    userModels.add(holder.getUserModel());
                    continue userModels;
                }
            }
        }
        return userModels;
    }

    @Override
    public void initialize() {
        idGenerator = new IdGenerator();

    }

    public void removeMessage(String globalId) {
        this.messages.remove(globalId);

    }

    @Override
    public void removeUserModelEntry(UserModel userModel, UserModelEntry userModelEntry) {
        UserModelHolder userModelHolder = this.userModelHolders.get(userModel.getUser());
        userModelHolder.getUserModelEntries().remove(userModelEntry.getScoredTerm().getTerm());

    }

    public void resetTerms(Set<Term> termsStillNeeded) {

        for (Term t : termsStillNeeded) {
            if (TermCategory.KEYPHRASE.equals(t.getCategory())) {
                this.keyPhraseTerms.put(t.getValue(), t);
            } else if (TermCategory.TERM.equals(t.getCategory())) {
                this.termsTerms.put(t.getValue(), t);
            }
        }
    }

    public void resetTermsPrepare() {
        this.termsTerms.clear();
        this.keyPhraseTerms.clear();
    }

    @Override
    public SubscriptionStatus saveAggregationSubscription(SubscriptionStatus aggregationSubscription) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public HashWithDate saveHashWithDate(HashWithDate hashWithDate) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Message storeMessage(Message message) {
        // TODO check that terms have an id ?
        this.messages.put(message.getGlobalId(), message);
        return message;
    }

    @Override
    public MessageGroup storeMessageGroup(MessageGroup messageGroup) {
        messageGroup.setId(idGenerator.getNextMessageGroupId());
        messageGroups.put(messageGroup.getGlobalId(), messageGroup);

        return messageGroup;
    }

    @Override
    public void storeMessageRanks(Collection<MessageRank> ranks) {
        for (MessageRank messageRank : ranks) {
            this.messageRanks.put(new UserMessageIdentifier(messageRank.getUserGlobalId(),
                    messageRank.getMessageGlobalId()), messageRank);
        }
    }

    @Override
    public void storeMessageRelation(Message message, MessageRelation relatedMessages) {
        relatedMessages.setId(idGenerator.getNextMessageRelationId());
        this.messageRelations.put(message.getGlobalId(), relatedMessages);
    }

    @Override
    public Collection<UserModelEntry> storeOrUpdateUserModelEntries(UserModel userModel,
            Collection<UserModelEntry> changedEntries) {
        UserModelHolder holder = getUserModelHolder(userModel);
        for (UserModelEntry entry : changedEntries) {
            if (entry.getId() == null) {
                entry.setId(idGenerator.getNextUserModelEntryId());
            }
            // internal its just a put.
            // for optimization we could ommit this iff entry.getId != null
            holder.addUserModelEntry(entry);
        }
        return changedEntries;
    }

    @Override
    public void updateAggregationSubscription(SubscriptionStatus aggregationStatus) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }
}