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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.spektrumprojekt.datamodel.user.UserSimilarity;
import de.spektrumprojekt.persistence.Persistence;

public class UserSimilarityRetriever implements UserToUserInterestSelector {

    private final Persistence persistence;
    private final double userSimilarityThreshold;

    public UserSimilarityRetriever(Persistence persistence) {
        this(persistence, 0.1d);
    }

    public UserSimilarityRetriever(Persistence persistence, double userSimilarityThreshold) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (userSimilarityThreshold < 0 || userSimilarityThreshold > 1) {
            throw new IllegalArgumentException(
                    "userSimilarityThreshold must be in [0..1] but is: "
                            + userSimilarityThreshold);
        }
        this.persistence = persistence;
        this.userSimilarityThreshold = userSimilarityThreshold;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " userSimilarityThreshold="
                + userSimilarityThreshold;
    }

    @Override
    public List<UserScore> getUserToUserInterest(String userGlobalId,
            String messageGroupGlobalId, Collection<String> usersToConsider) {

        // 3. Compute user similarity between the u and the users of the found user models.
        Collection<UserSimilarity> similarities = persistence.getUserSimilarities(
                userGlobalId, usersToConsider, messageGroupGlobalId, userSimilarityThreshold);

        List<UserScore> scores = new ArrayList<UserScore>(
                similarities.size());
        for (UserSimilarity sim : similarities) {
            UserScore score = new UserScore(sim.getUserGlobalIdTo(), sim.getSimilarity());
            scores.add(score);
        }

        return scores;
    }
}