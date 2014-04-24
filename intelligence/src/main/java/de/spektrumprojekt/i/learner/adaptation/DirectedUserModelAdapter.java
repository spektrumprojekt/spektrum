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
import de.spektrumprojekt.i.ranker.Ranker;
import de.spektrumprojekt.i.user.UserScore;
import de.spektrumprojekt.i.user.UserToUserInterestSelector;
import de.spektrumprojekt.persistence.Persistence;

public class DirectedUserModelAdapter implements
        MessageHandler<DirectedUserModelAdaptationMessage>, ConfigurationDescriptable {

    private final static Logger LOGGER = LoggerFactory.getLogger(DirectedUserModelAdapter.class);

    private long adaptedCount = 0;

    private long requestedAdaptedCount = 0;

    private final Persistence persistence;

    private final Ranker ranker;

    private final UserToUserInterestSelector userToUserInterestRetriever;

    private final DescriptiveStatistics descriptiveStatistics;

    private final boolean useWeightedAverage;

    private final String userModelType;

    private final EventHandler<UserModelAdaptationReRankEvent> userModelAdaptationReRankEventHandler = new EventHandler<UserModelAdaptationReRankEvent>();

    public DirectedUserModelAdapter(
            Persistence persistence,
            Ranker ranker,
            String userModelType,
            UserToUserInterestSelector userToUserInterestRetriever,
            boolean useWeightedAverage,
            boolean keepStatistics) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (ranker == null) {
            throw new IllegalArgumentException("ranker cannot be null.");
        }
        if (userToUserInterestRetriever == null) {
            throw new IllegalArgumentException("userToUserInterestRetriever cannot be null.");
        }
        if (userModelType == null) {
            throw new IllegalArgumentException("userModelType cannot be null.");
        }
        this.persistence = persistence;
        this.ranker = ranker;
        this.userToUserInterestRetriever = userToUserInterestRetriever;

        if (keepStatistics) {
            descriptiveStatistics = new DescriptiveStatistics();
        } else {
            descriptiveStatistics = null;
        }
        this.useWeightedAverage = useWeightedAverage;
        this.userModelType = userModelType;

    }

    public DirectedUserModelAdapter(
            Persistence persistence,
            Ranker ranker,
            UserToUserInterestSelector userToUserInterestRetriever) {
        this(
                persistence,
                ranker,
                UserModel.DEFAULT_USER_MODEL_TYPE,
                userToUserInterestRetriever,
                true,
                false);
    }

    private ValueAggregator createNewValueAggregator() {
        return useWeightedAverage ? new IncrementalWeightedAverage() : new MaxValueAggregator();
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

        // 2. Find user models to adapt from that contain the missing user terms.
        Collection<UserModel> userModels = persistence
                .getUsersWithUserModel(termsToAdapt, userModelType);
        Map<String, UserModel> userToUserModels = new HashMap<String, UserModel>();
        Collection<String> users = new HashSet<String>();
        for (UserModel userModel : userModels) {
            users.add(userModel.getUser().getGlobalId());
            userToUserModels.put(userModel.getUser().getGlobalId(), userModel);
        }

        String messageGroupGlobalId = message.getMessageGroupGlobalId();

        // 3. Compute user similarity between the u and the users of the found user models.

        Collection<UserScore> userToUserInterestScores = this.userToUserInterestRetriever
                .getUserToUserInterest(message.getUserGlobalId(), messageGroupGlobalId, users);

        // 4. If the user similarity satisfies a threshold u take the weight of the term and apply
        // it to UMu. If there are more weights available use a weighted average of the term weights
        // and the user similarity

        if (userToUserInterestScores != null && userToUserInterestScores.size() > 0) {
            // there are similarities to use

            Map<Term, ValueAggregator> statsForTermsToAdaptFrom = new HashMap<Term, ValueAggregator>();
            for (Term term : termsToAdapt) {
                statsForTermsToAdaptFrom.put(term, createNewValueAggregator());
            }

            for (UserScore userScore : userToUserInterestScores) {

                UserModel userModel = userToUserModels.get(userScore.getUserGlobalId());
                if (userModel != null) {

                    Map<Term, UserModelEntry> entries = persistence.getUserModelEntriesForTerms(
                            userModel, termsToAdapt);
                    for (Entry<Term, UserModelEntry> entry : entries.entrySet()) {
                        ValueAggregator stat = statsForTermsToAdaptFrom.get(entry
                                .getKey());
                        integrateStat(stat, userScore, entry.getValue());
                    }
                }
            }

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
                                            message.getUserGlobalId(),
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

            persistence.storeOrUpdateUserModelEntries(userModelToAdapt, entries.values());

            Message messageToRerate = this.persistence.getMessageByGlobalId(message.getMessageId());

            MessageFeatureContext messageFeatureContextOfReRank = ranker.rerank(messageToRerate,
                    message.getUserGlobalId());

            if (this.userModelAdaptationReRankEventHandler.getEventListenersSize() > 0) {
                UserModelAdaptationReRankEvent reRankEvent = new UserModelAdaptationReRankEvent();
                reRankEvent.setMessageFeatureContextOfReRank(messageFeatureContextOfReRank);
                reRankEvent.setAdaptationMessage(message);
                this.userModelAdaptationReRankEventHandler.fire(reRankEvent);
            }
        }

    }

    public long getAdaptedCount() {
        return adaptedCount;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName()
                + " userToUserInterestRetriever="
                + this.userToUserInterestRetriever.getConfigurationDescription();
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

    public EventHandler<UserModelAdaptationReRankEvent> getUserModelAdaptationReRankEventHandler() {
        return userModelAdaptationReRankEventHandler;
    }

    private void integrateStat(ValueAggregator stat, UserScore userScore,
            UserModelEntry value) {
        stat.add(value.getScoredTerm().getWeight(), userScore.getScore());
    }

    @Override
    public boolean supports(CommunicationMessage message) {
        return message instanceof DirectedUserModelAdaptationMessage;
    }

}
