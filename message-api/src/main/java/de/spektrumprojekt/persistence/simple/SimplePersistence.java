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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.duplicationdetection.HashWithDate;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageFilter;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessagePublicationDateComperator;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
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
import de.spektrumprojekt.persistence.UserMessageScoreVisitor;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.Statistics;

/**
 * This persistence maintains the data completly in memory and may (not yet implemented) load and
 * store it into seralizable files.
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * @author Philipp Katz
 */
public class SimplePersistence implements Persistence {

    public static class ObservationKey {

        private final String userGlobalId;

        private final String messageGlobalId;

        private final ObservationType observationType;

        public ObservationKey(String userGlobalId, String messageGlobalId,
                ObservationType observationType) {
            this.userGlobalId = userGlobalId;
            this.messageGlobalId = messageGlobalId;
            this.observationType = observationType;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ObservationKey other = (ObservationKey) obj;

            if (messageGlobalId == null) {
                if (other.messageGlobalId != null) {
                    return false;
                }
            } else if (!messageGlobalId.equals(other.messageGlobalId)) {
                return false;
            }
            if (observationType != other.observationType) {
                return false;
            }
            if (userGlobalId == null) {
                if (other.userGlobalId != null) {
                    return false;
                }
            } else if (!userGlobalId.equals(other.userGlobalId)) {
                return false;
            }
            return true;
        }

        public String getMessageGlobalId() {
            return messageGlobalId;
        }

        public ObservationType getObservationType() {
            return observationType;
        }

        public String getUserGlobalId() {
            return userGlobalId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (messageGlobalId == null ? 0 : messageGlobalId.hashCode());
            result = prime * result + (observationType == null ? 0 : observationType.hashCode());
            result = prime * result + (userGlobalId == null ? 0 : userGlobalId.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return "ObservationKey [userGlobalId=" + userGlobalId + ", messageGlobalId="
                    + messageGlobalId + ", observationType=" + observationType + "]";
        }
    }

    private final IdGenerator idGenerator = new IdGenerator();

    private final Map<String, User> users = new HashMap<String, User>();

    // first key: userModelType, second key: user of user model
    private final Map<String, Map<User, UserModelHolder>> userModelByTypeHolders = new HashMap<String, Map<User, UserModelHolder>>();

    private final Map<String, Message> messages = new HashMap<String, Message>();

    // key is global id of message
    private final Map<String, MessageRelation> messageRelations = new HashMap<String, MessageRelation>();

    // key is global id of message group
    private final Map<String, MessageGroup> messageGroups = new HashMap<String, MessageGroup>();

    private final Map<UserMessageIdentifier, UserMessageScore> messageScores = new HashMap<UserMessageIdentifier, UserMessageScore>();

    private final Map<String, Term> termsTerms = new HashMap<String, Term>();

    private final Map<String, Term> keyPhraseTerms = new HashMap<String, Term>();

    private final Map<String, UserSimilarity> userSimilarities = new HashMap<String, UserSimilarity>();

    private final Map<String, List<Message>> patternMessages = new HashMap<String, List<Message>>();

    private final Map<ObservationKey, Collection<Observation>> observations = new HashMap<SimplePersistence.ObservationKey, Collection<Observation>>();

    private final Map<String, SourceStatus> sourceStatusMap = new HashMap<String, SourceStatus>();

    private TermFrequency termFrequency = new TermFrequency();

    public void clearMessageRanks() {
        this.messageScores.clear();
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
        statistics.setMessageScoreCount(this.messageScores.size());

        statistics.setScoredTermCount(-1);
        statistics.setSubscriptionCount(0);

        statistics.setTermCount(this.termsTerms.size() + this.keyPhraseTerms.size());
        statistics.setUserCount(this.users.size());
        statistics.setUserModelCount(this.userModelByTypeHolders.size());
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
        for (UserSimilarity sim : values) {
            this.userSimilarities.put(sim.getKey(), sim);
        }
    }

    @Override
    public void deleteHashWithDates(List<HashWithDate> hashesToDelete) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public void deleteSource(String sourceGlobalId) {
        throw new UnsupportedOperationException("Implement me ...");
    }

    @Override
    public void deleteSubscription(String subscriptionGlobalId) {
        throw new UnsupportedOperationException("Implement me ...");
    }

    public String dumpUserModelSizes() {
        String result = "UserModel Sizes (userModelType => userId => userModelEntries.size) \n";
        for (Entry<String, Map<User, UserModelHolder>> userModelTypeEntry : this.userModelByTypeHolders
                .entrySet()) {
            for (Entry<User, UserModelHolder> entry : userModelTypeEntry.getValue().entrySet()) {
                result += userModelTypeEntry.getKey() + " " + entry.getKey().getGlobalId() + " "
                        + entry.getValue().getUserModelEntries().size() + "\n";
            }
        }
        return result;
    }

    @Override
    public Source findSource(String connectorType, Collection<Property> accessParameters) {
        throw new UnsupportedOperationException("Implement me.");
    }

    @Override
    public List<SourceStatus> findSourceStatusByProperty(Property property) {
        return null;
    }

    @Override
    public Collection<MessageGroup> getAllMessageGroups() {
        return this.messageGroups.values();
    }

    @Override
    public Collection<Term> getAllTerms() {
        return this.termsTerms.values();
    }

    @Override
    public Map<UserModel, Collection<UserModelEntry>> getAllUserModelEntries(String userModelType) {
        Map<UserModel, Collection<UserModelEntry>> result = new HashMap<UserModel, Collection<UserModelEntry>>();
        Map<User, UserModelHolder> map = userModelByTypeHolders.get(userModelType);
        if (map != null) {
            for (UserModelHolder holder : map.values()) {
                result.put(holder.getUserModel(), holder.getUserModelEntries().values());
            }
        }
        return result;
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
    public UserMessageScore getMessageScore(String userGlobalId, String messageGlobalId) {

        return this.messageScores.get(new UserMessageIdentifier(userGlobalId, messageGlobalId));
    }

    @Override
    public MessageRelation getMessageRelation(Message message) {
        return messageRelations.get(message.getGlobalId());
    }

    public Collection<Message> getMessages() {
        return this.messages.values();
    }

    @Override
    public List<Message> getMessages(MessageFilter messageFilter) {
        List<Message> filteredMessages = new ArrayList<Message>();
        List<Message> baseMessages = new ArrayList<Message>();

        if (messageFilter.getPattern() != null) {
            baseMessages.addAll(patternMessages.get(messageFilter.getPattern()));
        } else {
            baseMessages.addAll(this.messages.values());
        }
        Collections.sort(baseMessages, new MessagePublicationDateComperator());

        for (Message message : baseMessages) {
            if (messageFilter.getMessageGroupGlobalId() != null
                    && !messageFilter.getMessageGroupGlobalId().equals(
                            message.getMessageGroup().getGlobalId())) {
                continue;
            }
            if (messageFilter.getMessageGroupId() != null
                    && !messageFilter.getMessageGroupId().equals(
                            message.getMessageGroup().getId())) {
                continue;
            }

            if (messageFilter.getMinPublicationDate() == null
                    || message.getPublicationDate().after(messageFilter.getMinPublicationDate())) {
                filteredMessages.add(message);
            }

            if (messageFilter.getLastMessagesCount() > 0
                    && messageFilter.getLastMessagesCount() <= filteredMessages.size()) {
                break;
            }
        }
        return filteredMessages;
    }

    @Override
    public int getNumberOfSubscriptionsBySourceGlobalId(String globalId) {
        throw new UnsupportedOperationException("Implement me.");
    }

    public Map<ObservationKey, Collection<Observation>> getObservations() {
        return observations;
    }

    public Collection<Observation> getObservations(
            ObservationType observationType) {
        Collection<Observation> filtered = new HashSet<Observation>();
        for (Collection<Observation> allObservations : this.observations.values()) {
            for (Observation obs : allObservations) {
                if (observationType.equals(obs.getObservationType())) {
                    filtered.add(obs);
                }
            }
        }
        return filtered;

    }

    @Override
    public Collection<Observation> getObservations(String userGlobalId, String messageGlobalId,
            ObservationType observationType) {
        ObservationKey key = new ObservationKey(userGlobalId, messageGlobalId, observationType);
        Collection<Observation> observations = this.observations.get(key);
        return observations;
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
        User user = getUserByGlobalId(userGlobalId);
        if (user == null) {
            user = new User(userGlobalId);
            users.put(userGlobalId, user);
            user.setId(idGenerator.getNextUserId());
        }
        return user;
    }

    @Override
    public UserModel getOrCreateUserModelByUser(String userGlobalId, String userModelType) {
        User user = getOrCreateUser(userGlobalId);
        Map<User, UserModelHolder> userModelTypeHolders = getOrCreateUserModelTypeHoldersByUserModelType(userModelType);
        UserModelHolder userModelHolder = userModelTypeHolders.get(user);
        if (userModelHolder == null) {
            UserModel userModel = new UserModel(user, userModelType);
            userModel.setId(idGenerator.getNextUserModelId());
            userModelHolder = new UserModelHolder(userModel);
            userModelTypeHolders.put(user, userModelHolder);
        }

        return userModelHolder.getUserModel();
    }

    private Map<User, UserModelHolder> getOrCreateUserModelTypeHoldersByUserModelType(
            String userModelType) {
        Map<User, UserModelHolder> userModelHolder = this.userModelByTypeHolders.get(userModelType);
        if (userModelHolder == null) {
            userModelHolder = new HashMap<User, UserModelHolder>();
            this.userModelByTypeHolders.put(userModelType, userModelHolder);
        }
        return userModelHolder;
    }

    public Map<String, List<Message>> getPatternMessages() {
        return patternMessages;
    }

    @Override
    public Source getSourceByGlobalId(String sourceGlobalId) {
        throw new UnsupportedOperationException("Implement me ...");
    }

    @Override
    public SourceStatus getSourceStatusBySourceGlobalId(String subscriptionId) {
        return sourceStatusMap.get(subscriptionId);
    }

    @Override
    public List<SourceStatus> getSourceStatusList() {
        LinkedList<SourceStatus> result = new LinkedList<SourceStatus>();
        result.addAll(sourceStatusMap.values());
        return result;
    }

    @Override
    public Subscription getSubscriptionByGlobalId(String subscriptionGlobalId) {
        throw new UnsupportedOperationException("Implement me ...");
    }

    @Override
    public List<Subscription> getSubscriptions(SubscriptionFilter subscriptionFilter) {
        throw new UnsupportedOperationException("Not implemented.");
    }
    
    @Override
    public List<SubscriptionSourceStatus> getSubscriptionsWithSourceStatus(SubscriptionFilter subscriptionFilter) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public TermFrequency getTermFrequency() {
        termFrequency.init();
        return termFrequency;
    }

    @Override
    public User getUserByGlobalId(String userGlobalId) {
        return users.get(userGlobalId);
    }

    public User getUserById(Long userId) {
        for (User user : this.users.values()) {
            if (userId.equals(user.getId())) {
                return user;
            }
        }
        return null;
    }

    public Map<String, Map<User, UserModelHolder>> getUserModelByTypeHolders() {
        return userModelByTypeHolders;
    }

    public Map<User, UserModelHolder> getUserModelByTypeHolders(String userModelType) {
        return userModelByTypeHolders.get(userModelType);
    }

    @Override
    public Collection<UserModelEntry> getUserModelEntries(UserModel userModel,
            Collection<String> termsToMatch, Collection<Long> messageGroupIdsToConsider,
            MatchMode matchMode,
            boolean useMGFreeTermValue) {
        Map<User, UserModelHolder> entries = this.userModelByTypeHolders.get(userModel
                .getUserModelType());
        Collection<UserModelEntry> umEntries = new HashSet<UserModelEntry>();
        if (entries != null) {
            UserModelHolder holder = entries.get(userModel.getUser());
            if (holder != null) {
                for (String t : termsToMatch) {
                    umEntries.addAll(holder.getUserModelEntry(t, messageGroupIdsToConsider,
                            matchMode, useMGFreeTermValue));
                }
            }
        }
        return umEntries;
    }

    @Override
    public Collection<UserModelEntry> getUserModelEntries(
            UserModel userModel,
            Collection<String> termsToMatch,
            MatchMode matchMode) {
        return this.getUserModelEntries(
                userModel,
                termsToMatch,
                null,
                matchMode,
                false);
    }

    /**
     * 
     * @return a map of user model type => description of the counts
     */
    @Override
    public Map<String, String> getUserModelEntriesCountDescription() {
        Map<String, String> countDescription = new HashMap<String, String>();
        for (Entry<String, Map<User, UserModelHolder>> userModelHolders : this.userModelByTypeHolders
                .entrySet()) {

            int size = 0;
            int timeBins = 0;

            for (UserModelHolder userModel : userModelHolders.getValue().values()) {
                size += userModel.getUserModelEntries().size();
                for (UserModelEntry entry : userModel.getUserModelEntries().values()) {
                    if (entry.getTimeBinEntries() != null) {
                        timeBins += entry.getTimeBinEntries().size();
                    }
                }
            }
            double ratio = timeBins / (double) size;
            String desc = size + " ume " + timeBins + " timeBins " + ratio + " bins_per_entry";

            countDescription.put(userModelHolders.getKey(), desc);
        }
        return countDescription;

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

    public UserModelHolder getUserModelHolder(String userModelType, User user) {
        Map<User, UserModelHolder> userHolders = getUserModelByTypeHolders(userModelType);
        if (userHolders != null) {
            return userHolders.get(user);
        }
        return null;
    }

    public UserModelHolder getUserModelHolder(User user, String userModelType) {
        return this.getOrCreateUserModelTypeHoldersByUserModelType(userModelType).get(user);
    }

    private UserModelHolder getUserModelHolder(UserModel userModel) {
        UserModelHolder userModelHolder = this.getOrCreateUserModelTypeHoldersByUserModelType(
                userModel.getUserModelType()).get(userModel.getUser());
        if (userModelHolder == null) {
            throw new IllegalStateException(
                    "the userModel has not been created within this persistence. userModel="
                            + userModel);
        }
        return userModelHolder;
    }

    @Override
    public Collection<UserSimilarity> getUserSimilarities(String messageGroupGlobalId) {
        if (messageGroupGlobalId == null) {
            return new HashSet<UserSimilarity>(this.userSimilarities.values());
        }
        Collection<UserSimilarity> sims = new HashSet<UserSimilarity>();
        for (UserSimilarity similarity : this.userSimilarities.values()) {
            if (messageGroupGlobalId.equals(similarity.getMessageGroupGlobalId())) {
                sims.add(similarity);
            }
        }
        return sims;
    }

    @Override
    public Collection<UserSimilarity> getUserSimilarities(
            String userToGlobalId,
            Collection<String> usersFromGlobalId,
            String messageGroupGlobalId,
            double userSimilarityThreshold) {
        Collection<UserSimilarity> sims = new HashSet<UserSimilarity>();

        for (String userTo : usersFromGlobalId) {
            String simKey = UserSimilarity.getKey(userToGlobalId, userTo, messageGroupGlobalId);
            UserSimilarity sim = this.userSimilarities.get(simKey);
            if (sim != null && sim.getSimilarity() >= userSimilarityThreshold) {
                sims.add(sim);
            }
        }

        return sims;
    }

    @Override
    public UserSimilarity getUserSimilarity(String userGlobalIdFrom, String userGlobalIdTo,
            String messageGroupGlobalId) {
        return this.userSimilarities.get(UserSimilarity.getKey(userGlobalIdFrom, userGlobalIdTo,
                messageGroupGlobalId));
    }

    @Override
    public Collection<UserModel> getUsersWithUserModel(Collection<Term> terms,
            String userModelType, MatchMode matchMode) {
        Collection<UserModel> userModels = new HashSet<UserModel>();
        userModels: for (UserModelHolder holder : this
                .getOrCreateUserModelTypeHoldersByUserModelType(userModelType).values()) {
            for (Term term : terms) {
                if (MatchMode.EXACT.equals(matchMode)) {

                    if (holder.getUserModelEntry(term) != null) {
                        userModels.add(holder.getUserModel());
                        continue userModels;
                    }
                } else {

                    Collection<UserModelEntry> entries = holder.getUserModelEntry(term,
                            matchMode);
                    if (entries.size() > 0) {
                        userModels.add(holder.getUserModel());
                        continue userModels;
                    }
                }
            }
        }
        return userModels;
    }

    @Override
    public void initialize() {

    }

    public void removeMessage(String globalId) {
        this.messages.remove(globalId);

    }

    @Override
    public void removeUserModelEntry(UserModel userModel, UserModelEntry userModelEntry) {
        UserModelHolder userModelHolder = this.getOrCreateUserModelTypeHoldersByUserModelType(
                userModel.getUserModelType()).get(userModel.getUser());
        userModelHolder.removeUserModelEntry(userModelEntry);
    }

    @Override
    public void resetTermCount() {
        for (Term term : this.termsTerms.values()) {
            term.setCount(0);
        }
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
    public HashWithDate saveHashWithDate(HashWithDate hashWithDate) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Source saveSource(Source source) {
        throw new UnsupportedOperationException("Implement me ...");
    }

    @Override
    public SourceStatus saveSourceStatus(SourceStatus sourceStatus) {
        sourceStatusMap.put(sourceStatus.getSource().getGlobalId(),
                sourceStatus);
        return sourceStatus;
    }

    @Override
    public Message storeMessage(Message message) {
        if (message.getId() == null) {
            message.setId(idGenerator.getNextMessage());
        } else {
            Message exists = this.messages.get(message.getGlobalId());
            if (exists != null && !message.getId().equals(exists.getId())) {
                throw new RuntimeException(
                        "Can not replace a message with same global id but different (long) id.");
            }
        }
        for (MessagePart mp : message.getMessageParts()) {
            for (ScoredTerm scoredTerm : mp.getScoredTerms()) {
                Term term = this.getOrCreateTerm(scoredTerm.getTerm().getCategory(), scoredTerm
                        .getTerm().getValue());
                scoredTerm.setTerm(term);
            }
        }
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
    public void storeMessagePattern(String pattern, Message message) {
        List<Message> messages = patternMessages.get(pattern);
        if (messages == null) {
            messages = new ArrayList<Message>();
            patternMessages.put(pattern, messages);
        }
        messages.add(message);
    }

    @Override
    public void storeMessageRanks(Collection<UserMessageScore> ranks) {
        for (UserMessageScore messageRank : ranks) {
            this.messageScores.put(new UserMessageIdentifier(messageRank.getUserGlobalId(),
                    messageRank.getMessageGlobalId()), messageRank);
        }
    }

    @Override
    public void storeMessageRelation(Message message, MessageRelation relatedMessages) {
        relatedMessages.setId(idGenerator.getNextMessageRelationId());
        this.messageRelations.put(message.getGlobalId(), relatedMessages);
    }

    @Override
    public void storeObservation(Observation observation) {
        ObservationKey key = new ObservationKey(observation.getUserGlobalId(),
                observation.getMessageGlobalId(), observation.getObservationType());
        Collection<Observation> observations = this.observations.get(key);
        if (observations == null) {
            observations = new HashSet<Observation>();
            this.observations.put(key, observations);
        }
        observations.add(observation);
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
    public Subscription storeSubscription(Subscription subscription) {
        throw new UnsupportedOperationException("Implement me ...");
    }

    @Override
    public void storeUserSimilarity(UserSimilarity stat) {
        this.userSimilarities.put(stat.getKey(), stat);

    }

    @Override
    public void updateMessageRank(UserMessageScore rankToUpdate) {
        UserMessageIdentifier userMessageIdentifier = new UserMessageIdentifier(rankToUpdate);
        UserMessageScore existingRank = this.messageScores.get(userMessageIdentifier);

        if (existingRank != rankToUpdate) {
            this.messageScores.put(userMessageIdentifier, rankToUpdate);
        }
    }

    @Override
    public Source updateSource(Source source) {
        throw new UnsupportedOperationException("Implement me ...");
    }

    @Override
    public void updateSourceStatus(SourceStatus sourceStatus) {
        sourceStatusMap.put(sourceStatus.getSource().getGlobalId(), sourceStatus);
    }

    @Override
    public Subscription updateSubscription(Subscription subscription) {
        throw new UnsupportedOperationException("Implement me ...");
    }

    @Override
    public void updateTermFrequency(TermFrequency termFrequency) {
        this.termFrequency = termFrequency;
    }

    @Override
    public void updateTerms(Collection<Term> termsChanged) {
        for (Term term : termsChanged) {
            if (this.termsTerms.get(term.getValue()) != term) {
                throw new RuntimeException("The term '" + term
                        + "' is not created within this persistence. Cannot update!");

            }
        }
    }

    @Override
    public void visitAllMessageRanks(UserMessageScoreVisitor visitor, Date startDate, Date endDate)
            throws Exception {
        for (UserMessageScore messageRank : this.messageScores.values()) {
            Message message = this.getMessageByGlobalId(messageRank.getMessageGlobalId());
            if (startDate.after(message.getPublicationDate())) {
                continue;
            }
            if (endDate.before(message.getPublicationDate())) {
                continue;
            }
            visitor.visit(messageRank, message);
        }
    }

}
