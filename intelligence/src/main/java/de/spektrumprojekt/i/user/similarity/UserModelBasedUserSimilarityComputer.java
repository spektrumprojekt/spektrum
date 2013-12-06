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

package de.spektrumprojekt.i.user.similarity;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.datamodel.user.UserSimilarity;
import de.spektrumprojekt.i.term.similarity.TermVectorSimilarityComputer;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.simple.SimplePersistence;

public class UserModelBasedUserSimilarityComputer implements UserSimilarityComputer {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(UserModelBasedUserSimilarityComputer.class);

    private final SimplePersistence persistence;

    private final boolean holdComputedSimilarites;

    private Collection<UserSimilarity> userSimilarities;

    private final TermVectorSimilarityComputer termVectorSimilarityComputer;

    public UserModelBasedUserSimilarityComputer(Persistence persistence,
            TermVectorSimilarityComputer termVectorSimilarityComputer) {
        this(persistence, termVectorSimilarityComputer, false);
    }

    /**
     * 
     * @param persistence
     * @param holdComputedSimilarites
     *            true to keep the user similarities in this class after computation
     */
    public UserModelBasedUserSimilarityComputer(
            Persistence persistence,
            TermVectorSimilarityComputer termVectorSimilarityComputer,
            boolean holdComputedSimilarites) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (termVectorSimilarityComputer == null) {
            throw new IllegalArgumentException("termVectorSimilarityComputer cannot be null.");
        }
        this.persistence = (SimplePersistence) persistence;
        this.termVectorSimilarityComputer = termVectorSimilarityComputer;
        this.holdComputedSimilarites = holdComputedSimilarites;
    }

    private Map<Term, UserModelEntry> filterByMG(Map<Term, UserModelEntry> entryMap,
            MessageGroup mg) {
        Map<Term, UserModelEntry> filtered = new HashMap<Term, UserModelEntry>();

        Long mgId = mg == null ? null : mg.getId();
        for (Entry<Term, UserModelEntry> entry : entryMap.entrySet()) {
            Long termMgId = entry.getKey().getMessageGroupId();
            if (mgId == termMgId || mgId != null && mgId.equals(termMgId)) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    public Collection<UserSimilarity> getUserSimilarities() {
        if (!this.holdComputedSimilarites) {
            throw new IllegalStateException(
                    "holdComputedSimilarites is false, therefore similarities are not stored!");
        }
        return userSimilarities;
    }

    @Override
    public void run() {

        Collection<UserSimilarity> userSimilarities = new HashSet<UserSimilarity>();

        // get a list of all topics
        Collection<MessageGroup> messageGroups = persistence.getAllMessageGroups();

        Map<UserModel, Collection<UserModelEntry>> userModelEntries = persistence
                .getAllUserModelEntries(UserModel.DEFAULT_USER_MODEL_TYPE);

        for (Entry<UserModel, Collection<UserModelEntry>> userModel1 : userModelEntries.entrySet()) {
            User user1 = userModel1.getKey().getUser();
            Map<Term, UserModelEntry> entryMap1 = UserModelEntry.createTermToEntryMap(userModel1
                    .getValue());
            userModel2: for (Entry<UserModel, Collection<UserModelEntry>> userModel2 : userModelEntries
                    .entrySet()) {
                if (userModel1.equals(userModel2)) {
                    continue userModel2;
                }
                User user2 = userModel2.getKey().getUser();
                Map<Term, UserModelEntry> entryMap2 = UserModelEntry
                        .createTermToEntryMap(userModel2.getValue());

                for (MessageGroup mg : messageGroups) {

                    Map<Term, UserModelEntry> filtered1 = filterByMG(entryMap1, mg);
                    Map<Term, UserModelEntry> filtered2 = filterByMG(entryMap2, mg);

                    float sim = termVectorSimilarityComputer.getSimilarity(filtered1, filtered2);
                    sim = Math.min(1, sim);
                    sim = Math.max(0, sim);

                    UserSimilarity userSimilarity = new UserSimilarity(user1.getGlobalId(),
                            user2.getGlobalId(), mg.getGlobalId());
                    userSimilarity.setSimilarity(sim);

                    userSimilarities.add(userSimilarity);
                }

            }
        }

        persistence.deleteAndCreateUserSimilarities(userSimilarities);

        if (this.holdComputedSimilarites) {
            this.userSimilarities = userSimilarities;
        }

    }
}
