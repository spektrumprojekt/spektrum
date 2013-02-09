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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.observation.Interest;
import de.spektrumprojekt.datamodel.observation.Observation;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
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

    private Observation getObservationForDisintegration(LearnerMessageContext context,
            Message message) {

        List<Observation> relatedObservations = context.getRelatedObservations();
        Observation observationForDisintegration = null;

        // the observation the highest priority is picked, since this is the one what was integrated
        for (Observation related : relatedObservations) {
            if (observationForDisintegration == null) {
                observationForDisintegration = related;
            } else if (related.getPriority().priorityValue() >= observationForDisintegration
                    .getPriority().priorityValue()) {
                observationForDisintegration = related;
            }
        }

        if (observationForDisintegration != null) {

            if (!observationForDisintegration.getMessageGlobalId().equals(message.getGlobalId())) {
                throw new IllegalStateException(
                        "messageGlobalId of observation does not match! observation="
                                + observationForDisintegration
                                + " message=" + message);
            }
            if (!observationForDisintegration.getUserGlobalId().equals(
                    context.getObservation().getUserGlobalId())) {
                throw new IllegalStateException(
                        "userGlobalId of observation does not match! observation="
                                + observationForDisintegration
                                + " message=" + message);
            }
            if (!observationForDisintegration.getObservationType().equals(
                    context.getObservation().getObservationType())) {
                throw new IllegalStateException(
                        "observationType of observation does not match! observation1="
                                + observationForDisintegration
                                + " context.observation=" + context.getObservation());
            }

        }

        return observationForDisintegration;
    }

    private Map<Term, ScoredTerm> getScoredTermsOfMessage(Message message) {
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
        return scoredTermsOfMessage;
    }

    @Override
    public void process(LearnerMessageContext context) {

        // TODO check that the message has been learned before for the user
        // TODO this means storing the observations, checking for some

        Message message = context.getMessage();

        String userToLearnForGlobalId = context.getObservation().getUserGlobalId();
        Interest interest = context.getObservation().getInterest();

        if (interest == null) {
            // TODO generate a interest if not yet available
            throw new UnsupportedOperationException(
                    "Not yet implemented: 'generate a interest if not yet available' ");
        }
        UserModel userModel = persistence.getOrCreateUserModelByUser(
                userToLearnForGlobalId);

        Observation observationForDisintegration = getObservationForDisintegration(context,
                message);

        if (observationForDisintegration != null
                && (observationForDisintegration.getInterest().equals(interest)
                || context.getObservation().getPriority().priorityValue() < observationForDisintegration
                        .getPriority().priorityValue()
                )) {
            // nothing to do here. we got an observation with the same interest or the observation
            // has a lower priority as one we used before
            return;
        }

        Map<Term, ScoredTerm> scoredTermsOfMessage = getScoredTermsOfMessage(message);

        Collection<Term> terms = new HashSet<Term>(scoredTermsOfMessage.keySet());
        Map<Term, UserModelEntry> entries = context.getPersistence()
                .getUserModelEntriesForTerms(userModel,
                        scoredTermsOfMessage.keySet());

        Collection<Term> entriesToRemove = new HashSet<Term>();
        for (Entry<Term, UserModelEntry> entry : entries.entrySet()) {
            if (entry.getValue() != null) {
                if (observationForDisintegration != null) {
                    userModelEntryIntegrationStrategy.disintegrate(
                            entry.getValue(),
                            observationForDisintegration.getInterest(),
                            scoredTermsOfMessage.get(entry.getKey()),
                            observationForDisintegration.getObservationDate());
                }
                boolean remove = userModelEntryIntegrationStrategy.integrate(
                        entry.getValue(),
                        interest,
                        scoredTermsOfMessage.get(entry.getKey()),
                        context.getObservation().getObservationDate());
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
