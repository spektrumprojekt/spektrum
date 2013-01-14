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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import de.spektrumprojekt.datamodel.subscription.Subscription;
import de.spektrumprojekt.persistence.jpa.JPAConfiguration;

/**
 * 
 * @author Philipp Katz
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public final class SubscriptionPersistence extends AbstractPersistenceLayer {

    /**
     * <p>
     * Name of the persistence unit. This is configured in the <code>persistence.xml</code> file.
     * </p>
     */
    public static final String SUBSCRIPTION_PERSISTENCE_UNIT_NAME = "de.spektrumprojekt.datamodel.subscriptions";

    public SubscriptionPersistence(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory);
    }

    public SubscriptionPersistence(JPAConfiguration jpaConfiguration) {
        super(jpaConfiguration, null);
    }

    /**
     * <p>
     * Removes all {@link SubscriptionWithChannel}s and {@link Subscription}s from the database.
     * </p>
     */
    public void deleteAllSubscriptions() {
        EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.createQuery("DELETE FROM Subscription").executeUpdate();
        transaction.commit();
        entityManager.close();
    }

    /**
     * <p>
     * Get the {@link Subscription} with the specified subscriptionId.
     * </p>
     * 
     * @param subscriptionId
     *            The {@link Subscription}, or <code>null</code> if no such Subscription.
     * @return
     */
    public Subscription getSubscription(String subscriptionId) {
        EntityManager entityManager = getEntityManager();
        TypedQuery<Subscription> query = entityManager.createQuery(
                "SELECT s FROM Subscription s WHERE s.globalId = :subscriptionId",
                Subscription.class);
        query.setParameter("subscriptionId", subscriptionId);
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        Subscription subscription = null;
        try {
            subscription = query.getSingleResult();
        } catch (NoResultException e) {
        }
        transaction.commit();
        entityManager.close();
        return subscription;
    }

    public List<Subscription> getSubscriptions() {
        return getAll(Subscription.class);
    }

    public Subscription saveSubscription(Subscription subscription) {
        return save(subscription);
    }

    public void update(Subscription subscription) {
        Subscription persistentSubscription = getSubscription(subscription.getGlobalId());
        subscription.setId(persistentSubscription.getId());
        EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.merge(subscription);
        transaction.commit();
        entityManager.close();
    }

}
