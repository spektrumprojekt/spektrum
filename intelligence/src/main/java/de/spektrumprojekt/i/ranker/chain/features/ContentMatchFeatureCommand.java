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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.datamodel.MessageFeature;
import de.spektrumprojekt.i.ranker.RankerConfiguration;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.i.term.similarity.TermVectorSimilarityComputer;
import de.spektrumprojekt.i.timebased.MaxMergeValuesStrategy;
import de.spektrumprojekt.i.timebased.MergeValuesStrategy;
import de.spektrumprojekt.i.timebased.WeightedMergeValuesStrategy;
import de.spektrumprojekt.persistence.Persistence;

/**
 * Computes the matching of terms between the message and the user model
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class ContentMatchFeatureCommand implements Command<UserSpecificMessageFeatureContext> {

    private final Persistence persistence;
    private final TermVectorSimilarityComputer termVectorSimilarityComputer;
    private final float interestTermTreshold;
    private final RankerConfiguration rankerConfiguration;
    private final MergeValuesStrategy valuesStrategy;

    /**
     * 
     * @param persistence
     *            the persistence
     */
    public ContentMatchFeatureCommand(
            Persistence persistence,
            TermVectorSimilarityComputer termVectorSimilarityComputer,
            float interestTermTreshold,
            RankerConfiguration rankerConfiguration) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (termVectorSimilarityComputer == null) {
            throw new IllegalArgumentException("termVectorSimilarityComputer cannot be null.");
        }
        if (rankerConfiguration == null) {
            throw new IllegalArgumentException("userModelType cannot be null.");
        }
        this.persistence = persistence;
        this.interestTermTreshold = interestTermTreshold;
        this.termVectorSimilarityComputer = termVectorSimilarityComputer;
        this.rankerConfiguration = rankerConfiguration;

        de.spektrumprojekt.i.timebased.config.MergeValuesStrategy mergeValuesStrategy = rankerConfiguration
                .getShortTermMemoryConfiguration() == null ? de.spektrumprojekt.i.timebased.config.MergeValuesStrategy.MAX
                : rankerConfiguration.getShortTermMemoryConfiguration().getMergeValuesStrategy();

        switch (mergeValuesStrategy) {
        case MAX:
            valuesStrategy = new MaxMergeValuesStrategy();
            break;
        default:
            valuesStrategy = new WeightedMergeValuesStrategy(
                    rankerConfiguration.getShortTermMemoryConfiguration());
            break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + "userModelType="
                + rankerConfiguration.getUserModelTypes().keySet()
                + " valuesStrategy=" + valuesStrategy
                + " termVectorSimilarityComputer="
                + termVectorSimilarityComputer.getConfigurationDescription()
                + " interestTermTreshold=" + interestTermTreshold;
    }

    /**
     * 
     * @return the feature id
     */
    public Feature getFeatureId() {
        return Feature.CONTENT_MATCH_FEATURE;
    }

    private boolean notNullOrEmpty(Map<?, ?> entries) {
        return entries != null && entries.size() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(UserSpecificMessageFeatureContext context) {

        List<UserModel> userModels = new LinkedList<UserModel>();
        for (String userModelType : rankerConfiguration.getUserModelTypes().keySet()) {
            userModels.add(persistence.getOrCreateUserModelByUser(context.getUserGlobalId(),
                    userModelType));
        }
        String messageGroupId = context.getMessage().getMessageGroup() == null ? null : context
                .getMessage().getMessageGroup().getGlobalId();

        Collection<Term> messageTerms = MessageHelper.getAllTerms(context.getMessage());
        Float value = null;

        Map<String, Map<Term, UserModelEntry>> allEntries = new HashMap<String, Map<Term, UserModelEntry>>();
        if (rankerConfiguration.isMixMemoriesForRating()) {

            for (UserModel userModel : userModels) {
                allEntries.put(userModel.getUserModelType(),
                        persistence.getUserModelEntriesForTerms(userModel, messageTerms));
            }
            // computes the score by first merging different values per term and then aggregate the
            // scores
            value = this.termVectorSimilarityComputer.getSimilarity(messageGroupId, allEntries,
                    valuesStrategy, messageTerms);

        } else {

            // map from user model type to the content match score
            Map<String, Float> values = new HashMap<String, Float>();
            for (UserModel userModel : userModels) {
                Map<Term, UserModelEntry> entries = persistence.getUserModelEntriesForTerms(
                        userModel, messageTerms);
                if (notNullOrEmpty(entries)) {
                    values.put(userModel.getUserModelType(), this.termVectorSimilarityComputer
                            .getSimilarity(messageGroupId, entries, messageTerms));
                }
                allEntries.put(userModel.getUserModelType(), entries);
            }
            // merge the scores of the different user models into one value
            value = valuesStrategy.merge(values);
        }
        context.setMatchingUserModelEntries(allEntries);

        if (value != null) {
            MessageFeature feature = new MessageFeature(getFeatureId());

            feature.setValue(value);

            feature.setValue(Math.min(1, feature.getValue()));
            feature.setValue(Math.max(0, feature.getValue()));

            context.addMessageFeature(feature);
        }

    }
}
