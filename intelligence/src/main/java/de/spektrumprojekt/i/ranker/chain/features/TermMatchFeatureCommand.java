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

package de.spektrumprojekt.i.ranker.chain.features;

import java.util.Map;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.datamodel.MessageFeature;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.persistence.Persistence;

/**
 * Computes the matching of terms between the message and the user model
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class TermMatchFeatureCommand implements
        Command<UserSpecificMessageFeatureContext> {

    public enum TermWeightAggregation {
        MAX,
        /**
         * User
         */
        AVG;
    }

    private final TermWeightAggregation termWeightAggregation;

    private final Persistence persistence;

    private final float interestTermTreshold;

    /**
     * 
     * @param persistence
     *            the persistence
     */
    public TermMatchFeatureCommand(Persistence persistence,
            TermWeightAggregation termWeightAggregation, float interestTermTreshold) {
        this.persistence = persistence;
        this.interestTermTreshold = interestTermTreshold;
        this.termWeightAggregation = termWeightAggregation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " interestTermTreshold=" + interestTermTreshold
                + " termWeightAggregation=" + termWeightAggregation;
    }

    /**
     * 
     * @return the feature id
     */
    public Feature getFeatureId() {
        return Feature.TERM_MATCH_FEATURE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(UserSpecificMessageFeatureContext context) {

        UserModel userModel = persistence.getOrCreateUserModelByUser(context.getUserGlobalId());

        Map<Term, UserModelEntry> entries = persistence.getUserModelEntriesForTerms(userModel,
                MessageHelper.getAllTerms(context.getMessage()));

        context.setMatchingUserModelEntries(entries);

        if (entries != null && entries.size() > 0) {
            MessageFeature feature = new MessageFeature(getFeatureId());
            float maxRank = 0;
            float sum = 0;
            for (UserModelEntry entry : entries.values()) {
                maxRank = Math.max(maxRank, entry.getScoredTerm().getWeight());
                sum += entry.getScoredTerm().getWeight();
            }
            switch (termWeightAggregation) {
            case AVG:
                if (entries.size() > 0) {
                    feature.setValue(sum / entries.size());
                }
                break;
            case MAX:
                feature.setValue(maxRank);
                break;
            }

            context.addMessageFeature(feature);
        }

    }
}
