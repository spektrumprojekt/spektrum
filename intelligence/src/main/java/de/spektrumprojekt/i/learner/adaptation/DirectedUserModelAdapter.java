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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import de.spektrumprojekt.communication.CommunicationMessage;
import de.spektrumprojekt.communication.MessageHandler;
import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.datamodel.user.UserSimilarity;
import de.spektrumprojekt.persistence.Persistence;

public class DirectedUserModelAdapter implements
        MessageHandler<DirectedUserModelAdaptationMessage>, ConfigurationDescriptable {

    private double userSimilarityThreshold;

    private final Persistence persistence;

    public DirectedUserModelAdapter(Persistence persistence) {
        this.persistence = persistence;
    }

    @Override
    public void deliverMessage(DirectedUserModelAdaptationMessage message) throws Exception {
        UserModel userModelToAdapt = this.persistence.getOrCreateUserModelByUser(message
                .getUserGlobalId());

        // 1. Identify the terms of the messages that are not contained in the user model UMu.
        Collection<Term> termsToAdapt = Arrays.asList(message.getTermsToAdapt());

        // 2. Find user models to adapt from that contain the missing user terms.
        Collection<UserModel> userModels = persistence
                .getUsersWithUserModel(new ArrayList<Term>(termsToAdapt));
        Map<String, UserModel> userToUserModels = new HashMap<String, UserModel>();
        Collection<String> users = new HashSet<String>();
        for (UserModel userModel : userModels) {
            users.add(userModel.getGlobalId());
            userToUserModels.put(userModel.getGlobalId(), userModel);
        }

        String messageGroupGlobalId = null;

        // 3. Compute user similarity between the u and the users of the found user models.
        Collection<UserSimilarity> similarities = persistence.getUserSimilarities(
                message.getUserGlobalId(), users, messageGroupGlobalId, userSimilarityThreshold);

        // 4. If the user similarity satisfies a threshold u take the weight of the term and apply
        // it to UMu. If there are more weights available use a weighted average of the term weights
        // and the user similarity

        if (similarities != null && similarities.size() > 0) {
            // there are similarities to use

            Map<Term, IncrementalWeightedAverage> stats = new HashMap<Term, IncrementalWeightedAverage>();
            for (Term term : termsToAdapt) {
                stats.put(term, new IncrementalWeightedAverage());
            }

            for (UserSimilarity userSimilarity : similarities) {
                assert userSimilarity.getUserGlobalIdFrom().equals(message.getUserGlobalId());

                UserModel userModel = userToUserModels.get(userSimilarity.getUserGlobalIdTo());
                if (userModel != null) {

                    Map<Term, UserModelEntry> entries = persistence.getUserModelEntriesForTerms(
                            userModel, termsToAdapt);
                    for (Entry<Term, UserModelEntry> entry : entries.entrySet()) {
                        IncrementalWeightedAverage stat = stats.get(entry.getKey());
                        integrateStat(stat, userSimilarity, entry.getValue());
                    }

                }
            }

            Map<Term, UserModelEntry> entries = persistence.getUserModelEntriesForTerms(
                    userModelToAdapt, termsToAdapt);

            for (Entry<Term, IncrementalWeightedAverage> statEntry : stats.entrySet()) {
                if (statEntry.getValue().getValue() > 0) {
                    // adapt
                    UserModelEntry entry = entries.get(statEntry.getKey());
                    if (entry == null) {
                        ScoredTerm scoredTerm = new ScoredTerm(statEntry.getKey(), 0);
                        entry = new UserModelEntry(userModelToAdapt, scoredTerm);
                    }
                    entry.getScoredTerm().setWeight((float) statEntry.getValue().getValue());
                    entry.setAdapted(true);
                }
            }

            persistence.storeOrUpdateUserModelEntries(userModelToAdapt, entries.values());

        }

    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " userSimilarityThreshold="
                + userSimilarityThreshold;
    }

    @Override
    public Class<DirectedUserModelAdaptationMessage> getMessageClass() {
        return DirectedUserModelAdaptationMessage.class;
    }

    private void integrateStat(IncrementalWeightedAverage stat, UserSimilarity userSimilarity,
            UserModelEntry value) {
        stat.add(value.getScoredTerm().getWeight(), userSimilarity.getSimilarity());
    }

    @Override
    public boolean supports(CommunicationMessage message) {
        return message instanceof DirectedUserModelAdaptationMessage;
    }

}
