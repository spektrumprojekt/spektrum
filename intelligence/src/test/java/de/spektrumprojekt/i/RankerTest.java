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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.spektrumprojekt.callbacks.SimpleMessageGroupMemberRunner;
import de.spektrumprojekt.communication.CommunicationMessage;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.communication.vm.VirtualMachineCommunicator;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageRank;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.datamodel.user.UserSimilarity;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.learner.Learner;
import de.spektrumprojekt.i.learner.UserModelEntryIntegrationPlainStrategy;
import de.spektrumprojekt.i.learner.adaptation.DirectedUserModelAdapter;
import de.spektrumprojekt.i.learner.similarity.UserSimilarityComputer;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;
import de.spektrumprojekt.i.ranker.Ranker;
import de.spektrumprojekt.i.ranker.RankerConfiguration;
import de.spektrumprojekt.i.ranker.RankerConfigurationFlag;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.i.term.TermVectorSimilarityStrategy;
import de.spektrumprojekt.i.term.TermWeightStrategy;

/**
 * Test the ranker
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class RankerTest extends IntelligenceSpektrumTest {

    private MessageGroup messageGroup;
    private MessageRelation messageRelation;

    private final List<UserModel> userModels = new ArrayList<UserModel>();

    private Communicator communicator;
    private Queue<CommunicationMessage> rankerQueue;

    private void checkUserModelTerms(MessageFeatureContext context, UserModel... userModelsToAssert) {
        Collection<ScoredTerm> terms = context.getMessage().getMessageParts().iterator().next()
                .getScoredTerms();

        for (ScoredTerm term : terms) {

            Collection<UserModel> userModels = getPersistence().getUsersWithUserModel(
                    Arrays.asList(term.getTerm()));

            for (UserModel userModelToAssert : userModelsToAssert) {
                Assert.assertTrue(
                        "Must have UserModel " + userModelToAssert + " with term " + term.getTerm()
                                + "!", userModels.contains(userModelToAssert));
            }

        }
    }

    private void dump(Collection<UserSimilarity> similarities) {
        for (UserSimilarity sim : similarities) {
            System.out.println(sim);
        }
    }

    private void runSimilarityComputerAndCheck(User user1, User user2) {
        UserSimilarityComputer computer = new UserSimilarityComputer(getPersistence());
        Collection<UserSimilarity> similarities = computer.run();

        dump(similarities);

        // assert that there is a user2 -> user1 similarity
        similarities = getPersistence().getUserSimilarities(
                user2.getGlobalId(), Arrays.asList(user1.getGlobalId()),
                messageGroup.getGlobalId(), 0);
        UserSimilarity user1sim = null;
        for (UserSimilarity sim : similarities) {
            if (sim.getUserGlobalIdTo().equals(user1.getGlobalId())) {
                user1sim = sim;
            }
        }

        Assert.assertNotNull(user1sim);
        Assert.assertTrue("User Similarity " + user1sim.getSimilarity() + " should be > 0.5 ",
                user1sim.getSimilarity() > 0.25);
    }

    private Ranker setupRanker(RankerConfigurationFlag flags) {

        Collection<String> userForRanking = new HashSet<String>();
        userForRanking.add("user1");
        userForRanking.add("user2");
        userForRanking.add("user3");

        for (String globalId : userForRanking) {
            UserModel userModel = getPersistence().getOrCreateUserModelByUser(globalId);
            Assert.assertNotNull(userModel);
            userModels.add(userModel);
        }

        rankerQueue = new LinkedBlockingQueue<CommunicationMessage>();

        communicator = new VirtualMachineCommunicator(rankerQueue, rankerQueue);

        RankerConfiguration rankerConfiguration = new RankerConfiguration(
                TermWeightStrategy.TRIVIAL,
                TermVectorSimilarityStrategy.AVG, flags);

        Ranker ranker = new Ranker(
                getPersistence(),
                communicator,
                new SimpleMessageGroupMemberRunner<MessageFeatureContext>(userForRanking),
                rankerConfiguration);

        Learner learner = new Learner(
                getPersistence(),
                ranker.getInformationExtractionChain(),
                new UserModelEntryIntegrationPlainStrategy());

        communicator.registerMessageHandler(ranker);
        communicator.registerMessageHandler(learner);

        if (rankerConfiguration.hasFlag(RankerConfigurationFlag.USE_DIRECTED_USER_MODEL_ADAPTATION)) {
            DirectedUserModelAdapter adapter = new DirectedUserModelAdapter(getPersistence(),
                    ranker);
            communicator.registerMessageHandler(adapter);
        }

        communicator.open();

        return ranker;

    }

    @Before
    public void setupTest() throws ConfigurationException {
        setupPersistence();
        messageGroup = getPersistence().getMessageGroupByGlobalId("messageGroup");
        if (messageGroup == null) {
            messageGroup = getPersistence().storeMessageGroup(new MessageGroup("messageGroup"));
        }
    }

    /**
     * Test
     * 
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testRanker() throws Exception {
        Ranker ranker = setupRanker(null);
        String authorGlobalId = getPersistence().getOrCreateUser("user1").getGlobalId();

        Message message = createPlainTextMessage(
                "Test Content. This is some plain old test content with nothing spectacular in it.",
                authorGlobalId, messageGroup);

        // first we rank only to get the stemmed tokens (and by the way its testing empty user
        // models
        MessageFeatureContext context = ranker.rank(message, messageRelation, null, false);
        checkScoredTerms(context);

        MessagePart messagePart = context.getMessage().getMessageParts().iterator().next();

        int i = 0;
        for (UserModel userModel : userModels) {
            Collection<UserModelEntry> changedEntries = new HashSet<UserModelEntry>();

            for (ScoredTerm t : messagePart.getScoredTerms()) {
                // if "i" is even than all terms have rank 0 otherwise rank 1
                ScoredTerm scoredTerm = new ScoredTerm(t.getTerm(), i % 2);
                UserModelEntry entry = new UserModelEntry(userModel, scoredTerm);
                changedEntries.add(entry);
            }
            i++;
            getPersistence().storeOrUpdateUserModelEntries(userModel, changedEntries);
        }

        context = ranker.rank(message, messageRelation, null, false);
        i = 0;
        for (UserModel userModel : userModels) {

            UserSpecificMessageFeatureContext userContext = context.getUserContext(userModel
                    .getUser().getGlobalId());
            Assert.assertNotNull("User context should exist for userGlobalId="
                    + userModel.getUser().getGlobalId(), userContext);
            Assert.assertNotNull(userContext.getMessageRank());
            Assert.assertEquals(userContext.getMessageRank().getRank(), i++ % 2, 0.001);

        }

    }

    /**
     * Test
     * 
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testRankerWithDirectedAdaptation() throws Exception {

        User user1 = getPersistence().getOrCreateUser("user1");
        User user2 = getPersistence().getOrCreateUser("user2");
        User user3 = getPersistence().getOrCreateUser("user3");

        UserModel userModel1 = getPersistence().getOrCreateUserModelByUser(user1.getGlobalId());
        UserModel userModel2 = getPersistence().getOrCreateUserModelByUser(user2.getGlobalId());
        UserModel userModel3 = getPersistence().getOrCreateUserModelByUser(user3.getGlobalId());

        Ranker ranker = setupRanker(RankerConfigurationFlag.USE_DIRECTED_USER_MODEL_ADAPTATION);

        // user2 mentions user1
        Message message = createPlainTextMessage(
                "Test Content. This is some plain old test content with nothing spectacular in it.",
                user2.getGlobalId(), messageGroup);
        message.addProperty(MessageHelper.createMentionProperty(Arrays.asList(user1.getGlobalId())));

        // 1st rank just the mention
        MessageFeatureContext context = ranker.rank(message, messageRelation, null, false);

        waitForCommunicatorToDelivierMessages();

        checkUserModelTerms(context, userModel1, userModel2);

        runSimilarityComputerAndCheck(user1, user2);

        // here user1 is mentioned again, that should learn the content into the profile
        message = createPlainTextMessage(
                "The art of software engineering is the master piece of the modern era.",
                user3.getGlobalId(), messageGroup);
        message.addProperty(MessageHelper.createMentionProperty(Arrays.asList(user1.getGlobalId())));

        // in the same moment the user model adaption should be triggered for user2, and the user
        // model should be adapted by the just learned items for user 1
        context = ranker.rank(message, messageRelation, null, false);

        waitForCommunicatorToDelivierMessages();

        checkUserModelTerms(context, userModel1, userModel3);

        // here user1 writes a message, and that should learn the words "software", and
        // "engineering" from user 1 and adapt it to user 2
        message = createPlainTextMessage(
                "Is software engineering the master piece ?",
                user1.getGlobalId(), messageGroup);

        // here again a rank should exist for user 2
        context = ranker.rank(message, messageRelation, null, false);

        waitForCommunicatorToDelivierMessages();
        checkUserModelTerms(context, userModel1);

        MessageRank rankForUser2 = getPersistence().getMessageRank(user2.getGlobalId(),
                message.getGlobalId());
        Assert.assertNotNull(rankForUser2);
        // Assert.assertTrue("rankForUser2 should positive if adaption run, but it is: "
        // + rankForUser2.getRank(), rankForUser2.getRank() > 0.5);

    }

    private void waitForCommunicatorToDelivierMessages() throws InterruptedException {
        int wait = 10;
        while (!rankerQueue.isEmpty()) {
            Thread.sleep(500);
            // wait--;
            if (wait == 0) {
                Assert.fail("Queue not empty after 5 seconds messagesLeft="
                        + rankerQueue.size());
            }
        }
    }
}
