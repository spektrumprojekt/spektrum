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

package de.spektrumprojekt.i.learner.adaptation;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.event.EventHandler;
import de.spektrumprojekt.communication.CommunicationMessage;
import de.spektrumprojekt.communication.MessageHandler;
import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;
import de.spektrumprojekt.i.ranker.Scorer;
import de.spektrumprojekt.i.similarity.messagegroup.MessageGroupSimilarityRetriever;
import de.spektrumprojekt.i.similarity.user.UserScore;
import de.spektrumprojekt.i.similarity.user.UserToUserInterestSelector;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.Persistence.MatchMode;

public class DirectedUserModelAdapter implements
        MessageHandler<DirectedUserModelAdaptationMessage>, ConfigurationDescriptable {

    private final static Logger LOGGER = LoggerFactory.getLogger(DirectedUserModelAdapter.class);

    private long adaptedCount = 0;

    private long requestedAdaptedCount = 0;

    private final Persistence persistence;

    private final Scorer scorer;

    private final UserToUserInterestSelector userToUserInterestRetriever;

    private final DescriptiveStatistics descriptiveStatistics;

    private final String userModelType;

    private final EventHandler<UserModelAdaptationReScoreEvent> userModelAdaptationReScoreEventHandler = new EventHandler<UserModelAdaptationReScoreEvent>();

    private final MessageGroupSimilarityRetriever messageGroupSimilarityRetriever;

    private final UserModelAdapterConfiguration userModelAdapterConfiguration;

    public DirectedUserModelAdapter(
            Persistence persistence,
            Scorer scorer,
            String userModelType,
            UserToUserInterestSelector userToUserInterestRetriever,
            MessageGroupSimilarityRetriever messageGroupSimilarityRetriever,
            UserModelAdapterConfiguration userModelAdapterConfiguration,
            boolean keepStatistics) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (scorer == null) {
            throw new IllegalArgumentException("scorer cannot be null.");
        }
        if (!userModelAdapterConfiguration.isAdaptFromMessageGroups()
                && userToUserInterestRetriever == null) {
            throw new IllegalArgumentException("userToUserInterestRetriever cannot be null.");
        }
        if (userModelType == null) {
            throw new IllegalArgumentException("userModelType cannot be null.");
        }
        if (messageGroupSimilarityRetriever == null
                && userModelAdapterConfiguration.isAdaptFromMessageGroups()) {
            throw new IllegalArgumentException(
                    "messageGroupSimilarityRetriever cannot be null if adaptFromMessageGroups is true.");
        }
        this.persistence = persistence;
        this.scorer = scorer;
        this.userToUserInterestRetriever = userToUserInterestRetriever;

        if (keepStatistics) {
            descriptiveStatistics = new DescriptiveStatistics();
        } else {
            descriptiveStatistics = null;
        }
        this.userModelType = userModelType;
        this.messageGroupSimilarityRetriever = messageGroupSimilarityRetriever;

        this.userModelAdapterConfiguration = userModelAdapterConfiguration;

    }

    public DirectedUserModelAdapter(
            Persistence persistence,
            Scorer scorer,
            UserToUserInterestSelector userToUserInterestRetriever,
            MessageGroupSimilarityRetriever messageGroupSimilarityRetriever,
            UserModelAdapterConfiguration userModelAdapterConfiguration) {
        this(
                persistence,
                scorer,
                UserModel.DEFAULT_USER_MODEL_TYPE,
                userToUserInterestRetriever,
                messageGroupSimilarityRetriever,
                userModelAdapterConfiguration,
                false);
    }

    public Map<Term, UserModelEntry> adaptTerms(
            UserModel userModelToAdapt, Collection<Term> termsToAdapt,
            Map<Term, ValueAggregator> statsForTermsToAdaptFrom) {
        Map<Term, UserModelEntry> entries = persistence.getUserModelEntriesForTerms(
                userModelToAdapt, termsToAdapt);

        for (Entry<Term, ValueAggregator> statEntryToAdaptFrom : statsForTermsToAdaptFrom
                .entrySet()) {
            if (statEntryToAdaptFrom.getValue().getValue() > 0) {
                // adapt
                UserModelEntry entryToAdapt = entries.get(statEntryToAdaptFrom.getKey());
                if (entryToAdapt == null) {
                    ScoredTerm scoredTerm = new ScoredTerm(statEntryToAdaptFrom.getKey(), 0);
                    entryToAdapt = new UserModelEntry(userModelToAdapt, scoredTerm);
                    entries.put(scoredTerm.getTerm(), entryToAdapt);
                }
                if (entryToAdapt.getScoredTerm().getWeight() < statEntryToAdaptFrom.getValue()
                        .getValue()) {
                    entryToAdapt.getScoredTerm().setWeight(
                            (float) statEntryToAdaptFrom.getValue().getValue());

                    entryToAdapt.setAdapted(true);
                    adaptedCount++;
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace(
                                "Adapted user model entry for user '{}' set term '{}' to score '{}'. ",
                                new Object[] {
                                        userModelToAdapt.getUser().getGlobalId(),
                                        entryToAdapt.getScoredTerm().getTerm().getValue(),
                                        entryToAdapt.getScoredTerm().getWeight() });
                    }

                    if (descriptiveStatistics != null) {
                        descriptiveStatistics
                                .addValue(entryToAdapt.getScoredTerm().getWeight());
                    }
                }
            }
        }
        return entries;
    }

    private boolean adaptTermsOfMG(Long mgId) {
        /*
         * MessageGroup messageGroup = this.persistence.getMessageGroupByGlobalId("communotedev");
         * return mgId.equals(messageGroup.getId() + "");
         */
        return true;
    }

    private ValueAggregator createNewValueAggregator() {
        return userModelAdapterConfiguration.isUseWeightedAverageForAggregatingSimilarUsers() ? new IncrementalWeightedAverage()
                : new MaxValueAggregator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deliverMessage(DirectedUserModelAdaptationMessage message) throws Exception {
        UserModel userModelToAdapt = this.persistence.getOrCreateUserModelByUser(message
                .getUserGlobalId(), userModelType);
        requestedAdaptedCount++;

        // 1. Identify the terms of the messages that are not contained in the user model UMu.
        Collection<Term> termsToAdapt = Arrays.asList(message.getTermsToAdapt());

        final Map<Term, ValueAggregator> statsForTermsToAdaptFrom = new HashMap<Term, ValueAggregator>();

        if (userModelAdapterConfiguration.isAdaptFromMessageGroups()) {
            determineTermsBySimilarTopics(message, termsToAdapt, statsForTermsToAdaptFrom);
        } else {
            determineTermsBySimilarUsers(message, termsToAdapt, statsForTermsToAdaptFrom);
        }

        if (statsForTermsToAdaptFrom.size() > 0) {
            Map<Term, UserModelEntry> entries = adaptTerms(
                    userModelToAdapt,
                    termsToAdapt,
                    statsForTermsToAdaptFrom);

            persistence.storeOrUpdateUserModelEntries(userModelToAdapt, entries.values());

            Message messageToRerate = this.persistence.getMessageByGlobalId(message.getMessageId());

            MessageFeatureContext messageFeatureContextOfReScore = scorer.rescore(messageToRerate,
                    message.getUserGlobalId());

            if (this.userModelAdaptationReScoreEventHandler.getEventListenersSize() > 0) {
                UserModelAdaptationReScoreEvent reScoreEvent = new UserModelAdaptationReScoreEvent();
                reScoreEvent.setMessageFeatureContextOfReScore(messageFeatureContextOfReScore);
                reScoreEvent.setAdaptationMessage(message);
                this.userModelAdaptationReScoreEventHandler.fire(reScoreEvent);
            }
        }

    }

    private void determineTermsBySimilarTopics(DirectedUserModelAdaptationMessage message,
            Collection<Term> termsToAdapt, final Map<Term, ValueAggregator> statsForTermsToAdaptFrom) {

        UserModel userModel = persistence.getOrCreateUserModelByUser(message.getUserGlobalId(),
                userModelType);
        Collection<String> match = new HashSet<String>();
        Long targetMessageGroupId = null;
        for (Term t : termsToAdapt) {
            Long mgId = t.getMessageGroupId();
            if (adaptTermsOfMG(mgId)) {

                String toMatch = t.extractMessageGroupFreeTermValue();
                match.add(toMatch);
            }
            if (targetMessageGroupId == null) {
                targetMessageGroupId = t.getMessageGroupId();
            } else if (!targetMessageGroupId.equals(t.getMessageGroupId())) {
                throw new IllegalStateException(
                        "Have different MG for term adaptation. But a single message of one message group. I give up. targetMessageGroupId: "
                                + targetMessageGroupId
                                + " t.getMessageGroupId(): "
                                + t.getMessageGroupId() + " message: " + message);
            }
        }
        // user model entriesof other groups
        Collection<UserModelEntry> userModelEntries = persistence.getUserModelEntries(userModel,
                match,
                MatchMode.ENDS_WITH);

        for (Term t : termsToAdapt) {
            for (UserModelEntry entry : userModelEntries) {

                Long mgId = entry.getScoredTerm().getTerm().getMessageGroupId();

                // get topic sim between mgId and t.getMessageGroupId
                double topicSim = messageGroupSimilarityRetriever.getMessageGroupSimilarity(
                        targetMessageGroupId, mgId);

                if (topicSim >= userModelAdapterConfiguration.getMessageGroupSimilarityThreshold()) {
                    if (useMGForAdapation(mgId)) {

                        if (!entry.isAdapted()
                                && MatchMode.EXACT.matches(entry
                                        .getScoredTerm().getTerm()
                                        .extractMessageGroupFreeTermValue(),
                                        t.extractMessageGroupFreeTermValue())) {

                            ValueAggregator aggregator = getValueAggegrator(
                                    statsForTermsToAdaptFrom, t);

                            aggregator
                                    .add(entry.getScoredTerm().getWeight(), entry.getScoreCount());
                        }
                    }
                }
            }
        }

    }

    private void determineTermsBySimilarUsers(DirectedUserModelAdaptationMessage message,
            Collection<Term> termsToAdapt, final Map<Term, ValueAggregator> statsForTermsToAdaptFrom) {

        // User models to adapt from that contain at least one of terms in termsToAdapt
        Collection<UserModel> userModels = persistence.getUsersWithUserModel(termsToAdapt,
                userModelType, MatchMode.EXACT);
        Map<String, UserModel> userToUserModels = new HashMap<String, UserModel>();
        Collection<String> users = new HashSet<String>();
        for (UserModel userModel : userModels) {
            users.add(userModel.getUser().getGlobalId());
            userToUserModels.put(userModel.getUser().getGlobalId(), userModel);
        }

        // Compute user similarity between the user and the users of the found user models.
        Collection<UserScore> userToUserInterestScores = this.userToUserInterestRetriever
                .getUserToUserInterest(message.getUserGlobalId(),
                        message.getMessageGroupGlobalId(), users);

        // 4. If the user similarity satisfies a threshold u take the weight of the term and apply
        // it to UMu. If there are more weights available use a weighted average of the term weights
        // and the user similarity

        if (userToUserInterestScores != null && userToUserInterestScores.size() > 0) {
            // there are similarities to use

            for (UserScore userScore : userToUserInterestScores) {

                UserModel userModel = userToUserModels.get(userScore.getUserGlobalId());
                if (userModel != null) {

                    Map<Term, UserModelEntry> entries = persistence.getUserModelEntriesForTerms(
                            userModel, termsToAdapt);
                    for (Entry<Term, UserModelEntry> entry : entries.entrySet()) {
                        if (!entry.getValue().isAdapted()) {
                            Term t = entry.getKey();
                            ValueAggregator aggregator = getValueAggegrator(
                                    statsForTermsToAdaptFrom, t);
                            integrateStat(aggregator, userScore, entry.getValue());
                        }
                    }
                }
            }
        }
    }

    public long getAdaptedCount() {
        return adaptedCount;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName()
                + " userToUserInterestRetriever: "
                + (userToUserInterestRetriever == null ? "null" : userToUserInterestRetriever
                        .getConfigurationDescription())
                + " userModelAdapterConfiguration: "
                + this.userModelAdapterConfiguration.getConfigurationDescription()
                + " messageGroupSimilarityRetriever: "
                + (messageGroupSimilarityRetriever == null ? "null"
                        : messageGroupSimilarityRetriever.getConfigurationDescription());
    }

    public String getDebugInformation() {
        return descriptiveStatistics == null ? "debug information not maintained." :
                "Stats about the term scores that have been set for adapation: "
                        + this.descriptiveStatistics.toString().replace("\n", " ");
    }

    @Override
    public Class<DirectedUserModelAdaptationMessage> getMessageClass() {
        return DirectedUserModelAdaptationMessage.class;
    }

    public long getRequestAdaptedCount() {
        return requestedAdaptedCount;
    }

    public EventHandler<UserModelAdaptationReScoreEvent> getUserModelAdaptationReScoreEventHandler() {
        return userModelAdaptationReScoreEventHandler;
    }

    public ValueAggregator getValueAggegrator(
            final Map<Term, ValueAggregator> statsForTermsToAdaptFrom, Term term) {
        ValueAggregator stat = statsForTermsToAdaptFrom.get(term);
        if (stat == null) {
            stat = createNewValueAggregator();
            statsForTermsToAdaptFrom.put(term, stat);
        }
        return stat;
    }

    private void integrateStat(ValueAggregator stat, UserScore userScore,
            UserModelEntry value) {
        stat.add(value.getScoredTerm().getWeight(), userScore.getScore());
    }

    @Override
    public boolean supports(CommunicationMessage message) {
        return message instanceof DirectedUserModelAdaptationMessage;
    }

    private boolean useMGForAdapation(Long mgId) {
        // MessageGroup messageGroup = this.persistence.getMessageGroupByGlobalId("communotedev");
        // return mgId.equals(messageGroup.getId() + "");
        return true;
    }

}
