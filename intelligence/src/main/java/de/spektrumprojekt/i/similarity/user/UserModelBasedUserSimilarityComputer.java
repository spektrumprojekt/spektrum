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

package de.spektrumprojekt.i.similarity.user;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.time.TimeProviderHolder;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.datamodel.user.UserSimilarity;
import de.spektrumprojekt.i.learner.adaptation.UserModelBasedSimilarityConfiguration;
import de.spektrumprojekt.i.similarity.set.SetSimilarityResult;
import de.spektrumprojekt.i.term.similarity.TermVectorSimilarityComputer;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.simple.SimplePersistence;
import de.spektrumprojekt.persistence.simple.UserModelHolder;

public class UserModelBasedUserSimilarityComputer implements UserSimilarityComputer {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(UserModelBasedUserSimilarityComputer.class);

    private final SimplePersistence persistence;

    private final TermVectorSimilarityComputer termVectorSimilarityComputer;

    private UserModelBasedSimilarityConfiguration userModelBasedSimilarityConfiguration;

    /**
     * 
     * @param persistence
     * @param userModelBasedSimilarityConfiguration
     * @param termVectorSimilarityComputer
     */
    public UserModelBasedUserSimilarityComputer(
            Persistence persistence,
            UserModelBasedSimilarityConfiguration userModelBasedSimilarityConfiguration,
            TermVectorSimilarityComputer termVectorSimilarityComputer) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (userModelBasedSimilarityConfiguration == null) {
            throw new IllegalArgumentException(
                    "userModelBasedSimilarityConfiguration cannot be null.");
        }
        if (userModelBasedSimilarityConfiguration.getSetSimilarity() == null
                && termVectorSimilarityComputer == null) {
            throw new IllegalArgumentException(
                    "either setSimilarity or termVectorSimilarityComputer must be set. setSimilarity="
                            + userModelBasedSimilarityConfiguration.getSetSimilarity()
                            + " termVectorSimilarityComputer="
                            + termVectorSimilarityComputer);
        }
        if (userModelBasedSimilarityConfiguration.isReadUserSimilaritiesFromPrecomputedFile()
                && userModelBasedSimilarityConfiguration.isWriteUserSimilaritiesToPrecomputedFile()) {
            throw new IllegalArgumentException(
                    "Cannot read from and write to precomputed file to the same time. Set write to false to use precomputed.");
        }

        this.persistence = (SimplePersistence) persistence;
        this.userModelBasedSimilarityConfiguration = userModelBasedSimilarityConfiguration;
        this.termVectorSimilarityComputer = termVectorSimilarityComputer;
    }

    private Collection<UserSimilarity> computeUserSimilarities() {
        Collection<UserSimilarity> userSimilarities = new HashSet<UserSimilarity>();

        // get a list of all topics
        Collection<MessageGroup> messageGroups = persistence.getAllMessageGroups();

        Map<User, UserModelHolder> userModelEntries = persistence
                .getUserModelByTypeHolders(UserModel.DEFAULT_USER_MODEL_TYPE);

        user2Entries1: for (Entry<User, UserModelHolder> user2Entries1 : userModelEntries
                .entrySet()) {
            User user1 = user2Entries1.getKey();

            user2Entries2: for (Entry<User, UserModelHolder> user2Entries2 : userModelEntries
                    .entrySet()) {
                if (user2Entries1.equals(user2Entries2)) {
                    continue user2Entries2;
                }
                User user2 = user2Entries2.getKey();

                mg: for (MessageGroup mg : messageGroups) {

                    Map<Term, UserModelEntry> entries1 = user2Entries1.getValue()
                            .getUserModelEntriesByMessageGroupId(mg.getId());
                    Map<Term, UserModelEntry> entries2 = user2Entries2.getValue()
                            .getUserModelEntriesByMessageGroupId(mg.getId());

                    if (entries1 == null || entries2 == null) {
                        continue mg;
                    }

                    Map<Term, UserModelEntry> filtered1 = filter(entries1);
                    Map<Term, UserModelEntry> filtered2 = filter(entries2);

                    float sim;

                    if (userModelBasedSimilarityConfiguration.getSetSimilarity() != null) {
                        SetSimilarityResult result = userModelBasedSimilarityConfiguration
                                .getSetSimilarity().computeSimilarity(
                                        filtered1.keySet(),
                                        filtered2.keySet());
                        sim = result.getSim();
                    } else {
                        sim = termVectorSimilarityComputer.getSimilarity(filtered1, filtered2);
                    }
                    sim = Math.min(1, sim);
                    sim = Math.max(0, sim);

                    UserSimilarity userSimilarity = new UserSimilarity(user1.getGlobalId(),
                            user2.getGlobalId(), mg.getGlobalId());
                    userSimilarity.setSimilarity(sim);

                    userSimilarities.add(userSimilarity);
                }

            }
        }
        return userSimilarities;
    }

    private Map<Term, UserModelEntry> filter(Map<Term, UserModelEntry> entryMap) {
        Map<Term, UserModelEntry> filtered = new HashMap<Term, UserModelEntry>();

        for (Entry<Term, UserModelEntry> entry : entryMap.entrySet()) {
            if (!entry.getValue().isAdapted()) {
                filtered.put(entry.getKey(), entry.getValue());

            }
        }
        return filtered;
    }

    @Override
    public String getConfigurationDescription() {
        return this.toString();
    }

    private String getUserSimilarityDumpFilename() {
        String fname = userModelBasedSimilarityConfiguration
                .getPrecomputedUserSimilaritesFilename();

        if (userModelBasedSimilarityConfiguration.isPrecomputedIsWithDate()) {

            fname = fname.replace("TIME", "" + TimeProviderHolder.DEFAULT.getCurrentTime());
        }

        return fname;

    }

    private List<UserSimilarity> readPrecomputedUserSimilarites() throws IOException {
        String fname = getUserSimilarityDumpFilename();

        UserSimilarityOutput userSimilarityOutput = new UserSimilarityOutput();

        userSimilarityOutput.read(fname);

        LOGGER.info("Read {} userSims from {}", userSimilarityOutput.getElements().size(),
                fname);
        return userSimilarityOutput.getElements();
    }

    @Override
    public void run() throws IOException {

        Collection<UserSimilarity> userSimilarities;

        if (userModelBasedSimilarityConfiguration.isReadUserSimilaritiesFromPrecomputedFile()) {
            userSimilarities = readPrecomputedUserSimilarites();
        } else {
            userSimilarities = computeUserSimilarities();
        }

        persistence.deleteAndCreateUserSimilarities(userSimilarities);

        if (userModelBasedSimilarityConfiguration.isWriteUserSimilaritiesToPrecomputedFile()) {
            String fname = getUserSimilarityDumpFilename();

            UserSimilarityOutput userSimilarityOutput = new UserSimilarityOutput(this.persistence);
            userSimilarityOutput.getElements().addAll(userSimilarities);
            userSimilarityOutput.write(fname);

            LOGGER.info("Wrote {} user sims to {}.", userSimilarityOutput.getElements().size(),
                    getUserSimilarityDumpFilename());
        }

    }

    @Override
    public String toString() {
        return "UserModelBasedUserSimilarityComputer [persistence=" + persistence
                + ", termVectorSimilarityComputer=" + termVectorSimilarityComputer
                + ", userModelBasedSimilarityConfiguration="
                + userModelBasedSimilarityConfiguration + "]";
    }
}
