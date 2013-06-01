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

import java.util.HashSet;
import java.util.Set;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.user.UserSimilarity;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;
import de.spektrumprojekt.persistence.Persistence;

/**
 * Command feature to compute the auhtor feature
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class FeatureAggregateCommand implements Command<UserSpecificMessageFeatureContext> {

    private final Persistence persistence;

    public FeatureAggregateCommand(Persistence persistence) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence is null.");
        }
        this.persistence = persistence;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(UserSpecificMessageFeatureContext context) {

        MessageFeatureContext messageFeatureContext = context.getMessageFeatureContext();

        String userGlobalId = context.getUserGlobalId();
        int msgLength = 0;
        int cleanedTextLength = 0;
        int numTerms = 0;
        for (InformationExtractionContext iec : messageFeatureContext
                .getInformationExtractionContexts()) {
            msgLength += iec.getMessagePart().getContent().length();
            cleanedTextLength += iec.getCleanText().length();
            numTerms += iec.getMessagePart().getScoredTerms().size();
        }
        UserSimilarity simTo = persistence.getUserSimilarity(context.getMessage()
                .getAuthorGlobalId(), userGlobalId, context.getMessage().getMessageGroup()
                .getGlobalId());
        UserSimilarity simFrom = persistence.getUserSimilarity(userGlobalId, context.getMessage()
                .getAuthorGlobalId(), context.getMessage().getMessageGroup().getGlobalId());

        double to = simTo == null ? 0 : simTo.getSimilarity();
        double from = simFrom == null ? 0 : simFrom.getSimilarity();

        int numMentions = MessageHelper.getMentions(context.getMessage()).size();
        int numLikes = MessageHelper.getUserLikes(context.getMessage()).size();
        int numDis = context.getMessageRelation() == null ? 0 : context.getMessageRelation()
                .getNumberOfRelatedMessages();
        int numTags = MessageHelper.getTags(context.getMessage()).size();

        Set<String> authors = new HashSet<String>();
        authors.add(context.getMessage().getAuthorGlobalId());
        Set<String> mentions = new HashSet<String>(MessageHelper.getMentions(context.getMessage()));
        Set<String> tags = new HashSet<String>(MessageHelper.getTags(context.getMessage()));
        for (Message related : messageFeatureContext.getMessagesOfRelation().values()) {
            authors.add(related.getAuthorGlobalId());
            mentions.addAll(MessageHelper.getMentions(related));
            tags.addAll(MessageHelper.getTags(related));
        }

        FeatureAggregate featureAggregate = new FeatureAggregate();

        featureAggregate.features.putAll(messageFeatureContext.getFeaturesForUser(userGlobalId));
        featureAggregate.messageTextLength = msgLength;
        featureAggregate.cleanedTextLength = cleanedTextLength;
        featureAggregate.numTerms = numTerms;
        featureAggregate.userToSim = to;
        featureAggregate.userFromSim = from;
        featureAggregate.numMentions = numMentions;
        featureAggregate.numLikes = numLikes;
        featureAggregate.numTags = numTags;
        featureAggregate.numDiscussion = numDis;
        featureAggregate.numAuthors = authors.size();
        featureAggregate.numDiscussionMentions = mentions.size();
        featureAggregate.numDiscussionTags = tags.size();

        context.setFeatureAggregate(featureAggregate);
    }
}
