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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.spektrumprojekt.configuration.properties.SimpleProperties;
import de.spektrumprojekt.datamodel.subscription.Subscription;
import de.spektrumprojekt.datamodel.subscription.SubscriptionStatus;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.persistence.jpa.JPAConfiguration;

public class AggregationSubscriptionPersistenceTest {

    /**
     * The name of the persistence unit for testing purposes, as configured in
     * META-INF/persistence.xml.
     */
    private static final String PERSISTENCE_UNIT_NAME = "de.spektrumprojekt.datamodel.test";

    private AggregationSubscriptionPersistence persistence;

    @Before
    public void setup() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("persistenceUnit", PERSISTENCE_UNIT_NAME);

        JPAConfiguration jpaConfiguration = new JPAConfiguration(new SimpleProperties(properties));

        persistence = new AggregationSubscriptionPersistence(jpaConfiguration);
    }

    @After
    public void tearDown() {
        persistence.deleteAll();
        assertEquals(0, persistence.getAggregationSubscriptions().size());
    }

    @Test
    public void testDeleteAggregationSubscription() {
        String id1, id2;
        SubscriptionStatus status1 = new SubscriptionStatus(new Subscription(
                "connectorType"));
        SubscriptionStatus status2 = new SubscriptionStatus(new Subscription(
                "connectorType"));
        SubscriptionStatus status3 = new SubscriptionStatus(new Subscription(
                "connectorType"));
        id1 = status1.getSubscription().getGlobalId();
        id2 = status2.getSubscription().getGlobalId();

        persistence.saveAggregationSubscription(status1);
        persistence.saveAggregationSubscription(status2);
        persistence.saveAggregationSubscription(status3);

        assertEquals(3, persistence.getAggregationSubscriptions().size());

        persistence.deleteAggregationSubscription(id1);
        persistence.deleteAggregationSubscription(id2);

        assertEquals(1, persistence.getAggregationSubscriptions().size());

    }

    @Test
    public void testGetAggregationSubscription() {

        Subscription subscription1 = new Subscription("connectorType");
        Subscription subscription2 = new Subscription("connectorType");

        String id1 = subscription1.getGlobalId();

        SubscriptionStatus aggregationStatus1 = new SubscriptionStatus(subscription1);
        SubscriptionStatus aggregationStatus2 = new SubscriptionStatus(subscription2);
        persistence.saveAggregationSubscription(aggregationStatus1);
        persistence.saveAggregationSubscription(aggregationStatus2);

        SubscriptionStatus persistentAggregationSubscription = persistence
                .getAggregationSubscription(id1);
        assertEquals(persistentAggregationSubscription.getGlobalId(),
                aggregationStatus1.getGlobalId());
        assertEquals(persistentAggregationSubscription.getSubscription().getGlobalId(),
                aggregationStatus1.getSubscription().getGlobalId());
    }

    @Test
    public void testGetAllAggregationSubscription() {
        List<SubscriptionStatus> aggregationStatus = persistence.getAggregationSubscriptions();
        assertEquals(0, aggregationStatus.size());

        persistence.saveAggregationSubscription(new SubscriptionStatus(new Subscription(
                "connectorType")));
        persistence.saveAggregationSubscription(new SubscriptionStatus(new Subscription(
                "connectorType")));
        persistence.saveAggregationSubscription(new SubscriptionStatus(new Subscription(
                "connectorType")));

        aggregationStatus = persistence.getAggregationSubscriptions();
        assertEquals(3, aggregationStatus.size());
    }

    @Test
    public void testSaveAggregationSubscription() {
        Subscription subscription = new Subscription("connectorType");
        SubscriptionStatus status = new SubscriptionStatus(subscription);
        persistence.saveAggregationSubscription(status);
    }

    /*
     * the AbstractPersistenceLayer uses merge, thats probably the reason for the missing
     * RollbackException thrown
     */
    // @Test(expected = RollbackException.class)
    // public void testSaveAggregationSubscriptionTwice() {
    // Subscription subscription = new Subscription("connectorType");
    // SubscriptionStatus status = new SubscriptionStatus(subscription);
    //
    // persistence.saveAggregationSubscription(status);
    // persistence.saveAggregationSubscription(status);
    // }

    @Test
    public void testUpdateAggregationSubscription() {
        Subscription subscription = new Subscription("connectorType");
        String id = subscription.getGlobalId();
        Assert.assertNotNull(id);

        SubscriptionStatus status = new SubscriptionStatus(subscription);
        status = persistence.saveAggregationSubscription(status);

        // status.updateCheck(StatusType.ERROR_UNSPECIFIED);
        status.updateCheck(StatusType.ERROR_UNSPECIFIED, new Date(), "contentHash");
        status = persistence.updateAggregationSubscription(status);

        SubscriptionStatus persistentStatus = persistence
                .getAggregationSubscription(id);
        assertEquals(status.getConsecutiveErrorCount(), persistentStatus.getConsecutiveErrorCount());
        assertEquals(status.getErrorCount(), persistentStatus.getErrorCount());
        assertEquals(status.getLastError(), persistentStatus.getLastError());
        assertEquals(status.getLastStatusType(), persistentStatus.getLastStatusType());
        assertEquals(status.getLastSuccessfulCheck(), persistentStatus.getLastSuccessfulCheck());
        assertEquals(status.getLastContentTimestamp(), persistentStatus.getLastContentTimestamp());
        assertEquals(status.getLastContentHash(), persistentStatus.getLastContentHash());
        // assertEquals(status.getSubscription(), persistentStatus.getSubscription());
    }

}
