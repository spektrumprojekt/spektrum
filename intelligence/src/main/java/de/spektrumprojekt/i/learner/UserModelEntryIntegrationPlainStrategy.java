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

package de.spektrumprojekt.i.learner;

import java.util.Date;

import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.observation.Interest;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class UserModelEntryIntegrationPlainStrategy implements
        UserModelEntryIntegrationStrategy {

    // the score defines the minimum value of a scored term so that it counts
    private float minScore = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public UserModelEntry createNew(UserModel userModel, Interest interest, ScoredTerm scoredTerm,
            Date oberservationDate) {
        Term term = scoredTerm.getTerm();
        ScoredTerm entryTerm = new ScoredTerm(term, 0);
        UserModelEntry entry = new UserModelEntry(userModel, entryTerm);
        // since it just counts it up and sets the score, just call integrate
        boolean shouldRemove = this.integrate(entry, interest, entryTerm, oberservationDate);
        if (shouldRemove) {
            return null;
        }
        return entry;
    }

    @Override
    public boolean disintegrate(UserModelEntry entry, Interest interest, ScoredTerm scoredTerm,
            Date oberservationDate) {

        return this.updateEntry(entry, -1 * interest.getScore(), scoredTerm);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " minScore=" + minScore;
    }

    /**
     * 
     * @return the minimum score to use for integrate the term
     */
    public float getMinScore() {
        return minScore;
    }

    /**
     * well isn't it weird, we don't even use the score of the term to compute the relevance
     * 
     * TODO COP: configuration optimization point: use the score of term as well, or in case of
     * another user model entry, use the term score as weight to increase / decrease the value
     * 
     * {@inheritDoc}
     */
    @Override
    public boolean integrate(UserModelEntry entry, Interest interest, ScoredTerm scoredTerm,
            Date obersvationDate) {

        return updateEntry(entry, interest.getScore(), scoredTerm);
    }

    /**
     * 
     * @param minScore
     *            the minimum score to use for integrate the term
     */
    public void setMinScore(float minScore) {
        this.minScore = minScore;
    }

    private boolean updateEntry(UserModelEntry entry, float interestScore, ScoredTerm scoredTerm) {
        if (scoredTerm.getWeight() >= minScore) {
            // TODO must be synchronized on this entry, several updates would be problematic

            if (interestScore >= 0) {
                entry.setScoreCount(entry.getScoreCount() + 1);
                entry.setAdapted(false);
            } else {
                entry.setScoreCount(entry.getScoreCount() - 1);
            }
            entry.setScoreSum(entry.getScoreSum() + interestScore);

            entry.consolidate();
        }
        return !(entry.getScoreCount() > 0);
    }
}
