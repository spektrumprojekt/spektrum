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

package de.spektrumprojekt.i.learner.chain;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.i.learner.Interest;
import de.spektrumprojekt.i.learner.LearnerMessageContext;
import de.spektrumprojekt.i.learner.UserModelEntryIntegrationStrategy;
import de.spektrumprojekt.persistence.Persistence;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class UserModelLearnerCommand implements Command<LearnerMessageContext> {

    private final UserModelEntryIntegrationStrategy userModelEntryIntegrationStrategy;
    private final Persistence persistence;

    public UserModelLearnerCommand(Persistence persistence,
            UserModelEntryIntegrationStrategy userModelEntryIntegrationStrategy) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (userModelEntryIntegrationStrategy == null) {
            throw new IllegalArgumentException(
                    "userModelEntryIntegrationStrategy cannot be null.");
        }

        this.persistence = persistence;
        this.userModelEntryIntegrationStrategy = userModelEntryIntegrationStrategy;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + ": userModelEntryIntegrationStrategy="
                + userModelEntryIntegrationStrategy.getConfigurationDescription();
    }

    @Override
    public void process(LearnerMessageContext context) {

        // TODO check that the message has been learned before for the user
        // TODO this means storing the observations, checking for some

        Message message = context.getMessage();
        String userToLearnForGlobalId = context.getUserToLearnForGlobalId();
        Interest interest = context.getInterest();

        UserModel userModel = persistence.getOrCreateUserModelByUser(
                userToLearnForGlobalId);

        Map<Term, ScoredTerm> scoredTermsOfMessage = new HashMap<Term, ScoredTerm>();
        for (MessagePart messagePart : message.getMessageParts()) {
            // in case the message parts have the same terms, use the max value of it
            for (ScoredTerm scoredTerm : messagePart.getScoredTerms()) {
                ScoredTerm exists = scoredTermsOfMessage.get(scoredTerm.getTerm());
                ScoredTerm use = scoredTerm;
                if (exists != null) {
                    use = new ScoredTerm(exists.getTerm(), Math.max(exists.getWeight(),
                            use.getWeight()));
                }
                scoredTermsOfMessage.put(use.getTerm(), use);
            }
        }

        Collection<Term> terms = new HashSet<Term>(scoredTermsOfMessage.keySet());
        Map<Term, UserModelEntry> entries = context.getPersistence()
                .getUserModelEntriesForTerms(userModel,
                        scoredTermsOfMessage.keySet());

        Collection<Term> entriesToRemove = new HashSet<Term>();
        for (Entry<Term, UserModelEntry> entry : entries.entrySet()) {
            if (entry.getValue() != null) {
                boolean remove = userModelEntryIntegrationStrategy.integrate(entry.getValue(),
                        interest,
                        scoredTermsOfMessage.get(entry.getKey()), message.getPublicationDate());
                if (remove) {
                    entriesToRemove.add(entry.getKey());
                }
                terms.remove(entry.getKey());
            }
        }
        for (Term t : entriesToRemove) {
            entries.remove(t);
        }
        // terms not know so far are left
        for (Term t : terms) {
            UserModelEntry entry = userModelEntryIntegrationStrategy.createNew(userModel,
                    interest,
                    scoredTermsOfMessage.get(t), message.getPublicationDate());
            if (entry != null) {
                entries.put(t, entry);
            }
        }

        persistence.storeOrUpdateUserModelEntries(userModel, entries.values());

    }

}
