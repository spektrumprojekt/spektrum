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

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.spektrumprojekt.configuration.properties.SimpleProperties;
import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageRank;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.message.Term.TermCategory;
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

    @Test
    public void MessagePatterns() throws Exception {

        String pattern1 = "TICKET-1";
        String pattern2 = "TICKET-2";
        String pattern3 = "TICKET-3";

        Message message1 = createTestMessage("TICKET-1 opened");
        Message message2 = createTestMessage("TICKET-2 modified");
        Message message3 = createTestMessage("TICKET-1 closed");

        persistence.storeMessagePattern(pattern1, message1);
        persistence.storeMessagePattern(pattern2, message2);
        persistence.storeMessagePattern(pattern1, message3);

        Collection<Message> p1msgs = persistence.getMessagesForPattern(pattern1);
        assertEquals(2, p1msgs.size());

        Collection<Message> p2msgs = persistence.getMessagesForPattern(pattern2);
        assertEquals(1, p2msgs.size());

        Collection<Message> p3msgs = persistence.getMessagesForPattern(pattern3);
        assertEquals(0, p3msgs.size());
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

        Collection<Message> returnedMessages = this.persistence.getMessagesSince(
                group.getGlobalId(), new Date(startDate));

        Assert.assertEquals(messages.size(),
                CollectionUtils.intersection(returnedMessages, messages).size());

        returnedMessages = this.persistence.getMessagesSince(null, new Date(startDate));

        Assert.assertEquals(messages.size(),
                CollectionUtils.intersection(returnedMessages, messages).size());

        Date since = new Date(startDate + intervall * size / 2);
        returnedMessages = this.persistence.getMessagesSince(
                group.getGlobalId(), since);

        for (Message returnedMessage : returnedMessages) {
            Assert.assertTrue("Date of message " + returnedMessage.getPublicationDate()
                    + " should be greater than " + since,
                    !since.after(returnedMessage.getPublicationDate()));
        }

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
    public void testMessageRanks() {
        Collection<MessageRank> ranks = new HashSet<MessageRank>();

        Message message1 = createTestMessage("hello, world!");
        Message message2 = createTestMessage("hello, world!");

        message1 = persistence.storeMessage(message1);
        message2 = persistence.storeMessage(message2);

        User user = persistence.getOrCreateUser("testUser");

        Assert.assertNotNull(message1);
        Assert.assertNotNull(message2);
        Assert.assertNotNull(user);

        MessageRank rank1 = new MessageRank(message1.getGlobalId(), user.getGlobalId());
        rank1.setRank(0.25f);
        MessageRank rank2 = new MessageRank(message2.getGlobalId(), user.getGlobalId());
        rank2.setRank(0.75f);

        ranks.add(rank1);
        ranks.add(rank2);
        persistence.storeMessageRanks(ranks);

        MessageRank rank1return = persistence.getMessageRank(user.getGlobalId(),
                message1.getGlobalId());
        MessageRank rank2return = persistence.getMessageRank(user.getGlobalId(),
                message2.getGlobalId());

        Assert.assertNotNull(rank1return);
        Assert.assertEquals(rank1.getRank(), rank1return.getRank(), 0.0001);

        Assert.assertNotNull(rank2return);
        Assert.assertEquals(rank2.getRank(), rank2return.getRank(), 0.0001);
    }

    @Test
    public void testObservations() {
        Observation obs = new Observation("userId1", "messageId1", ObservationType.LIKE,
                ObservationPriority.USER_FEEDBACK, null,
                new Date(), Interest.EXTREME);

        persistence.storeObservation(obs);

        Collection<Observation> persistedObservations = persistence.getObservations(
                obs.getUserGlobalId(),
                obs.getMessageGlobalId(), obs.getObservationType());

        Assert.assertNotNull(persistedObservations);
        Assert.assertEquals(1, persistedObservations.size());

        Observation persistedObservation = persistedObservations.iterator().next();

        Assert.assertEquals(obs.getUserGlobalId(), persistedObservation.getUserGlobalId());
        Assert.assertEquals(obs.getMessageGlobalId(), persistedObservation.getMessageGlobalId());
        Assert.assertEquals(obs.getObservationType(), persistedObservation.getObservationType());

        Observation obs2 = new Observation("userId1", "messageId1", ObservationType.LIKE,
                ObservationPriority.FIRST_LEVEL_FEATURE_INFERRED, null,
                new Date(), Interest.EXTREME);
        persistence.storeObservation(obs2);
        persistedObservations = persistence.getObservations(obs.getUserGlobalId(),
                obs.getMessageGlobalId(),
                obs.getObservationType());

        Assert.assertNotNull(persistedObservations);
        Assert.assertEquals(2, persistedObservations.size());

        for (Observation ob : persistedObservations) {
            Assert.assertEquals(obs.getUserGlobalId(), ob.getUserGlobalId());
            Assert.assertEquals(obs.getMessageGlobalId(), ob.getMessageGlobalId());
            Assert.assertEquals(obs.getObservationType(), ob.getObservationType());
        }
    }
}
