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

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.datamodel.MessageFeature;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.i.term.similarity.TermVectorSimilarityComputer;
import de.spektrumprojekt.persistence.Persistence;

/**
 * Computes the matching of terms between the message and the user model
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class ContentMatchFeatureCommand implements
        Command<UserSpecificMessageFeatureContext> {

    private final Persistence persistence;
    private final String userModelType;
    private final TermVectorSimilarityComputer termVectorSimilarityComputer;
    private final float interestTermTreshold;

    /**
     * 
     * @param persistence
     *            the persistence
     */
    public ContentMatchFeatureCommand(
            Persistence persistence,
            String userModelType,
            TermVectorSimilarityComputer termVectorSimilarityComputer,
            float interestTermTreshold) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (termVectorSimilarityComputer == null) {
            throw new IllegalArgumentException("termVectorSimilarityComputer cannot be null.");
        }
        if (userModelType == null) {
            throw new IllegalArgumentException("userModelType cannot be null.");
        }
        this.persistence = persistence;
        this.userModelType = userModelType;
        this.interestTermTreshold = interestTermTreshold;
        this.termVectorSimilarityComputer = termVectorSimilarityComputer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName()
                + "userModelType=" + userModelType
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(UserSpecificMessageFeatureContext context) {

        UserModel userModel = persistence.getOrCreateUserModelByUser(context.getUserGlobalId(),
                userModelType);

        Collection<Term> messageTerms = MessageHelper.getAllTerms(context.getMessage());
        Map<Term, UserModelEntry> entries = persistence.getUserModelEntriesForTerms(userModel,
                messageTerms);

        context.setMatchingUserModelEntries(entries);
        String messageGroupId = context.getMessage().getMessageGroup() == null ? null : context
                .getMessage()
                .getMessageGroup().getGlobalId();
        if (entries != null && entries.size() > 0) {
            MessageFeature feature = new MessageFeature(getFeatureId());

            float value = this.termVectorSimilarityComputer.getSimilarity(messageGroupId, entries,
                    messageTerms);
            feature.setValue(value);

            feature.setValue(Math.min(1, feature.getValue()));
            feature.setValue(Math.max(0, feature.getValue()));

            context.addMessageFeature(feature);
        }

    }

}
