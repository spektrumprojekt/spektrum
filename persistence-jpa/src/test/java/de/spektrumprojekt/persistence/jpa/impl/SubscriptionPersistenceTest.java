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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.RollbackException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.spektrumprojekt.configuration.properties.SimpleProperties;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.source.Source;
import de.spektrumprojekt.datamodel.subscription.Subscription;
import de.spektrumprojekt.persistence.jpa.JPAConfiguration;

public class SubscriptionPersistenceTest {

    /**
     * The name of the persistence unit for testing purposes, as configured in
     * META-INF/persistence.xml.
     */
    private static final String PERSISTENCE_UNIT_NAME = "de.spektrumprojekt.datamodel.test";

    private SubscriptionPersistence subsciptionPersistence;
    private SourcePersistence sourcePersistence;

    @Before
    public void setUp() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("persistenceUnit", PERSISTENCE_UNIT_NAME);

        JPAConfiguration jpaConfiguration = new JPAConfiguration(
                new SimpleProperties(properties));

        subsciptionPersistence = new SubscriptionPersistence(jpaConfiguration);
        sourcePersistence = new SourcePersistence(jpaConfiguration);
    }

    @After
    public void tearDown() {
        subsciptionPersistence.deleteAllSubscriptions();
        assertEquals(0, subsciptionPersistence.getSubscriptions().size());
    }

    @Test
    public void testGetAll() {

        Source mySource = new Source("mySource", "someConnector");
        Source anotherMySource = new Source(
                "anotherMySource", "someConnector");

        mySource = sourcePersistence.saveSource(mySource);
        anotherMySource = sourcePersistence.saveSource(anotherMySource);

        for (int i = 0; i < 10; i++) {
            Subscription subscription = new Subscription("findMeId" + i, mySource);
            subsciptionPersistence.storeSubscription(subscription);
        }
        for (int i = 0; i < 10; i++) {
            Subscription subscription = new Subscription("dontFindMeId" + i, anotherMySource);
            subsciptionPersistence.storeSubscription(subscription);
        }

        List<Subscription> subscriptions = subsciptionPersistence
                .getAllSubscriptionsBySourceGlobalId("mySource");
        Assert.assertNotNull(subscriptions);
        Assert.assertEquals(10, subscriptions.size());
        for (Subscription sub : subscriptions) {
            Assert.assertEquals("someConnector", sub.getSource().getConnectorType());
            Assert.assertTrue(sub.getGlobalId().startsWith("findMeId"));
        }
    }

    @Test(expected = RollbackException.class)
    public void testGlobalIdDuplicate() {
        Source source = new Source("connectorType");

        Subscription subscription = new Subscription("test", source);
        Subscription subscription2 = new Subscription("test", source);

        subsciptionPersistence.save(subscription);
        subsciptionPersistence.save(subscription2);
    }

    @Test
    public void testSaveSubscription() {
        Source source = new Source("connectorType");

        Subscription subscription = new Subscription("subId", source);

        String globalId = subscription.getGlobalId();
        subsciptionPersistence.storeSubscription(subscription);

        assertEquals(1, subsciptionPersistence.getSubscriptions().size());

        Subscription persistentSubscription = subsciptionPersistence
                .getSubscriptionByGlobalId(globalId);

        assertEquals(persistentSubscription != null, true);
        assertEquals(globalId, persistentSubscription.getGlobalId());

        Assert.assertNotNull(persistentSubscription.getSource());
        Assert.assertEquals(source.getGlobalId(), persistentSubscription.getSource().getGlobalId());

        persistentSubscription.getSource().addAccessParameter(new Property("dummy", "test"));
        subsciptionPersistence.updateSubscription(persistentSubscription);

        persistentSubscription = subsciptionPersistence.getSubscriptionByGlobalId(globalId);
        assertEquals(persistentSubscription != null, true);
        assertEquals(globalId, persistentSubscription.getGlobalId());

        Assert.assertNotNull(persistentSubscription.getSource());
        Assert.assertEquals(source.getGlobalId(), persistentSubscription.getSource().getGlobalId());

        Assert.assertNull(persistentSubscription.getSource().getAccessParameter("dummy"));

    }
}
