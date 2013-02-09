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

package de.spektrumprojekt.i;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.observation.Interest;
import de.spektrumprojekt.datamodel.observation.Observation;
import de.spektrumprojekt.datamodel.observation.Observation.ObservationPriority;
import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.i.informationextraction.InformationExtractionCommand;
import de.spektrumprojekt.i.learner.Learner;
import de.spektrumprojekt.i.learner.LearningMessage;
import de.spektrumprojekt.i.learner.UserModelEntryIntegrationPlainStrategy;
import de.spektrumprojekt.i.learner.UserModelEntryIntegrationStrategy;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;

/**
 * Test for the {@link Learner}
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class LearnerTest extends MyStreamTest {

    private final String user1ToLearnForGlobalId = "userToLearnFor1";
    private final String user2ToLearnForGlobalId = "userToLearnFor2";

    private UserModelEntryIntegrationStrategy userModelEntryIntegrationStrategy;

    /**
     * 
     * @throws ConfigurationException
     *             in case of an error
     */
    @Before
    public void beforeTest() throws ConfigurationException {
        setupPersistence();

        this.userModelEntryIntegrationStrategy = new UserModelEntryIntegrationPlainStrategy();
    }

    private void checkObservations(String userGlobalId, Message message, int expectedSize) {
        Collection<Observation> observations = getPersistence().getObservations(userGlobalId,
                message.getGlobalId(), ObservationType.RATING);
        Assert.assertNotNull(observations);
        Assert.assertEquals(expectedSize, observations.size());
    }

    /**
     * Check the user model entries
     * 
     * @param message
     *            the message
     * @param userGlobalId
     *            the users id
     * @param terms
     *            the terms user model entries must exist for
     * @param value
     *            the value those entry should have
     */
    private void checkUserModelEntries(Message message, String userGlobalId,
            Collection<Term> terms, float value) {
        // get user model entries for scored terms above

        UserModel userModel = getPersistence().getOrCreateUserModelByUser(userGlobalId);
        Map<Term, UserModelEntry> entries = getPersistence().getUserModelEntriesForTerms(userModel,
                terms);

        Assert.assertEquals("must have exactly as many entries as terms.", entries.size(),
                terms.size());

        for (UserModelEntry entry : entries.values()) {
            Assert.assertNotNull(entry);
            Assert.assertEquals(value, entry.getScoredTerm().getWeight(), 0.0001);
        }
    }

    /**
     * TODO move to some general accessible class ?
     * 
     * @param message
     *            the message
     * @return all terms of the message
     */
    public Collection<Term> getTermsOfMessage(Message message) {
        Collection<Term> terms = new HashSet<Term>();
        for (MessagePart part : message.getMessageParts()) {
            for (ScoredTerm scoredTerm : part.getScoredTerms()) {
                terms.add(scoredTerm.getTerm());
            }
        }
        return terms;
    }

    /**
     * Test the learner
     * 
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testLearner() throws Exception {

        Message message = createPlainTextMessage(
                "Test Content. This is some plain old test content with nothing spectacular in it.",
                null, null);
        message = getPersistence().storeMessage(message);

        // extract the terms
        InformationExtractionCommand<MessageFeatureContext> ieCommand = InformationExtractionCommand
                .createDefaultGermanEnglish(getPersistence(), null, true, false);
        MessageFeatureContext context = new MessageFeatureContext(getPersistence(), message, null);
        ieCommand.process(context);
        checkScoredTerms(context);

        Collection<Term> terms = getTermsOfMessage(message);
        Assert.assertTrue("must have some terms.", terms.size() > 0);

        Learner learner = new Learner(getPersistence(), ieCommand,
                userModelEntryIntegrationStrategy);

        // learning an extreme=1 interest
        Observation observation = new Observation(user1ToLearnForGlobalId, message.getGlobalId(),
                ObservationType.RATING, ObservationPriority.USER_FEEDBACK, null, new Date(),
                Interest.EXTREME);
        LearningMessage learningMessage = new LearningMessage(observation);
        learner.deliverMessage(learningMessage);
        checkUserModelEntries(message, user1ToLearnForGlobalId, terms, 1.0f);
        checkObservations(user1ToLearnForGlobalId, message, 1);

        // learning an high=0.75 interest for a new user
        observation = new Observation(user2ToLearnForGlobalId, message.getGlobalId(),
                ObservationType.RATING, ObservationPriority.USER_FEEDBACK, null, new Date(),
                Interest.HIGH);
        learningMessage = new LearningMessage(observation);
        learner.deliverMessage(learningMessage);
        checkUserModelEntries(message, user2ToLearnForGlobalId, terms, 0.75f);
        checkObservations(user2ToLearnForGlobalId, message, 1);

        // learning a low=0.25 interest for a user 1 again
        observation = new Observation(user1ToLearnForGlobalId, message.getGlobalId(),
                ObservationType.RATING, ObservationPriority.USER_FEEDBACK, null, new Date(),
                Interest.LOW);
        learningMessage = new LearningMessage(observation);
        learner.deliverMessage(learningMessage);
        checkUserModelEntries(message, user1ToLearnForGlobalId, terms, 0.25f);
        checkObservations(user1ToLearnForGlobalId, message, 2);

        // learning a high=0.75 interest for a user 1 again but with a lower priority, hence the
        // result should not change to the last one
        observation = new Observation(user1ToLearnForGlobalId, message.getGlobalId(),
                ObservationType.RATING, ObservationPriority.SECOND_LEVEL_FEATURE_INFERRED, null,
                new Date(), Interest.HIGH);
        learningMessage = new LearningMessage(observation);
        learner.deliverMessage(learningMessage);
        checkUserModelEntries(message, user1ToLearnForGlobalId, terms, 0.25f);
        checkObservations(user1ToLearnForGlobalId, message, 3);

    }
}
