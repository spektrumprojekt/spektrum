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

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import de.spektrumprojekt.aggregator.Aggregator;
import de.spektrumprojekt.aggregator.adapter.rss.FeedAdapter;
import de.spektrumprojekt.aggregator.chain.AggregatorChain;
import de.spektrumprojekt.aggregator.configuration.AggregatorConfiguration;
import de.spektrumprojekt.communication.CommunicationMessage;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.communication.vm.VirtualMachineCommunicator;
import de.spektrumprojekt.configuration.properties.SimpleProperties;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.source.Source;
import de.spektrumprojekt.datamodel.subscription.Subscription;
import de.spektrumprojekt.persistence.jpa.JPAConfiguration;
import de.spektrumprojekt.persistence.jpa.JPAPersistence;
import de.spektrumprojekt.persistence.jpa.impl.SubscriptionPersistence;

public class SubscriptionManagerTest {

    private static final String PERSISTENCE_UNIT_NAME = "de.spektrumprojekt.datamodel.test";

    private static final String URL_1 = "http://www.heise.de/newsticker/heise-atom.xml";

    private static final String URL_2 = "http://www.engadget.com/rss.xml";

    private static final String URL_3 = "http://www.spektrumprojekt.de/thegit";

    private JPAPersistence persistence;

    private SubscriptionPersistence subscriptionPersistence;

    private SubscriptionManager manager;

    private Aggregator aggregator;
    private AggregatorConfiguration aggregatorConfiguration;
    private AggregatorChain aggregatorChain;

    private Communicator communicator;

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

        Queue<CommunicationMessage> queue = new ConcurrentLinkedQueue<CommunicationMessage>();
        communicator = new VirtualMachineCommunicator(queue, queue);

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

        manager = new SubscriptionManager(communicator,
                persistence, aggregatorChain, aggregatorConfiguration);
    }

    @Test
    public void testSubscribe() {
        Subscription subscription = getRSSSubscription(URL_1, null);
        manager.subscribe(subscription);

        Assert.assertNotNull(persistence.getSubscriptionByGlobalId(subscription.getGlobalId()));
    }

    @Test
    public void testSubscribeWithSameSource() {
        Subscription subscription = getRSSSubscription(URL_3, null);

        manager.subscribe(subscription);
        Subscription persistedSubscription = this.persistence
                .getSubscriptionByGlobalId(subscription.getGlobalId());
        Assert.assertNotNull(persistedSubscription);
        Assert.assertNotNull(persistedSubscription.getGlobalId());

        Subscription subscription2 = getRSSSubscription(URL_3, null);
        Assert.assertFalse("Global Ids should be different.", subscription.getGlobalId()
                .equals(subscription2
                        .getGlobalId()));

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
                .equals(persistedSubscription2
                        .getGlobalId()));
        Assert.assertFalse("Database Ids should be different.", persistedSubscription.getId()
                .equals(persistedSubscription2
                        .getId()));

    }

    @Test
    public void testUnsubscribe() {
        Subscription subscription = getRSSSubscription(URL_1, null);
        manager.subscribe(subscription);
        Assert.assertNotNull(persistence.getSubscriptionByGlobalId(subscription.getGlobalId()));

        int numberOfSubscriptions = getNumberOfSubscriptions();
        manager.unsubscribe(subscription.getGlobalId());

        Assert.assertNull(persistence.getSubscriptionByGlobalId(subscription.getGlobalId()));
        Assert.assertEquals(numberOfSubscriptions - 1, getNumberOfSubscriptions());
    }

    @Test
    public void testUpdate() {
        Subscription subscription = getRSSSubscription(URL_1, null);
        manager.subscribe(subscription);

        Subscription updatedSubscription = getRSSSubscription(
                URL_1, subscription.getGlobalId());

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

}
