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

package de.spektrumprojekt.persistence.jpa.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.spektrumprojekt.configuration.properties.SimpleProperties;
import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.InteractionLevel;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageFilter;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.datamodel.message.MessageRelation.MessageRelationType;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.message.Term.TermCategory;
import de.spektrumprojekt.datamodel.message.TermFrequency;
import de.spektrumprojekt.datamodel.message.UserMessageScore;
import de.spektrumprojekt.datamodel.observation.Interest;
import de.spektrumprojekt.datamodel.observation.Observation;
import de.spektrumprojekt.datamodel.observation.ObservationPriority;
import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.Statistics;
import de.spektrumprojekt.persistence.jpa.JPAConfiguration;
import de.spektrumprojekt.persistence.jpa.JPAPersistence;

public class MessagePersistenceTest {

    /**
     * The name of the persistence unit for testing purposes, as configured in
     * META-INF/persistence.xml.
     */
    private static final String PERSISTENCE_UNIT_NAME = "de.spektrumprojekt.datamodel.test";

    private Persistence persistence;

    private void checkTermsAreDifferent(Term t1, Term t2) {
        Assert.assertFalse(t1.getId().equals(t2.getId()));
        Assert.assertFalse(t1.getGlobalId().equals(t2.getGlobalId()));
        Assert.assertFalse(t1.getValue().equals(t2.getValue()));
        Assert.assertFalse(t1.equals(t2));
    }

    private Message createTestMessage(String text) {
        return createTestMessage(text, null);
    }

    private Message createTestMessage(String text, Date date) {
        if (date == null) {
            date = new Date();
        }
        Message message = new Message(MessageType.CONTENT, StatusType.OK, null, date);
        MessagePart messagePart = new MessagePart(MimeType.TEXT_PLAIN, text);
        message.addMessagePart(messagePart);
        return message;
    }

    @Before
    public void setup() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("persistenceUnit", PERSISTENCE_UNIT_NAME);

        JPAConfiguration jpaConfiguration = new JPAConfiguration(new SimpleProperties(properties));
        persistence = new JPAPersistence(jpaConfiguration);
        persistence.initialize();
    }

    @Test
    public void testGetAllMessageGroups() {
        Collection<MessageGroup> groups = new HashSet<MessageGroup>();
        for (int i = 0; i < 10; i++) {
            groups.add(this.persistence.storeMessageGroup(new MessageGroup()));
        }

        Collection<MessageGroup> retrieveGroups = this.persistence.getAllMessageGroups();

        // we can have groups that existed before this test run
        Assert.assertTrue(groups.size() <= retrieveGroups.size());

        Assert.assertEquals(groups.size(), CollectionUtils.intersection(groups, retrieveGroups)
                .size());
    }

    @Test
    public void testGetMessageRelation() {
        String globalId = UUID.randomUUID().toString();
        String[] globalMessageIds = new String[] { UUID.randomUUID().toString(),
                UUID.randomUUID().toString() };
        Message message = new Message(globalId, MessageType.CONTENT, StatusType.OK, null,
                new Date());
        MessageRelation messageRelation = new MessageRelation(MessageRelationType.RELATION,
                globalId, globalMessageIds);

        persistence.storeMessageRelation(message, messageRelation);
        String[] loadedIds = persistence.getMessageRelation(message).getRelatedMessageGlobalIds();
        for (int i = 0; i < globalMessageIds.length; i++) {
            Assert.assertEquals(globalMessageIds[i], loadedIds[i]);
        }
    }

    @Test
    public void testGetMessagesSince() {
        Collection<Message> messages = new HashSet<Message>();

        MessageGroup group = this.persistence.storeMessageGroup(new MessageGroup());

        long size = 20;
        long intervall = 10000;
        final long startDate = new Date().getTime() - 1000000;
        long currentDate = startDate;
        for (int i = 0; i < size; i++) {

            Message message = createTestMessage("Unit Test testGetMessagesSince " + i, new Date(
                    currentDate));
            message.setMessageGroup(group);
            message = this.persistence.storeMessage(message);
            messages.add(message);

            currentDate += i * intervall;
        }

        MessageFilter messageFilter = new MessageFilter();
        messageFilter.setMessageGroupGlobalId(group.getGlobalId());
        messageFilter.setMinPublicationDate(new Date(startDate));

        // some iteration ?
        Collection<Message> returnedMessages = persistence.getMessages(messageFilter);

        Assert.assertEquals(messages.size(),
                CollectionUtils.intersection(returnedMessages, messages).size());

        messageFilter = new MessageFilter();
        messageFilter.setMessageGroupGlobalId(null);
        messageFilter.setMinPublicationDate(new Date(startDate));

        returnedMessages = persistence.getMessages(messageFilter);

        Assert.assertEquals(messages.size(),
                CollectionUtils.intersection(returnedMessages, messages).size());

        Date since = new Date(startDate + intervall * size / 2);

        messageFilter = new MessageFilter();
        messageFilter.setMessageGroupGlobalId(group.getGlobalId());
        messageFilter.setMinPublicationDate(since);

        returnedMessages = persistence.getMessages(messageFilter);

        for (Message returnedMessage : returnedMessages) {
            Assert.assertTrue("Date of message " + returnedMessage.getPublicationDate()
                    + " should be greater than " + since,
                    !since.after(returnedMessage.getPublicationDate()));
        }

    }

    @Test
    public void testGetNthUserMessageScore() {

        final int diff = 1000;
        long cutOffDate = new Date().getTime() - diff * 10;

        User user = this.persistence.getOrCreateUser("testGetNthUserMessageScoreUser");
        Collection<UserMessageScore> scores = new HashSet<UserMessageScore>();
        for (int i = 1; i <= 10; i++) {

            Message m = this.createTestMessage("Young: BlaText" + i,
                    new Date(cutOffDate + i * diff));
            m.setAuthorGlobalId(user.getGlobalId());

            InteractionLevel interactionLevel = i % 2 == 0 ? InteractionLevel.DIRECT
                    : InteractionLevel.NONE;
            scores.add(new UserMessageScore(m.getGlobalId(), user.getGlobalId(), interactionLevel,
                    i / 10f));
            this.persistence.storeMessage(m);
        }

        for (int i = 1; i <= 10; i++) {

            Message m = this.createTestMessage("Old: BlaText" + i, new Date(cutOffDate - i * diff));
            m.setAuthorGlobalId(user.getGlobalId());

            scores.add(new UserMessageScore(m.getGlobalId(), user.getGlobalId(),
                    InteractionLevel.NONE, i / 10f));

            this.persistence.storeMessage(m);
        }

        this.persistence.storeUserMessageScores(scores);

        // check to only get new
        for (int n = 1; n <= 12; n++) {
            UserMessageScore score = this.persistence.getNthUserMessageScore(
                    user.getGlobalId(),
                    n,
                    new Date(cutOffDate - 1),
                    new InteractionLevel[] { InteractionLevel.NONE, InteractionLevel.DIRECT });

            double exp = n > 10 ? 0.1 : 1 - (n - 1) / 10d;
            Assert.assertNotNull(score);
            Assert.assertEquals(exp, score.getScore(), 0.001);

            UserMessageScore score2 = this.persistence.getNthUserMessageScore(
                    user.getGlobalId(),
                    n,
                    new Date(cutOffDate - 1),
                    null);
            Assert.assertEquals(score.getMessageGlobalId(), score2.getMessageGlobalId());
            Assert.assertEquals(score.getUserGlobalId(), score2.getUserGlobalId());
            Assert.assertEquals(score.getScore(), score2.getScore(), 0.0001);
            Assert.assertEquals(score.getInteractionLevel(), score2.getInteractionLevel());

        }

        UserMessageScore score = this.persistence.getNthUserMessageScore(
                user.getGlobalId(),
                3,
                new Date(cutOffDate - 1),
                new InteractionLevel[] { InteractionLevel.NONE });
        Assert.assertNotNull(score);
        Assert.assertEquals(0.5, score.getScore(), 0.001);
    }

    @Test
    public void testGetOrCreateTerm() {

        Term term1 = persistence.getOrCreateTerm(TermCategory.TERM, "testterm1");
        Term term2 = persistence.getOrCreateTerm(TermCategory.TERM, "testterm2");
        Term term3 = persistence.getOrCreateTerm(TermCategory.TERM, "testterm3");

        Assert.assertNotNull(term1);
        Assert.assertNotNull(term2);
        Assert.assertNotNull(term3);

        checkTermsAreDifferent(term1, term2);
        checkTermsAreDifferent(term1, term3);
        checkTermsAreDifferent(term2, term3);

        Collection<Term> terms = new HashSet<Term>();
        Assert.assertTrue(terms.add(term1));
        Assert.assertTrue(terms.add(term2));
        Assert.assertTrue(terms.add(term3));
    }

    @Test
    public void testMessagePatterns() throws Exception {

        Date date = new Date(new Date().getTime() - 4 * DateUtils.MILLIS_PER_HOUR);

        String pattern1 = "TICKET-1";
        String pattern2 = "TICKET-2";
        String pattern3 = "TICKET-3";

        Message message1 = createTestMessage("TICKET-1 opened");
        Message message2 = createTestMessage("TICKET-2 modified");
        Thread.sleep(1000);
        Message message3 = createTestMessage("TICKET-1 closed");

        message1 = persistence.storeMessage(message1);
        message2 = persistence.storeMessage(message2);
        message3 = persistence.storeMessage(message3);

        persistence.storeMessagePattern(pattern1, message1);
        persistence.storeMessagePattern(pattern2, message2);
        persistence.storeMessagePattern(pattern1, message3);
        persistence.storeMessagePattern(pattern2, message1);

        MessageFilter messageFilter = new MessageFilter();
        messageFilter.setMinPublicationDate(date);

        messageFilter.setPattern(pattern1);
        Collection<Message> p1msgs = persistence.getMessages(messageFilter);
        assertEquals(2, p1msgs.size());

        messageFilter.setPattern(pattern2);
        Collection<Message> p2msgs = persistence.getMessages(messageFilter);
        assertEquals(2, p2msgs.size());

        messageFilter.setPattern(pattern3);
        Collection<Message> p3msgs = persistence.getMessages(messageFilter);
        ;
        assertEquals(0, p3msgs.size());

        messageFilter.setPattern(pattern1);
        p1msgs = persistence.getMessages(messageFilter);
        assertEquals(2, p1msgs.size());

        messageFilter.setPattern(pattern2);
        p2msgs = persistence.getMessages(messageFilter);
        assertEquals(2, p2msgs.size());

        messageFilter.setPattern(pattern3);
        p3msgs = persistence.getMessages(messageFilter);
        assertEquals(0, p3msgs.size());
    }

    @Test
    public void testMessagePersistence() throws ParseException {

        Statistics statistics = persistence.computeStatistics();
        long messageCountBefore = statistics.getMessageCount();

        Message message1 = new Message(MessageType.CONTENT, StatusType.OK, "s1", new Date());
        MessagePart messagePart = new MessagePart(MimeType.TEXT_PLAIN, "hello, world!");

        message1.addMessagePart(messagePart);
        message1.getProperties().add(new Property("foo", "bar"));

        Term term1 = persistence.getOrCreateTerm(TermCategory.TERM, "yeah");
        Term term2 = persistence.getOrCreateTerm(TermCategory.TERM, "catz");

        assertNotNull(term1.getId());
        assertNotNull(term2.getId());

        messagePart.addScoredTerm(new ScoredTerm(term1, 0.5f));
        messagePart.addScoredTerm(new ScoredTerm(term2, 0.9f));

        persistence.storeMessage(message1);

        statistics = persistence.computeStatistics();

        assertEquals(1, statistics.getMessageCount() - messageCountBefore);
        Message persistentMessage = persistence.getMessageByGlobalId(message1.getGlobalId());

        assertNotNull(persistentMessage);
        assertEquals(1, persistentMessage.getMessageParts().size());
        assertEquals(2, persistentMessage.getMessageParts().iterator().next().getScoredTerms()
                .size());
    }

    @Test
    public void testObservations() {
        Observation obs = new Observation("userId1", "messageId1", ObservationType.LIKE,
                ObservationPriority.USER_FEEDBACK, null, new Date(), Interest.EXTREME);

        persistence.storeObservation(obs);

        Collection<Observation> persistedObservations = persistence.getObservations(
                obs.getUserGlobalId(), obs.getMessageGlobalId(), obs.getObservationType());

        Assert.assertNotNull(persistedObservations);
        Assert.assertEquals(1, persistedObservations.size());

        Observation persistedObservation = persistedObservations.iterator().next();

        Assert.assertEquals(obs.getUserGlobalId(), persistedObservation.getUserGlobalId());
        Assert.assertEquals(obs.getMessageGlobalId(), persistedObservation.getMessageGlobalId());
        Assert.assertEquals(obs.getObservationType(), persistedObservation.getObservationType());

        Observation obs2 = new Observation("userId1", "messageId1", ObservationType.LIKE,
                ObservationPriority.FIRST_LEVEL_FEATURE_INFERRED, null, new Date(),
                Interest.EXTREME);
        persistence.storeObservation(obs2);
        persistedObservations = persistence.getObservations(obs.getUserGlobalId(),
                obs.getMessageGlobalId(), obs.getObservationType());

        Assert.assertNotNull(persistedObservations);
        Assert.assertEquals(2, persistedObservations.size());

        for (Observation ob : persistedObservations) {
            Assert.assertEquals(obs.getUserGlobalId(), ob.getUserGlobalId());
            Assert.assertEquals(obs.getMessageGlobalId(), ob.getMessageGlobalId());
            Assert.assertEquals(obs.getObservationType(), ob.getObservationType());
        }
    }

    @Test
    public void testStoreUserMessageScores() {
        Collection<UserMessageScore> scores = new HashSet<UserMessageScore>();

        Message message1 = createTestMessage("hello, world!");
        Message message2 = createTestMessage("hello, world2!");

        message1 = persistence.storeMessage(message1);
        message2 = persistence.storeMessage(message2);

        User user = persistence.getOrCreateUser("testUser");
        User user2 = persistence.getOrCreateUser("testUser2");

        Assert.assertNotNull(message1);
        Assert.assertNotNull(message2);
        Assert.assertNotNull(user);

        UserMessageScore score1 = new UserMessageScore(message1.getGlobalId(), user.getGlobalId());
        score1.setScore(0.25f);
        score1.setInteractionLevel(InteractionLevel.NONE);
        UserMessageScore score2 = new UserMessageScore(message2.getGlobalId(), user.getGlobalId());
        score2.setScore(0.75f);
        score2.setInteractionLevel(InteractionLevel.NONE);

        scores.add(score1);
        scores.add(score2);
        persistence.storeUserMessageScores(scores);

        UserMessageScore score1return = persistence.getMessageScore(user.getGlobalId(),
                message1.getGlobalId());
        UserMessageScore score2return = persistence.getMessageScore(user.getGlobalId(),
                message2.getGlobalId());

        Assert.assertNotNull(score1return);
        Assert.assertEquals(score1.getScore(), score1return.getScore(), 0.0001);
        Assert.assertEquals(score1.getInteractionLevel(), score1return.getInteractionLevel());

        Assert.assertNotNull(score2return);
        Assert.assertEquals(score2.getScore(), score2return.getScore(), 0.0001);
        Assert.assertEquals(score2.getInteractionLevel(), score2return.getInteractionLevel());

        UserMessageScore score3 = new UserMessageScore(message1.getGlobalId(), user2.getGlobalId());
        score3.setScore(0.15f);
        score3.setInteractionLevel(InteractionLevel.INDIRECT);

        scores.clear();
        scores.add(score3);
        persistence.storeUserMessageScores(scores);

        UserMessageScore score3return = persistence.getMessageScore(user2.getGlobalId(),
                message1.getGlobalId());

        Assert.assertNotNull(score3return);
        Assert.assertEquals(score3.getScore(), score3return.getScore(), 0.0001);
        Assert.assertEquals(score3.getInteractionLevel(), score3return.getInteractionLevel());
    }

    @Test
    public void testTermFrequency() {
        TermFrequency tf = persistence.getTermFrequency();
        Assert.assertNotNull(tf);

        tf.setAllTermCount(12);
        tf.setMessageCount(1200);
        tf.setUniqueTermCount(120);

        persistence.updateTermFrequency(tf);

        TermFrequency tf2 = persistence.getTermFrequency();

        Assert.assertEquals(tf.getAllTermCount(), tf2.getAllTermCount());
        Assert.assertEquals(tf.getMessageCount(), tf2.getMessageCount());
        Assert.assertEquals(tf.getUniqueTermCount(), tf2.getUniqueTermCount());

        for (int i = 0; i < 10; i++) {
            tf2.setMessageGroupMessageCount("mg" + i, i + 10);
        }
        persistence.updateTermFrequency(tf2);

        TermFrequency tf3 = persistence.getTermFrequency();

        for (int i = 0; i < 10; i++) {
            Integer integer = tf3.getMessageGroupMessageCounts().get("mg" + i);
            Assert.assertNotNull(i + "", integer);
            Assert.assertEquals(i + "", i + 10, integer.intValue());
        }

        for (int i = 0; i < 10; i++) {
            tf3.incrementMessageGroupMessageCount("mg" + i);
        }
        persistence.updateTermFrequency(tf3);

        TermFrequency tf4 = persistence.getTermFrequency();
        for (int i = 0; i < 10; i++) {

            Assert.assertEquals(i + 11, tf4.getMessageGroupMessageCounts().get("mg" + i).intValue());
        }
    }

    /**
     * Test term with message group string
     */
    @Test
    public void testTermMG() {
        final String test = "test";
        final MessageGroup mg = new MessageGroup();
        mg.setId(13l);

        String mgValue = Term.getMessageGroupSpecificTermValue(mg, test);
        Term mgTerm = this.persistence.getOrCreateTerm(TermCategory.TERM, mgValue);
        Term nonMgTerm = this.persistence.getOrCreateTerm(TermCategory.TERM, test);

        Assert.assertEquals(mg.getId(), mgTerm.getMessageGroupId());
        Assert.assertNull(nonMgTerm.getMessageGroupId());

        mgTerm = this.persistence.getOrCreateTerm(TermCategory.TERM, mgValue);
        nonMgTerm = this.persistence.getOrCreateTerm(TermCategory.TERM, test);

        Assert.assertEquals(mg.getId(), mgTerm.getMessageGroupId());
        Assert.assertNull(nonMgTerm.getMessageGroupId());
    }
}
