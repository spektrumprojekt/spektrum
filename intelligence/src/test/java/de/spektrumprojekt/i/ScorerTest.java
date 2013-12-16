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
import java.util.Collections;
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
import de.spektrumprojekt.communication.vm.VirtualMachineCommunicator;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.UserMessageScore;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.datamodel.user.UserSimilarity;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.learner.Learner;
import de.spektrumprojekt.i.learner.adaptation.DirectedUserModelAdapter;
import de.spektrumprojekt.i.learner.contentbased.UserModelConfiguration;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;
import de.spektrumprojekt.i.ranker.Scorer;
import de.spektrumprojekt.i.ranker.ScorerConfiguration;
import de.spektrumprojekt.i.ranker.ScorerConfigurationFlag;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.i.similarity.messagegroup.MessageGroupSimilarityConfiguration;
import de.spektrumprojekt.i.similarity.messagegroup.TermBasedMessageGroupSimilarityComputer;
import de.spektrumprojekt.i.similarity.user.InteractionBasedUserSimilarityComputer;
import de.spektrumprojekt.i.similarity.user.UserSimilarityRetriever;
import de.spektrumprojekt.i.similarity.user.UserSimilaritySimType;
import de.spektrumprojekt.i.term.TermVectorSimilarityStrategy;
import de.spektrumprojekt.i.term.TermWeightStrategy;
import de.spektrumprojekt.persistence.Persistence.MatchMode;

/**
 * Test the scorer
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class ScorerTest extends IntelligenceSpektrumTest {

    private MessageGroup messageGroup;
    private MessageRelation messageRelation;

    private final List<UserModel> userModels = new ArrayList<UserModel>();

    private VirtualMachineCommunicator communicator;
    private Queue<CommunicationMessage> rankerQueue;

    private DirectedUserModelAdapter adapter;

    private void checkUserModelTerms(MessageFeatureContext context, UserModel... userModelsToAssert) {
        Collection<ScoredTerm> terms = context.getMessage().getMessageParts().iterator().next()
                .getScoredTerms();

        for (ScoredTerm term : terms) {

            Collection<UserModel> userModels = getPersistence().getUsersWithUserModel(
                    Collections.singleton(term.getTerm()),
                    UserModel.DEFAULT_USER_MODEL_TYPE,
                    MatchMode.EXACT
                    );

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

    public MessageGroup getAndCreateMG(String messageGroupId) {
        MessageGroup messageGroup = getPersistence().getMessageGroupByGlobalId(messageGroupId);
        if (messageGroup == null) {
            messageGroup = getPersistence().storeMessageGroup(new MessageGroup(messageGroupId));
        }
        return messageGroup;
    }

    private void runSimilarityComputerAndCheck(User user1, User user2) {
        InteractionBasedUserSimilarityComputer computer = new InteractionBasedUserSimilarityComputer(
                getPersistence(),
                UserSimilaritySimType.VOODOO, true);
        computer.run();
        Collection<UserSimilarity> similarities = computer.getUserSimilarities();

        dump(similarities);

        // assert that there is a user2 -> user1 similarity
        similarities = getPersistence().getUserSimilarities(user2.getGlobalId(),
                Arrays.asList(user1.getGlobalId()), messageGroup.getGlobalId(), 0);
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

    private Scorer setupScorer(ScorerConfigurationFlag flag, boolean useAdaptionFromMGs,
            String... usersForRanking) {

        Collection<String> userForRanking = new HashSet<String>(Arrays.asList(usersForRanking));

        for (String globalId : userForRanking) {
            UserModel userModel = getPersistence().getOrCreateUserModelByUser(globalId,
                    UserModel.DEFAULT_USER_MODEL_TYPE);
            Assert.assertNotNull(userModel);
            userModels.add(userModel);
        }

        rankerQueue = new LinkedBlockingQueue<CommunicationMessage>();

        communicator = new VirtualMachineCommunicator(rankerQueue, rankerQueue);

        ScorerConfiguration scorerConfiguration = new ScorerConfiguration(
                TermWeightStrategy.TRIVIAL,
                TermVectorSimilarityStrategy.AVG,
                ScorerConfigurationFlag.USE_MESSAGE_GROUP_SPECIFIC_USER_MODEL
                );
        if (flag != null) {
            scorerConfiguration.addFlags(flag);
        }

        scorerConfiguration.put(UserModel.DEFAULT_USER_MODEL_TYPE,
                UserModelConfiguration.getUserModelConfigurationWithTermCountLearningStrategy());

        Scorer scorer = new Scorer(getPersistence(), communicator,
                new SimpleMessageGroupMemberRunner<MessageFeatureContext>(userForRanking),
                scorerConfiguration);

        Learner learner = new Learner(getPersistence(), scorerConfiguration,
                scorer.getInformationExtractionChain());

        communicator.registerMessageHandler(scorer);
        communicator.registerMessageHandler(learner);

        if (scorerConfiguration.hasFlag(ScorerConfigurationFlag.USE_DIRECTED_USER_MODEL_ADAPTATION)) {

            scorerConfiguration.getUserModelAdapterConfiguration().setAdaptFromMessageGroups(
                    useAdaptionFromMGs);
            scorerConfiguration.getUserModelAdapterConfiguration()
                    .setMessageGroupSimilarityConfiguration(
                            new MessageGroupSimilarityConfiguration());
            scorerConfiguration.getUserModelAdapterConfiguration()
                    .setMessageGroupSimilarityThreshold(0.1d);
            scorerConfiguration.getUserModelAdapterConfiguration()
                    .setUseWeightedAverageForAggregatingSimilarUsers(true);

            if (useAdaptionFromMGs) {

                adapter = new DirectedUserModelAdapter(
                        getPersistence(),
                        scorer,
                        new UserSimilarityRetriever(getPersistence()),
                        new TermBasedMessageGroupSimilarityComputer(getPersistence(),
                                scorerConfiguration.getUserModelAdapterConfiguration()
                                        .getMessageGroupSimilarityConfiguration()),
                        scorerConfiguration.getUserModelAdapterConfiguration());
            } else {
                adapter = new DirectedUserModelAdapter(
                        getPersistence(),
                        scorer,
                        new UserSimilarityRetriever(getPersistence()),
                        null,
                        scorerConfiguration.getUserModelAdapterConfiguration());
            }

            communicator.registerMessageHandler(adapter);

        }

        communicator.open();

        return scorer;

    }

    @Before
    public void setupTest() throws ConfigurationException {
        setupPersistence();
        messageGroup = getAndCreateMG("messageGroup");
    }

    /**
     * Test
     * 
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testRanker() throws Exception {
        Scorer ranker = setupScorer(null, false, "user1", "user2", "user3");
        String authorGlobalId = getPersistence().getOrCreateUser("user1").getGlobalId();

        Message message = createPlainTextMessage(
                "Test Content. This is some plain old test content with nothing spectacular in it.",
                authorGlobalId, messageGroup);

        // first we rank only to get the stemmed tokens (and by the way its testing empty user
        // models
        MessageFeatureContext context = ranker.score(message, messageRelation, null, false);
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

        context = ranker.score(message, messageRelation, null, false);
        i = 0;
        for (UserModel userModel : userModels) {

            UserSpecificMessageFeatureContext userContext = context.getUserContext(userModel
                    .getUser().getGlobalId());
            Assert.assertNotNull("User context should exist for userGlobalId="
                    + userModel.getUser().getGlobalId(), userContext);
            Assert.assertNotNull(userContext.getMessageScore());
            Assert.assertEquals(userContext.getMessageScore().getScore(), i++ % 2, 0.001);

        }

    }

    /**
     * Test
     * 
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testRankerWithDirectedAdaptationByMGs() throws Exception {

        User user1 = getPersistence().getOrCreateUser("userMg1");
        User user2 = getPersistence().getOrCreateUser("userMg2");

        MessageGroup messageGroup1 = getAndCreateMG("mg1");
        MessageGroup messageGroup2 = getAndCreateMG("mg2");

        UserModel userModel1 = getPersistence().getOrCreateUserModelByUser(user1.getGlobalId(),
                UserModel.DEFAULT_USER_MODEL_TYPE);
        UserModel userModel2 = getPersistence().getOrCreateUserModelByUser(user2.getGlobalId(),
                UserModel.DEFAULT_USER_MODEL_TYPE);

        Scorer ranker = setupScorer(ScorerConfigurationFlag.USE_DIRECTED_USER_MODEL_ADAPTATION,
                true, "userMg1", "userMg2");

        // user1 write ins mg1
        Message message = createPlainTextMessage(
                "Test Content. This is some plain old test content with nothing spectacular in it.",
                user1.getGlobalId(), messageGroup1);

        // 1st rank
        MessageFeatureContext context = ranker.score(message, messageRelation, null, false);

        waitForCommunicatorToDelivierMessages();

        checkUserModelTerms(context, userModel1);

        // here user1 writes a message to user 2
        message = createPlainTextMessage(
                "The art of software engineering is the master piece of the modern era.",
                user2.getGlobalId(), messageGroup1);
        message.addProperty(MessageHelper.createMentionProperty(Arrays.asList(user1.getGlobalId())));

        // in the same moment the user model adaption should be triggered for user2, and the user
        // model should be adapted by the just learned items for user 1
        context = ranker.score(message, messageRelation, null, false);

        waitForCommunicatorToDelivierMessages();

        checkUserModelTerms(context, userModel1, userModel2);

        // here user1 writes a message to another message group. For user 1 is the author feature to
        // match, but for user 2 the adaption should run.
        message = createPlainTextMessage("Is software engineering the master piece ?",
                user1.getGlobalId(), messageGroup2);

        // here again a rank should exist for user 2
        context = ranker.score(message, messageRelation, null, false);

        waitForCommunicatorToDelivierMessages();
        checkUserModelTerms(context, userModel1);

        UserMessageScore rankForUser2 = getPersistence().getMessageRank(user2.getGlobalId(),
                message.getGlobalId());
        Assert.assertNotNull(rankForUser2);
        Assert.assertTrue("RequestAdaptedCount should be > 0",
                this.adapter.getRequestAdaptedCount() > 0);
        Assert.assertTrue("getAdaptedCount should be > 0", this.adapter.getAdaptedCount() > 0);
        Assert.assertTrue("rankForUser2 should positive if adaption run, but it is: "
                + rankForUser2.getScore(), rankForUser2.getScore() > 0.5);

    }

    /**
     * Test
     * 
     * @throws Exception
     *             in case of an error
     */
    @Test
    public void testRankerWithDirectedAdaptationBySimilarUsers() throws Exception {

        User user1 = getPersistence().getOrCreateUser("user1");
        User user2 = getPersistence().getOrCreateUser("user2");
        User user3 = getPersistence().getOrCreateUser("user3");

        UserModel userModel1 = getPersistence().getOrCreateUserModelByUser(user1.getGlobalId(),
                UserModel.DEFAULT_USER_MODEL_TYPE);
        UserModel userModel2 = getPersistence().getOrCreateUserModelByUser(user2.getGlobalId(),
                UserModel.DEFAULT_USER_MODEL_TYPE);
        UserModel userModel3 = getPersistence().getOrCreateUserModelByUser(user3.getGlobalId(),
                UserModel.DEFAULT_USER_MODEL_TYPE);

        Scorer ranker = setupScorer(ScorerConfigurationFlag.USE_DIRECTED_USER_MODEL_ADAPTATION,
                false, "user1", "user2", "user3");

        // user2 mentions user1
        Message message = createPlainTextMessage(
                "Test Content. This is some plain old test content with nothing spectacular in it.",
                user2.getGlobalId(), messageGroup);
        message.addProperty(MessageHelper.createMentionProperty(Arrays.asList(user1.getGlobalId())));

        // 1st rank just the mention
        MessageFeatureContext context = ranker.score(message, messageRelation, null, false);

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
        context = ranker.score(message, messageRelation, null, false);

        waitForCommunicatorToDelivierMessages();

        checkUserModelTerms(context, userModel1, userModel3);

        // here user1 writes a message, and that should learn the words "software", and
        // "engineering" from user 1 and adapt it to user 2
        message = createPlainTextMessage("Is software engineering the master piece ?",
                user1.getGlobalId(), messageGroup);

        // here again a rank should exist for user 2
        context = ranker.score(message, messageRelation, null, false);

        waitForCommunicatorToDelivierMessages();
        checkUserModelTerms(context, userModel1);

        UserMessageScore rankForUser2 = getPersistence().getMessageRank(user2.getGlobalId(),
                message.getGlobalId());
        Assert.assertNotNull(rankForUser2);
        Assert.assertTrue("RequestAdaptedCount should be > 0",
                this.adapter.getRequestAdaptedCount() > 0);
        Assert.assertTrue("getAdaptedCount should be > 0", this.adapter.getAdaptedCount() > 0);
        Assert.assertTrue("rankForUser2 should positive if adaption run, but it is: "
                + rankForUser2.getScore(), rankForUser2.getScore() > 0.5);

    }

    private void waitForCommunicatorToDelivierMessages() throws InterruptedException {
        int wait = 10;
        while (!rankerQueue.isEmpty()) {
            Thread.sleep(500);
            // wait--;
            if (wait == 0) {
                Assert.fail("Queue not empty after 5 seconds messagesLeft=" + rankerQueue.size());
            }
        }

        if (this.communicator.hasErrors()) {
            Assert.fail("Communicator delievering has errors. Check the log. "
                    + this.communicator.getErrors());
        }
    }

}
