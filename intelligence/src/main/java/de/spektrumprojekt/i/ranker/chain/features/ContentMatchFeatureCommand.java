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
import de.spektrumprojekt.i.learner.contentbased.UserModelEntryIntegrationStrategy;
import de.spektrumprojekt.i.ranker.ScorerConfiguration;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.i.ranker.feature.Feature;
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
    private final ScorerConfiguration scorerConfiguration;
    private final MergeValuesStrategy valuesStrategy;
    private boolean useAdaptedEntries;

    private Map<String, UserModelEntryIntegrationStrategy> userModelEntryIntegrationStrategies;

    public ContentMatchFeatureCommand(
            Persistence persistence,
            TermVectorSimilarityComputer termVectorSimilarityComputer,
            ScorerConfiguration scorerConfiguration) {
        this(
                persistence,
                termVectorSimilarityComputer,
                scorerConfiguration,
                !scorerConfiguration.getUserModelAdapterConfiguration()
                        .isOnlyUseAdaptedTermsForRescoring());
    }

    /**
     * 
     * @param persistence
     *            the persistence
     */
    public ContentMatchFeatureCommand(
            Persistence persistence,
            TermVectorSimilarityComputer termVectorSimilarityComputer,
            ScorerConfiguration scorerConfiguration,
            boolean useAdaptedEntries) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (termVectorSimilarityComputer == null) {
            throw new IllegalArgumentException("termVectorSimilarityComputer cannot be null.");
        }
        if (scorerConfiguration == null) {
            throw new IllegalArgumentException("userModelType cannot be null.");
        }
        this.persistence = persistence;
        this.termVectorSimilarityComputer = termVectorSimilarityComputer;
        this.scorerConfiguration = scorerConfiguration;
        this.useAdaptedEntries = useAdaptedEntries;

        de.spektrumprojekt.i.timebased.config.MergeValuesStrategy mergeValuesStrategy = scorerConfiguration
                .getShortTermMemoryConfiguration() == null ? de.spektrumprojekt.i.timebased.config.MergeValuesStrategy.MAX
                : scorerConfiguration.getShortTermMemoryConfiguration().getMergeValuesStrategy();

        switch (mergeValuesStrategy) {
        case MAX:
            valuesStrategy = new MaxMergeValuesStrategy();
            break;
        default:
            valuesStrategy = new WeightedMergeValuesStrategy(
                    scorerConfiguration.getShortTermMemoryConfiguration());
            break;
        }

    }

    private boolean containsAdaptedEntries(Map<String, Map<Term, UserModelEntry>> allEntries) {
        for (Map<Term, UserModelEntry> entries : allEntries.values()) {
            for (UserModelEntry entry : entries.values()) {
                if (entry.isAdapted()) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map<Term, UserModelEntry> getAndFilterUserModelEntries(
            Collection<Term> messageTerms,
            UserModel userModel) {

        Map<Term, UserModelEntry> entries = persistence.getUserModelEntriesForTerms(userModel,
                messageTerms);
        UserModelEntryIntegrationStrategy userModelEntryIntegrationStrategy = userModelEntryIntegrationStrategies
                .get(userModel.getUserModelType());
        if (userModelEntryIntegrationStrategy == null) {
            throw new IllegalStateException("No userModelEntryIntegrationStrategy for "
                    + userModel.getUserModelType());
        }
        if (!useAdaptedEntries) {
            entries = UserModelEntry.filteredForNonAdaptedEntries(entries);
        }
        entries = userModelEntryIntegrationStrategy.cleanUpEntries(entries);

        return entries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + "userModelType="
                + scorerConfiguration.getUserModelConfigurations().keySet()
                + " valuesStrategy=" + valuesStrategy
                + " termVectorSimilarityComputer="
                + termVectorSimilarityComputer.getConfigurationDescription()
                + " useAdaptedEntries=" + useAdaptedEntries;
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
        for (String userModelType : scorerConfiguration.getUserModelConfigurations().keySet()) {
            userModels.add(persistence.getOrCreateUserModelByUser(context.getUserGlobalId(),
                    userModelType));
        }
        String messageGroupId = context.getMessage().getMessageGroup() == null ? null : context
                .getMessage().getMessageGroup().getGlobalId();

        Collection<Term> messageTerms = MessageHelper.getAllTerms(context.getMessage());
        Float value = null;

        Map<String, Map<Term, UserModelEntry>> allEntries = new HashMap<String, Map<Term, UserModelEntry>>();
        if (scorerConfiguration.isMixMemoriesForRating()) {

            for (UserModel userModel : userModels) {
                Map<Term, UserModelEntry> userModelEntriesForTerms = getAndFilterUserModelEntries(
                        messageTerms, userModel);
                allEntries.put(userModel.getUserModelType(),
                        userModelEntriesForTerms);
            }
            // computes the score by first merging different values per term and then aggregate the
            // scores
            value = this.termVectorSimilarityComputer.getSimilarity(messageGroupId, allEntries,
                    valuesStrategy, messageTerms);

        } else {

            // map from user model type to the content match score
            Map<String, Float> values = new HashMap<String, Float>();
            for (UserModel userModel : userModels) {
                Map<Term, UserModelEntry> entries = getAndFilterUserModelEntries(messageTerms,
                        userModel);
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

        if (this.containsAdaptedEntries(allEntries)) {
            context.setMatchingUserModelEntriesContainsAdapted(true);
        }
        if (value != null) {
            MessageFeature feature = new MessageFeature(getFeatureId());

            feature.setValue(value);

            feature.setValue(Math.min(1, feature.getValue()));
            feature.setValue(Math.max(0, feature.getValue()));

            context.addMessageFeature(feature);

        }

    }

    public void setUserModelEntryIntegrationStrategies(
            Map<String, UserModelEntryIntegrationStrategy> userModelEntryIntegrationStrategies) {
        this.userModelEntryIntegrationStrategies = userModelEntryIntegrationStrategies;
    }
}
