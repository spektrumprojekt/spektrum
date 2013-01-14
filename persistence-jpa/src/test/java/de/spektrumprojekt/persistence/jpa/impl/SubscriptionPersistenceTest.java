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
import java.util.Map;

import javax.persistence.RollbackException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.spektrumprojekt.configuration.properties.SimpleProperties;
import de.spektrumprojekt.datamodel.subscription.Subscription;
import de.spektrumprojekt.persistence.jpa.JPAConfiguration;

public class SubscriptionPersistenceTest {

	/**
	 * The name of the persistence unit for testing purposes, as configured in
	 * META-INF/persistence.xml.
	 */
	private static final String PERSISTENCE_UNIT_NAME = "de.spektrumprojekt.datamodel.test";

	// private EntityManagerFactory entityManagerFactory;
	private SubscriptionPersistence persistenceLayer;

	@Before
	public void setUp() {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("persistenceUnit", PERSISTENCE_UNIT_NAME);

		JPAConfiguration jpaConfiguration = new JPAConfiguration(
				new SimpleProperties(properties));

		persistenceLayer = new SubscriptionPersistence(jpaConfiguration);
	}

	@After
	public void tearDown() {
		persistenceLayer.deleteAllSubscriptions();
		assertEquals(0, persistenceLayer.getSubscriptions().size());
	}

	@Test(expected = RollbackException.class)
	public void testGlobalIdDuplicate() {
		Subscription subscription = new Subscription("test", "RSS");
		Subscription subscription2 = new Subscription("test", "RSS");
		persistenceLayer.save(subscription);
		persistenceLayer.save(subscription2);
	}

	@Test
	public void testSaveSubscription() {
		Subscription subscription = new Subscription("connectorType");
		String globalId = subscription.getGlobalId();
		persistenceLayer.saveSubscription(subscription);

		assertEquals(1, persistenceLayer.getSubscriptions().size());

		Subscription persistentSubscription = persistenceLayer
				.getSubscription(globalId);

		assertEquals(persistentSubscription != null, true);
		assertEquals(globalId, persistentSubscription.getGlobalId());
	}

	// @Test
	// public void testDeleteSubscription() {
	//
	// Subscription subscription = new Subscription("1234");
	// SubscriptionWithChannel subscriptionChannel = new
	// SubscriptionWithChannel(subscription,
	// "test@localhost");
	//
	// Subscription subscription2 = new Subscription("2345");
	// SubscriptionWithChannel subscriptionChannel2 = new
	// SubscriptionWithChannel(subscription2,
	// "test@localhost");
	//
	// Subscription subscription3 = new Subscription("3456");
	// SubscriptionWithChannel subscriptionChannel3 = new
	// SubscriptionWithChannel(subscription3,
	// "test@localhost");
	//
	// persistenceLayer.saveSubscriptionSpecification(subscriptionChannel);
	// persistenceLayer.saveSubscriptionSpecification(subscriptionChannel2);
	// persistenceLayer.saveSubscriptionSpecification(subscriptionChannel3);
	// assertEquals(3, persistenceLayer.getSubscriptionSpecifications().size());
	//
	// persistenceLayer.deleteSubscriptionWithChannel("1234");
	// assertEquals(2, persistenceLayer.getSubscriptionSpecifications().size());
	//
	// SubscriptionWithChannel subscriptionWithChannel =
	// persistenceLayer.getSubscriptionSpecifications().get(0);
	// assertEquals("2345",
	// subscriptionWithChannel.getSubscription().getSubscriptionID());
	//
	// }

}
