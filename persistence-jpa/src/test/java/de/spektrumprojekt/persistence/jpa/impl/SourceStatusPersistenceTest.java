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
import de.spektrumprojekt.datamodel.source.Source;
import de.spektrumprojekt.datamodel.source.SourceStatus;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.persistence.jpa.JPAConfiguration;

public class SourceStatusPersistenceTest {

    /**
     * The name of the persistence unit for testing purposes, as configured in
     * META-INF/persistence.xml.
     */
    private static final String PERSISTENCE_UNIT_NAME = "de.spektrumprojekt.datamodel.test";

    private SourceStatusPersistence persistence;

    @Before
    public void setup() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("persistenceUnit", PERSISTENCE_UNIT_NAME);

        JPAConfiguration jpaConfiguration = new JPAConfiguration(new SimpleProperties(properties));

        persistence = new SourceStatusPersistence(jpaConfiguration);
    }

    @After
    public void tearDown() {
        persistence.deleteAll();
        assertEquals(0, persistence.getSourceStatusList().size());
    }

    @Test
    public void testDeleteAggregationSubscription() {
        String id1, id2;
        SourceStatus status1 = new SourceStatus(new Source(
                "connectorType"));
        SourceStatus status2 = new SourceStatus(new Source(
                "connectorType"));
        SourceStatus status3 = new SourceStatus(new Source(
                "connectorType"));
        id1 = status1.getSource().getGlobalId();
        id2 = status2.getSource().getGlobalId();

        persistence.storeSourceStatus(status1);
        persistence.storeSourceStatus(status2);
        persistence.storeSourceStatus(status3);

        assertEquals(3, persistence.getSourceStatusList().size());

        persistence.deleteSourceStatus(id1);
        persistence.deleteSourceStatus(id2);

        assertEquals(1, persistence.getSourceStatusList().size());

    }

    @Test
    public void testGetAggregationSubscription() {

        Source source1 = new Source("connectorType");
        Source source2 = new Source("connectorType");

        String id1 = source1.getGlobalId();

        SourceStatus sourceStatus1 = new SourceStatus(source1);
        SourceStatus sourceStatus2 = new SourceStatus(source2);
        persistence.storeSourceStatus(sourceStatus1);
        persistence.storeSourceStatus(sourceStatus2);

        SourceStatus persistentSourceStatus = persistence
                .getSourceStatusBySourceGlobalId(id1);
        assertEquals(persistentSourceStatus.getGlobalId(),
                sourceStatus1.getGlobalId());
        assertEquals(persistentSourceStatus.getSource().getGlobalId(),
                sourceStatus1.getSource().getGlobalId());
    }

    @Test
    public void testGetAllAggregationSubscription() {
        List<SourceStatus> sourceStatusList = persistence.getSourceStatusList();
        assertEquals(0, sourceStatusList.size());

        persistence.storeSourceStatus(new SourceStatus(new Source(
                "connectorType")));
        persistence.storeSourceStatus(new SourceStatus(new Source(
                "connectorType")));
        persistence.storeSourceStatus(new SourceStatus(new Source(
                "connectorType")));

        sourceStatusList = persistence.getSourceStatusList();
        assertEquals(3, sourceStatusList.size());
    }

    @Test
    public void testSaveAggregationSubscription() {
        Source subscription = new Source("connectorType");
        SourceStatus status = new SourceStatus(subscription);
        persistence.storeSourceStatus(status);
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
        Source subscription = new Source("connectorType");
        String id = subscription.getGlobalId();
        Assert.assertNotNull(id);

        SourceStatus status = new SourceStatus(subscription);
        status = persistence.storeSourceStatus(status);

        // status.updateCheck(StatusType.ERROR_UNSPECIFIED);
        status.updateCheck(StatusType.ERROR_UNSPECIFIED, new Date(), "contentHash");
        status = persistence.updateSourceStatus(status);

        SourceStatus persistentStatus = persistence
                .getSourceStatusBySourceGlobalId(id);
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
