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
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.learner.adaptation.DirectedUserModelAdapter;
import de.spektrumprojekt.i.learner.similarity.UserSimilarityComputer;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;
import de.spektrumprojekt.i.ranker.Ranker;
import de.spektrumprojekt.i.ranker.Ranker.RankerConfigurationFlag;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;

/**
 * Test the ranker
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class RankerTest extends MyStreamTest {

    private MessageGroup messageGroup;
    private MessageRelation messageRelation;
    private String authorGlobalId;

    private final List<UserModel> userModels = new ArrayList<UserModel>();

    private Ranker setupRanker(RankerConfigurationFlag flags) {

        Collection<String> userForRanking = new HashSet<String>();
        userForRanking.add("user1");
        userForRanking.add("user2");

        for (String globalId : userForRanking) {
            UserModel userModel = getPersistence().getOrCreateUserModelByUser(globalId);
            Assert.assertNotNull(userModel);
            userModels.add(userModel);
        }

        Queue<CommunicationMessage> rankerQueue = new LinkedBlockingQueue<CommunicationMessage>();
        Communicator communicator = new VirtualMachineCommunicator(rankerQueue, rankerQueue);

        Ranker ranker = new Ranker(getPersistence(), communicator,
                new SimpleMessageGroupMemberRunner<MessageFeatureContext>(userForRanking), flags);
        communicator.registerMessageHandler(ranker);

        if (ranker.getFlags().contains(RankerConfigurationFlag.USE_DIRECTED_USER_MODEL_ADAPTATION)) {
            DirectedUserModelAdapter adapter = new DirectedUserModelAdapter(getPersistence());
            communicator.registerMessageHandler(adapter);
        }

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

        UserSimilarityComputer computer = new UserSimilarityComputer(getPersistence());

        Ranker ranker = setupRanker(RankerConfigurationFlag.USE_DIRECTED_USER_MODEL_ADAPTATION);

        // user2 mentions user1
        Message message = createPlainTextMessage(
                "Test Content. This is some plain old test content with nothing spectacular in it.",
                user2.getGlobalId(), messageGroup);
        message.addProperty(MessageHelper.createMentionProperty(Arrays.asList(user1.getGlobalId())));

        // 1st rank just the mention
        MessageFeatureContext context = ranker.rank(message, messageRelation, null, false);

        computer.run();

        // TODO assert that there is a user1 <-> user2 similarity

        // here user1 is mentioned again, that should learn the content into the profile
        message = createPlainTextMessage(
                "The art of software engineering is the master piece of the modern era.",
                authorGlobalId, messageGroup);
        message.addProperty(MessageHelper.createMentionProperty(Arrays.asList(user1.getGlobalId())));

        // in the same moment the user model adaption should be triggered for user2, and the user
        // model should be adapted by the just learned items for user 1
        context = ranker.rank(message, messageRelation, null, false);

        // here user1 is mentioned again, that should learn the content into the profile
        message = createPlainTextMessage(
                "Is software engineering the master piece ?",
                authorGlobalId, messageGroup);

        // here again a rank should exist for user 2
        context = ranker.rank(message, messageRelation, null, false);

        MessageRank rankForUser2 = context.getUserContext(user2.getGlobalId()).getMessageRank();
        Assert.assertNotNull(rankForUser2);
        // Assert.assertTrue("rankForUser2 should positive if adaption run, but it is: " +
        // rankForUser2.getRank(), rankForUser2.getRank() > 0.5);

    }
}