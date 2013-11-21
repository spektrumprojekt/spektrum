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

package de.spektrumprojekt.i.ranker.chain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.datamodel.message.UserMessageScore;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.learner.adaptation.DirectedUserModelAdaptationMessage;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;

/**
 * Command for computing the message rank out of the features
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class TriggerUserModelAdaptationCommand implements
        Command<UserSpecificMessageFeatureContext> {

    private final float rankThreshold; // => interest term?
    private final float confidenceThreshold;

    private final String userModelType;
    private final Communicator communicator;

    public TriggerUserModelAdaptationCommand(Communicator communicator) {
        this(communicator, UserModel.DEFAULT_USER_MODEL_TYPE, 0.75f, 0.75f);
    }

    public TriggerUserModelAdaptationCommand(Communicator communicator, String userModelType,
            float confidenceThreshold,
            float rankThreshold) {
        if (communicator == null) {
            throw new IllegalArgumentException("communicator cannot be null!");
        }
        if (userModelType == null) {
            throw new IllegalArgumentException("userModelType cannot be null!");
        }
        this.communicator = communicator;
        this.userModelType = userModelType;
        this.confidenceThreshold = confidenceThreshold;
        this.rankThreshold = rankThreshold;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName()
                + " userModelType=" + userModelType
                + " rankThreshold=" + rankThreshold
                + " confidenceThreshold=" + confidenceThreshold;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(UserSpecificMessageFeatureContext context) {

        UserMessageScore messageRank = context.getMessageScore();

        if (messageRank.getScore() < rankThreshold) {

            String messageGroupGlobalId = context.getMessage().getMessageGroup().getGlobalId();

            if (messageGroupGlobalId != null
                    && context.getMatchingUserModelEntries() != null
                    && context.getMatchingUserModelEntries().get(this.userModelType) != null) {

                Collection<Term> allMessageTerms = MessageHelper.getAllTerms(context.getMessage());
                Collection<Term> matchingTermsOfUserModelNoIncludedInOthers = new HashSet<Term>();

                Collection<String> allTermsOfAllUserModels = new HashSet<String>();
                Collection<String> termsOfOtherUserModels = new HashSet<String>();
                for (Entry<String, Map<Term, UserModelEntry>> matchingTermsEntry : context
                        .getMatchingUserModelEntries().entrySet()) {
                    if (!this.userModelType.equals(matchingTermsEntry.getKey())) {
                        for (Term term : matchingTermsEntry.getValue().keySet()) {
                            termsOfOtherUserModels.add(term.getValue());
                        }
                    }
                }
                allTermsOfAllUserModels.addAll(termsOfOtherUserModels);
                for (Term term : context
                        .getMatchingUserModelEntries().get(this.userModelType).keySet()) {
                    if (!termsOfOtherUserModels.contains(term.getValue())) {
                        matchingTermsOfUserModelNoIncludedInOthers.add(term);
                        allTermsOfAllUserModels.add(term.getValue());
                    }
                }

                int messageTerms = allMessageTerms.size();
                int allMatchingUserModelTerms = allTermsOfAllUserModels.size();

                float confidence = messageTerms == 1 ? 0 : (float) allMatchingUserModelTerms
                        / messageTerms;

                if (confidence < confidenceThreshold) {

                    List<Term> termsToAdapt = new ArrayList<Term>();
                    for (Term termToAdaptCandidate : allMessageTerms) {
                        boolean match = false;
                        match: for (Term termToMatchCheck : matchingTermsOfUserModelNoIncludedInOthers) {
                            if (termToAdaptCandidate.getValue().equals(termToMatchCheck.getValue())) {
                                match = true;
                                break match;
                            }
                        }
                        if (!match) {
                            termsToAdapt.add(termToAdaptCandidate);
                        }
                    }

                    Term[] termsToAdaptArray = termsToAdapt.toArray(new Term[termsToAdapt
                            .size()]);

                    if (termsToAdaptArray.length > 0) {
                        DirectedUserModelAdaptationMessage adaptationMessage = new DirectedUserModelAdaptationMessage(
                                context.getUserGlobalId(),
                                context.getMessage().getGlobalId(),
                                messageGroupGlobalId,
                                termsToAdaptArray,
                                messageRank);
                        communicator.sendMessage(adaptationMessage);
                    }
                }
            }

        }

    }
}
