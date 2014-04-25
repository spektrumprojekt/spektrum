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

package de.spektrumprojekt.i.scorer.chain.features;

import java.util.HashSet;
import java.util.Set;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.datamodel.MessageFeature;
import de.spektrumprojekt.i.scorer.MessageFeatureContext;
import de.spektrumprojekt.i.scorer.feature.Feature;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;

/**
 * Compute the message specific features that are not user dependent
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class MessageSpecificFeaturesCommand implements Command<MessageFeatureContext> {

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
    public void process(MessageFeatureContext context) {
        /*
         * if (!InformationExtractionCommand.isInformationExtractionExecuted(context.getMessage()))
         * { throw new IllegalStateException("InformationExtraction has not been executed. context="
         * + context); }
         */

        MessageFeature messageRootFeature = new MessageFeature(Feature.MESSAGE_ROOT_FEATURE);

        Property parentId = MessageHelper.getParentMessage(context.getMessage());
        if (parentId == null || parentId.getPropertyValue() == null) {

            // it is a root message
            messageRootFeature.setValue(1f);

        }

        int msgLength = 0;
        int cleanedTextLength = 0;
        int numTerms = 0;
        for (InformationExtractionContext iec : context.getInformationExtractionContexts()) {

            if (iec.getCleanText() != null) {
                cleanedTextLength += iec.getCleanText().length();
            }
            if (iec.getMessagePart().getScoredTerms() != null) {
                numTerms += iec.getMessagePart().getScoredTerms().size();
            }
            if (iec.getMessagePart().getContent() != null) {
                msgLength += iec.getMessagePart().getContent().length();
            }
        }

        int numMentions = MessageHelper.getMentions(context.getMessage()).size();
        int numLikes = MessageHelper.getUserLikes(context.getMessage()).size();
        int numDis = context.getMessageRelation() == null ? 0 : context.getMessageRelation()
                .getNumberOfRelatedMessages();
        int numTags = MessageHelper.getTags(context.getMessage()).size();

        Set<String> authors = new HashSet<String>();
        authors.add(context.getMessage().getAuthorGlobalId());
        Set<String> mentions = new HashSet<String>(MessageHelper.getMentions(context.getMessage()));
        Set<String> tags = new HashSet<String>(MessageHelper.getTags(context.getMessage()));
        for (Message related : context.getMessagesOfRelation().values()) {
            authors.add(related.getAuthorGlobalId());
            mentions.addAll(MessageHelper.getMentions(related));
            tags.addAll(MessageHelper.getTags(related));
        }

        int numAttach = 0;
        int numImageAttach = 0;
        for (MessagePart mp : context.getMessage().getMessageParts()) {
            if (mp.isAttachment()) {
                numAttach++;
            }
            if (mp.isImageAttachment()) {
                numImageAttach++;
            }
        }

        context.addMessageFeature(messageRootFeature);
        context.addMessageFeature(Feature.MESSAGE_TEXT_LENGTH_FEATURE, msgLength);
        context.addMessageFeature(Feature.CLEANED_TEXT_LENGTH_FEATURE, cleanedTextLength);
        context.addMessageFeature(Feature.NUM_TERMS_FEATURE, numTerms);
        context.addMessageFeature(Feature.NUM_MENTIONS_FEATURE, numMentions);
        context.addMessageFeature(Feature.NUM_LIKES_FEATURE, numLikes);
        context.addMessageFeature(Feature.NUM_TAGS_FEATURE, numTags);
        context.addMessageFeature(Feature.NUM_DISCUSSION_FEATURE, numDis);
        context.addMessageFeature(Feature.NUM_AUTHORS_FEATURE, authors.size());
        context.addMessageFeature(Feature.NUM_MENTIONS_FEATURE, mentions.size());
        context.addMessageFeature(Feature.NUM_DISCUSSION_TAGS_FEATURE, tags.size());
        context.addMessageFeature(Feature.NUM_ATTACHMENTS_FEATURE, numAttach);
        context.addMessageFeature(Feature.NUM_IMAGE_ATTACHMENTS_FEATURE, numImageAttach);
        context.addMessageFeature(Feature.NUM_NONE_IMAGE_ATTACHMENTS_FEATURE, numAttach
                - numImageAttach);
    }
}
