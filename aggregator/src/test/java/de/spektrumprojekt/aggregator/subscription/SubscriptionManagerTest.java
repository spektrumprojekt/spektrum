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

package de.spektrumprojekt.aggregator.subscription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import de.spektrumprojekt.aggregator.Aggregator;
import de.spektrumprojekt.aggregator.TestHelper;
import de.spektrumprojekt.aggregator.adapter.AdapterException;
import de.spektrumprojekt.aggregator.adapter.rss.FeedAdapter;
import de.spektrumprojekt.aggregator.adapter.rss.FileAdapter;
import de.spektrumprojekt.aggregator.chain.AggregatorChain;
import de.spektrumprojekt.aggregator.configuration.AggregatorConfiguration;
import de.spektrumprojekt.communication.CommunicationMessage;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.communication.MessageHandler;
import de.spektrumprojekt.communication.transfer.MessageCommunicationMessage;
import de.spektrumprojekt.communication.vm.VirtualMachineCommunicator;
import de.spektrumprojekt.configuration.properties.SimpleProperties;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.source.Source;
import de.spektrumprojekt.datamodel.source.SourceNotFoundException;
import de.spektrumprojekt.datamodel.source.SourceStatus;
import de.spektrumprojekt.datamodel.subscription.Subscription;
import de.spektrumprojekt.datamodel.subscription.SubscriptionMessageFilter;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.exceptions.SubscriptionNotFoundException;
import de.spektrumprojekt.persistence.jpa.JPAConfiguration;
import de.spektrumprojekt.persistence.jpa.JPAPersistence;
import de.spektrumprojekt.persistence.jpa.impl.SubscriptionPersistence;

public class SubscriptionManagerTest {

    /**
     * Handler to recive the {@link TestMessage}
     * 
     * @author Communardo Software GmbH - <a
     *         href="http://www.communardo.de/">http://www.communardo.de/</a>
     * 
     */
    public class MessageHandlerTest implements MessageHandler<MessageCommunicationMessage> {

        private final List<MessageCommunicationMessage> messages = new ArrayList<MessageCommunicationMessage>();

        /**
         * {@inheritDoc}
         */
        @Override
        public void deliverMessage(MessageCommunicationMessage message) throws Exception {
            messages.add(message);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Class<MessageCommunicationMessage> getMessageClass() {
            return MessageCommunicationMessage.class;
        }

        /**
         * getter for received messages
         * 
         * @return messages
         */
        public List<MessageCommunicationMessage> getMessages() {
            return messages;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean supports(CommunicationMessage message) {
            return message instanceof MessageCommunicationMessage;
        }
    }

    private static final String PERSISTENCE_UNIT_NAME = "de.spektrumprojekt.datamodel.test";

    private static final String URL_1 = "http://www.heise.de/newsticker/heise-atom.xml";

    private static final String URL_2 = "http://www.engadget.com/rss.xml";

    private static final String URL_3 = "http://www.spektrumprojekt.de/thegit";

    private static final String URL_4 = "http://www.spiegel.de/schlagzeilen/tops/index.rss";

    private static final String URL_5 = "http://www.spiegel.de/schlagzeilen/eilmeldungen/index.rss";

    private JPAPersistence persistence;

    private SubscriptionPersistence subscriptionPersistence;

    private PersistentSubscriptionManager manager;
    private Aggregator aggregator;
    private AggregatorConfiguration aggregatorConfiguration;

    private AggregatorChain aggregatorChain;

    private Communicator communicator;

    private MessageHandlerTest messageHandlerTest;

    private Queue<CommunicationMessage> queue;

    /**
     * Creates a RSS Subscription
     * 
     * @param feedURI
     *            URL of the feed
     * @return Subscription
     */
    private Subscription getFileSubscription(String path, String subscriptionGlobalId) {
        if (subscriptionGlobalId == null) {
            subscriptionGlobalId = UUID.randomUUID().toString();
        }
        Source source = new Source(FileAdapter.SOURCE_TYPE);
        Subscription sub = new Subscription(subscriptionGlobalId, source);
        source.addAccessParameter(new Property(FileAdapter.ACCESS_PARAMETER_PATH, path));

        return sub;
    }

    private int getNumberOfSubscriptions() {
        return subscriptionPersistence.getSubscriptions().size();
    }

    /**
     * Creates a RSS Subscription
     * 
     * @param feedURI
     *            URL of the feed
     * @return Subscription
     */
    private Subscription getRSSSubscription(String feedURI, String subscriptionGlobalId) {
        if (subscriptionGlobalId == null) {
            subscriptionGlobalId = UUID.randomUUID().toString();
        }
        Source source = new Source(FeedAdapter.SOURCE_TYPE);
        Subscription sub = new Subscription(subscriptionGlobalId, source);
        source.addAccessParameter(new Property(FeedAdapter.ACCESS_PARAMETER_URI, feedURI));

        return sub;
    }

    @Before
    public void setup() throws Exception {

        messageHandlerTest = new MessageHandlerTest();

        queue = new ConcurrentLinkedQueue<CommunicationMessage>();
        communicator = new VirtualMachineCommunicator(queue, queue);

        communicator.registerMessageHandler(messageHandlerTest);

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("persistenceUnit", PERSISTENCE_UNIT_NAME);

        persistence = new JPAPersistence(new SimpleProperties(properties));
        persistence.initialize();
        subscriptionPersistence = new SubscriptionPersistence(new JPAConfiguration(
                new SimpleProperties(properties)));

        aggregatorConfiguration = AggregatorConfiguration.loadXmlConfig();
        Assert.assertNotNull(aggregatorConfiguration);

        aggregator = new Aggregator(communicator, persistence, aggregatorConfiguration);

        aggregatorChain = aggregator.getAggregatorChain();

        manager = new PersistentSubscriptionManager(communicator, persistence, aggregatorChain,
                aggregatorConfiguration);

        communicator.open();
    }

    @Test
    public void testLastAccessMessage() throws Exception {
        String subscriptionId = "testLastAccessMessageSubscriptionId";
        Subscription subscription = getFileSubscription(
                TestHelper.getTestFilePath(TestHelper.FILE_NAME_INVALID_XML), subscriptionId);
        manager.subscribe(subscription);
        FileAdapter adapter = new FileAdapter(aggregatorChain, aggregatorConfiguration);
        SourceStatus status = persistence.getSourceStatusBySourceGlobalId(subscription.getSource()
                .getGlobalId());
        AdapterException exception = null;
        try {
            adapter.poll(status);
        } catch (AdapterException e) {
            exception = e;
        }
        manager.processed(subscription.getSource(), exception.getStatusType(), exception);
        status = persistence
                .getSourceStatusBySourceGlobalId(subscription.getSource().getGlobalId());
        Assert.assertNotNull(status.getLastAccessMessage());
        // be careful because of the l18n of the Errormessage
        Assert.assertTrue(status.getLastAccessMessage().contains("\"</item>\""));
        Assert.assertTrue(status.getLastAccessMessage().contains("Error on line 15"));
        manager.processed(status.getSource(), StatusType.OK);
        status = persistence
                .getSourceStatusBySourceGlobalId(subscription.getSource().getGlobalId());
        Assert.assertNull(status.getLastAccessMessage());
    }

    @Test
    public void testSubscribe() throws Exception {
        Subscription subscription = getRSSSubscription(URL_1, null);
        manager.subscribe(subscription);

        Assert.assertNotNull(persistence.getSubscriptionByGlobalId(subscription.getGlobalId()));
    }

    @Test
    public void testSubscribeWithFilter() throws Exception {

        int initalCount = 10;
        SubscriptionMessageFilter subscriptionMessageFilter = new SubscriptionMessageFilter(
                initalCount, null);

        this.messageHandlerTest.getMessages().clear();
        Subscription subscription1 = getRSSSubscription(URL_4, null);
        manager.subscribe(subscription1);
        Assert.assertNotNull(persistence.getSubscriptionByGlobalId(subscription1.getGlobalId()));

        // wait for the subscriptions to run.
        Thread.sleep(1000);
        waitForQueueToEmpty();

        // wait for all messages to be delivered
        int size = this.messageHandlerTest.getMessages().size();
        Assert.assertTrue("Should have at least 5 messages but have: " + size, size >= 5);

        this.messageHandlerTest.getMessages().clear();
        Subscription subscription2 = getRSSSubscription(URL_4, null);
        manager.subscribe(subscription2, subscriptionMessageFilter);
        Subscription persisted = persistence.getSubscriptionByGlobalId(subscription2.getGlobalId());
        Assert.assertNotNull(persisted);

        Thread.sleep(1000);
        waitForQueueToEmpty();

        Assert.assertEquals(Math.min(size, initalCount), this.messageHandlerTest.getMessages()
                .size());

        // wait for all messages to be delivered
        // check the subscription somehow
    }

    @Test
    public void testSubscribeWithSameSource() throws Exception {
        Subscription subscription = getRSSSubscription(URL_3, null);

        manager.subscribe(subscription);
        Subscription persistedSubscription = this.persistence
                .getSubscriptionByGlobalId(subscription.getGlobalId());
        Assert.assertNotNull(persistedSubscription);
        Assert.assertNotNull(persistedSubscription.getGlobalId());

        Subscription subscription2 = getRSSSubscription(URL_3, null);
        Assert.assertFalse("Global Ids should be different.",
                subscription.getGlobalId().equals(subscription2.getGlobalId()));

        manager.subscribe(subscription2);
        Subscription persistedSubscription2 = this.persistence
                .getSubscriptionByGlobalId(subscription2.getGlobalId());
        Assert.assertNotNull(persistedSubscription2);
        Assert.assertNotNull(persistedSubscription2.getGlobalId());

        Assert.assertEquals(persistedSubscription.getSource().getGlobalId(), persistedSubscription2
                .getSource().getGlobalId());
        Assert.assertEquals(persistedSubscription.getSource().getId(), persistedSubscription2
                .getSource().getId());
        Assert.assertFalse("Global Ids should be different.", persistedSubscription.getGlobalId()
                .equals(persistedSubscription2.getGlobalId()));
        Assert.assertFalse("Database Ids should be different.", persistedSubscription.getId()
                .equals(persistedSubscription2.getId()));

    }

    @Test
    public void testSubscribeWithSourceStatusProperties() throws Exception {

        this.messageHandlerTest.getMessages().clear();

        Collection<Property> sourceStatusProperties = new HashSet<Property>();
        for (int i = 1; i <= 3; i++) {
            sourceStatusProperties.add(new Property("testKey" + i, "val" + i));
        }

        Subscription subscription = getRSSSubscription(URL_5, null);
        manager.subscribe(subscription, null, sourceStatusProperties);
        subscription = persistence.getSubscriptionByGlobalId(subscription.getGlobalId());
        Assert.assertNotNull(subscription);

        // wait for the subscriptions to run.
        Thread.sleep(1000);
        waitForQueueToEmpty();

        SourceStatus sourceStatus = persistence.getSourceStatusBySourceGlobalId(subscription
                .getSource().getGlobalId());

        Assert.assertNotNull(sourceStatus);
        Assert.assertTrue(sourceStatus.getProperties().size() >= 3);

        for (int i = 1; i <= 3; i++) {
            Property prop = null;
            p: for (Property property : sourceStatus.getProperties()) {
                if (("testKey" + i).equals(property.getPropertyKey())) {
                    prop = property;
                    break p;
                }
            }
            Assert.assertNotNull(prop);
            Assert.assertEquals("val" + i, prop.getPropertyValue());
        }

    }

    @Test
    public void testSuspendSubscription() throws Exception {
        Subscription subscription = getRSSSubscription(URL_3, null);

        manager.subscribe(subscription);
        Subscription persistedSubscription = manager.getSubscription(subscription.getGlobalId());
        Assert.assertNotNull(persistedSubscription);
        Assert.assertNotNull(persistedSubscription.getGlobalId());

        Assert.assertFalse("Subscription not be suspended at the start.",
                persistedSubscription.isSuspended());

        // suspend
        manager.suspendSubscription(persistedSubscription.getGlobalId());

        persistedSubscription = manager.getSubscription(subscription.getGlobalId());
        Assert.assertNotNull(persistedSubscription);
        Assert.assertNotNull(persistedSubscription.getGlobalId());

        Assert.assertTrue("Subscription should be suspended now.",
                persistedSubscription.isSuspended());

        // continue
        manager.continueSubscription(persistedSubscription.getGlobalId());

        persistedSubscription = manager.getSubscription(subscription.getGlobalId());
        Assert.assertNotNull(persistedSubscription);
        Assert.assertNotNull(persistedSubscription.getGlobalId());

        Assert.assertFalse("Subscription not be suspended any more.",
                persistedSubscription.isSuspended());

    }

    @Test
    public void testUnsubscribe() throws Exception {
        Subscription subscription = getRSSSubscription(URL_1, null);
        manager.subscribe(subscription);
        Assert.assertNotNull(persistence.getSubscriptionByGlobalId(subscription.getGlobalId()));

        int numberOfSubscriptions = getNumberOfSubscriptions();
        manager.unsubscribe(subscription.getGlobalId());

        try {
            persistence.getSubscriptionByGlobalId(subscription.getGlobalId());
            Assert.fail("Subscription should not exist anymore and a SubscriptionNotFoundException should be thrown.");
        } catch (SubscriptionNotFoundException e) {
            // success
        }
        Assert.assertEquals(numberOfSubscriptions - 1, getNumberOfSubscriptions());
    }

    @Test
    public void testUpdate() throws Exception {
        Subscription subscription = getRSSSubscription(URL_1, null);
        manager.subscribe(subscription);

        Subscription updatedSubscription = getRSSSubscription(URL_1, subscription.getGlobalId());

        updatedSubscription.addSubscriptionParameter(new Property("subKey", "subValue"));

        Assert.assertTrue(manager.updateOrCreate(updatedSubscription));
        Assert.assertFalse("No update should have been made.",
                manager.updateOrCreate(updatedSubscription));

        Property urlProp = persistence.getSubscriptionByGlobalId(subscription.getGlobalId())
                .getSubscriptionParameter("subKey");

        Assert.assertNotNull(urlProp);
        Assert.assertNotNull(urlProp.getPropertyValue());
        Assert.assertEquals("subValue", urlProp.getPropertyValue());
    }

    public void testUpdateProperty() throws AdapterNotFoundException, SourceNotFoundException,
            SubscriptionNotFoundException {
        Subscription subscription = getRSSSubscription(URL_1, null);
        Collection<Property> sourceStatusProperties = new HashSet<Property>();
        sourceStatusProperties.add(new Property("stat1", "1"));

        manager.subscribe(subscription, null, sourceStatusProperties);

        List<SourceStatus> status = manager.findSourceStatusByProperty(new Property("stat1", "1"));
        Assert.assertEquals(1, status.size());
        String sourceId = status.get(0).getSource().getGlobalId();

        manager.updateSourceAccessParameter(sourceId,
                Collections.singleton(new Property(FeedAdapter.ACCESS_PARAMETER_TITLE, "title")));

        subscription = manager.getSubscription(subscription.getGlobalId());
        Assert.assertNotNull(subscription);

        Property prop = subscription.getSource().getAccessParameter(
                FeedAdapter.ACCESS_PARAMETER_TITLE);
        Assert.assertNotNull(prop);
        Assert.assertEquals("title", prop.getPropertyValue());
    }

    private void waitForQueueToEmpty() throws InterruptedException {
        int limit = 10;
        while (queue.size() > 0 && limit > 0) {
            Thread.sleep(500);
            limit--;
        }
        Assert.assertEquals("Waited some seconds but here are still messages in the queue!", 0,
                queue.size());
    }

}
