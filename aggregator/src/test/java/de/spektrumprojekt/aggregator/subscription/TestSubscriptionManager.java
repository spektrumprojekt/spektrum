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

import de.spektrumprojekt.aggregator.adapter.rss.FeedAdapter;
import de.spektrumprojekt.aggregator.configuration.AggregatorConfiguration;
import de.spektrumprojekt.communication.CommunicationMessage;
import de.spektrumprojekt.communication.vm.VirtualMachineCommunicator;
import de.spektrumprojekt.configuration.properties.SimpleProperties;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.subscription.Subscription;
import de.spektrumprojekt.persistence.jpa.JPAConfiguration;
import de.spektrumprojekt.persistence.jpa.JPAPersistence;
import de.spektrumprojekt.persistence.jpa.impl.SubscriptionPersistence;

public class TestSubscriptionManager {

    private static final String PERSISTENCE_UNIT_NAME = "de.spektrumprojekt.datamodel.test";

    private static final String URL_1 = "http://www.heise.de/newsticker/heise-atom.xml";

    private static final String URL_2 = "http://www.engadget.com/rss.xml";

    private JPAPersistence persistence;

    private SubscriptionPersistence subscriptionPersistence;

    private SubscriptionManager manager;

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
        Subscription sub = new Subscription(subscriptionGlobalId, FeedAdapter.SOURCE_TYPE);
        sub.addAccessParameter(new Property(FeedAdapter.ACCESS_PARAMETER_URI, feedURI));
        return sub;
    }

    @Before
    public void setup() throws Exception {
        Queue<CommunicationMessage> queue = new ConcurrentLinkedQueue<CommunicationMessage>();
        AggregatorConfiguration configuration = AggregatorConfiguration.loadXmlConfig();
        Assert.assertNotNull(configuration);

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("persistenceUnit", PERSISTENCE_UNIT_NAME);

        persistence = new JPAPersistence(new SimpleProperties(properties));
        persistence.initialize();
        subscriptionPersistence = new SubscriptionPersistence(new JPAConfiguration(
                new SimpleProperties(properties)));

        manager = new SubscriptionManager(new VirtualMachineCommunicator(queue, queue),
                persistence, configuration);
    }

    @Test
    public void testSubscribe() {
        Subscription subscription = getRSSSubscription(URL_1, null);
        manager.subscribe(subscription);
        Assert.assertNotNull(persistence.getAggregationSubscription(subscription.getGlobalId()));
    }

    @Test
    public void testUnsubscribe() {
        Subscription subscription = getRSSSubscription(URL_1, null);
        manager.subscribe(subscription);
        int numberOfSubscriptions = getNumberOfSubscriptions();
        manager.unsubscribe(subscription.getGlobalId());
        Assert.assertNull(persistence.getAggregationSubscription(subscription.getGlobalId()));
        Assert.assertEquals(numberOfSubscriptions - 1, getNumberOfSubscriptions());
    }

    @Test
    public void testUpdate() {
        Subscription subscription = getRSSSubscription(URL_1, null);
        manager.subscribe(subscription);
        Subscription updatedSubscription = getRSSSubscription(
                URL_2, subscription.getGlobalId());
        Assert.assertTrue(manager.updateOrCreate(updatedSubscription));
        Assert.assertFalse(manager.updateOrCreate(updatedSubscription));
        Property urlProp = persistence.getAggregationSubscription(subscription.getGlobalId())
                .getSubscription().getAccessParameter(FeedAdapter.ACCESS_PARAMETER_URI);

        Assert.assertNotNull(urlProp);
        Assert.assertNotNull(urlProp.getPropertyValue());
        Assert.assertEquals(URL_2, urlProp.getPropertyValue());
    }
}
