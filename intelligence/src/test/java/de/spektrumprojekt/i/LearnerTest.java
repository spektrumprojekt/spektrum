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
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.i.informationextraction.InformationExtractionCommand;
import de.spektrumprojekt.i.learner.Interest;
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
            Assert.assertEquals(entry.getScoredTerm().getWeight(), value, 0.0001);
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

        // extract the terms
        InformationExtractionCommand<MessageFeatureContext> ieCommand = InformationExtractionCommand
                .createDefaultGermanEnglish(getPersistence(), true, false);
        MessageFeatureContext context = new MessageFeatureContext(getPersistence(), message, null);
        ieCommand.process(context);
        checkScoredTerms(context);

        Collection<Term> terms = getTermsOfMessage(message);
        Assert.assertTrue("must have some terms.", terms.size() > 0);

        Learner learner = new Learner(getPersistence(), userModelEntryIntegrationStrategy);

        // learning an exterme=1 interest
        LearningMessage learningMessage = new LearningMessage(message, user1ToLearnForGlobalId,
                Interest.EXTREME);
        learner.deliverMessage(learningMessage);
        checkUserModelEntries(message, user1ToLearnForGlobalId, terms, 1.0f);

        // learning an high=0.8 interest for a new user
        learningMessage = new LearningMessage(message, user2ToLearnForGlobalId,
                Interest.HIGH);
        learner.deliverMessage(learningMessage);
        checkUserModelEntries(message, user2ToLearnForGlobalId, terms, 0.75f);

        // learning a low=0.2 interest for a user 1 again
        // TODO actualy we have a problem here, it is not a new learning message, but a change,
        // since the user already learned for the message!
        learningMessage = new LearningMessage(message, user1ToLearnForGlobalId,
                Interest.LOW);
        learner.deliverMessage(learningMessage);
        checkUserModelEntries(message, user1ToLearnForGlobalId, terms, 0.625f);

    }
}