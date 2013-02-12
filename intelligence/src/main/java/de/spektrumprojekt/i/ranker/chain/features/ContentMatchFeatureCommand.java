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
import java.util.Map;
import java.util.Map.Entry;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.datamodel.MessageFeature;
import de.spektrumprojekt.i.informationextraction.frequency.TermFrequencyComputer;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.persistence.Persistence;

/**
 * Computes the matching of terms between the message and the user model
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class ContentMatchFeatureCommand implements
        Command<UserSpecificMessageFeatureContext> {

    public enum TermWeightAggregation {
        MAX,
        /**
         * User
         */
        AVG,

        COSINUS;
    }

    public enum TermWeightStrategy {
        /** just a weight of 1 */
        NONE,
        LINEAR_INVERSE_TERM_FREQUENCY,
        INVERSE_TERM_FREQUENCY;
    }

    private final TermWeightAggregation termWeightAggregation;

    private final Persistence persistence;

    private final float interestTermTreshold;

    private final TermWeightStrategy termWeightStrategy;

    private TermFrequencyComputer termFrequencyComputer;

    /**
     * 
     * @param persistence
     *            the persistence
     */
    public ContentMatchFeatureCommand(Persistence persistence,
            TermFrequencyComputer termFrequencyComputer,
            TermWeightAggregation termWeightAggregation,
            TermWeightStrategy termWeightStrategy,
            float interestTermTreshold) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (termFrequencyComputer == null) {
            throw new IllegalArgumentException("termFrequencyComputer cannot be null.");
        }
        if (termWeightAggregation == null) {
            throw new IllegalArgumentException("termWeightAggregation cannot be null.");
        }
        if (termWeightStrategy == null) {
            throw new IllegalArgumentException("termWeightStrategy cannot be null.");
        }
        this.persistence = persistence;
        this.termFrequencyComputer = termFrequencyComputer;
        this.interestTermTreshold = interestTermTreshold;
        this.termWeightAggregation = termWeightAggregation;
        this.termWeightStrategy = termWeightStrategy;
    }

    private float determineTermWeight(Message message, Term term) {
        String messageGroupId = message.getMessageGroup() == null ? null : message
                .getMessageGroup().getGlobalId();
        float weight;
        switch (this.termWeightStrategy) {
        case NONE:
            weight = 1;
            break;
        case LINEAR_INVERSE_TERM_FREQUENCY:
            float numMessageWithTerm = termFrequencyComputer.getMessageCount(messageGroupId);
            if (term.getCount() == 0) {
                throw new RuntimeException("No! term.count cannot be 0! " + term);
            }
            if (numMessageWithTerm == 0) {
                throw new RuntimeException("No! numMessageWithTerm cannot be 0! " + term);
            }
            weight = 1 - term.getCount() / numMessageWithTerm;
            break;
        case INVERSE_TERM_FREQUENCY:
            float log = 1;
            float numMessageWithTerm2 = termFrequencyComputer.getMessageCount(messageGroupId);
            if (term.getCount() == 0) {
                throw new RuntimeException("No! term.count cannot be 0! " + term);
            }
            if (numMessageWithTerm2 == 0) {
                throw new RuntimeException("No! numMessageWithTerm cannot be 0! " + term);
            }
            log = 1 + numMessageWithTerm2 / term.getCount();
            weight = (float) Math.log(log);
            break;
        default:
            throw new RuntimeException(this.termWeightStrategy + " is not supported.");
        }
        return weight;
    }

    private float getAverage(Message message, Map<Term, UserModelEntry> relevantEntries,
            Collection<Term> terms) {
        float sumTop = 0;
        float sumBottom = 0;
        for (Term term : terms) {
            UserModelEntry entry = relevantEntries.get(term);
            float entryScore = 0;
            if (entry != null) {
                entryScore = entry.getScoredTerm().getWeight();
            }
            float termWeight = determineTermWeight(message, term);

            sumTop += termWeight * entryScore;
            sumBottom += termWeight;
        }

        return sumTop / sumBottom;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName()
                + " interestTermTreshold=" + interestTermTreshold
                + " termWeightAggregation=" + termWeightAggregation
                + " termWeightStrategy=" + termWeightStrategy;
    }

    private float getCosinusSimilarity(Message message, Map<Term, UserModelEntry> relevantEntries,
            Collection<Term> terms) {
        float sumTop = 0;
        float squareSum1 = 0;
        float squareSum2 = 0;
        for (Term term : terms) {
            UserModelEntry entry = relevantEntries.get(term);
            float entryScore = 0;
            if (entry != null) {
                entryScore = entry.getScoredTerm().getWeight();
            }
            float termWeight = determineTermWeight(message, term);

            sumTop += termWeight * entryScore;
            squareSum1 += entryScore * entryScore;
            squareSum2 += termWeight * termWeight;
        }

        if (squareSum1 + squareSum2 == 0) {
            return 0;
        }
        return (float) (sumTop / Math.sqrt(squareSum1 * squareSum2));
    }

    /**
     * 
     * @return the feature id
     */
    public Feature getFeatureId() {
        return Feature.CONTENT_MATCH_FEATURE;
    }

    private float getMax(Message message, Map<Term, UserModelEntry> relevantEntries,
            Collection<Term> terms) {
        float max = 0;
        for (Entry<Term, UserModelEntry> entry : relevantEntries.entrySet()) {
            float termWeight = determineTermWeight(message, entry.getKey());

            float entryScore = 0;
            if (entry.getValue() != null) {
                entryScore = entry.getValue().getScoredTerm().getWeight();
            }

            max = Math.max(max, termWeight * entryScore);

        }
        return max;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(UserSpecificMessageFeatureContext context) {

        UserModel userModel = persistence.getOrCreateUserModelByUser(context.getUserGlobalId());

        Collection<Term> messageTerms = MessageHelper.getAllTerms(context.getMessage());
        Map<Term, UserModelEntry> entries = persistence.getUserModelEntriesForTerms(userModel,
                messageTerms);

        context.setMatchingUserModelEntries(entries);

        if (entries != null && entries.size() > 0) {
            MessageFeature feature = new MessageFeature(getFeatureId());

            switch (termWeightAggregation) {
            case COSINUS:
                feature.setValue(getCosinusSimilarity(context.getMessage(), entries, messageTerms));
                break;
            case AVG:
                feature.setValue(getAverage(context.getMessage(), entries, messageTerms));
                break;
            case MAX:
                feature.setValue(getMax(context.getMessage(), entries, messageTerms));
                break;
            }

            feature.setValue(Math.min(1, feature.getValue()));
            feature.setValue(Math.max(0, feature.getValue()));

            context.addMessageFeature(feature);
        }

    }
}
