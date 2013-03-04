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
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.datamodel.subscription.SubscriptionStatus;
import de.spektrumprojekt.persistence.jpa.JPAConfiguration;

/**
 * <p>
 * Persistence implementation for {@link SubscriptionStatus}.
 * </p>
 * 
 * @author Philipp Katz
 */
public class AggregationSubscriptionPersistence extends AbstractPersistenceLayer {

    /** The logger for this class. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AggregationSubscriptionPersistence.class);

    public AggregationSubscriptionPersistence(JPAConfiguration jpaConfiguration) {
        super(jpaConfiguration, null);
    }

    public void deleteAggregationSubscription(String subscriptionId) {
        Validate.notNull(subscriptionId, "subscriptionId must not be null");

        EntityManager entityManager = getEntityManager();
        TypedQuery<SubscriptionStatus> query = entityManager
                .createQuery(
                        "SELECT status FROM SubscriptionStatus status left join status.subscription s WHERE s.globalId = :subscriptionId",
                        SubscriptionStatus.class);
        query.setParameter("subscriptionId", subscriptionId);
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        try {
            SubscriptionStatus result = query.getSingleResult();
            entityManager.remove(result);
        } catch (NoResultException e) {
            LOGGER.warn("No AggregationSubscription for Subscription ID {}", subscriptionId);
        }
        transaction.commit();
        entityManager.close();
    }

    public void deleteAll() {
        EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.createQuery("DELETE FROM SubscriptionStatus").executeUpdate();
        entityManager.createQuery("DELETE FROM Subscription").executeUpdate();
        transaction.commit();
        entityManager.close();
    }

    /**
     * <p>
     * Retrieve the {@link SubscriptionStatus} for the subscription with the specified subscription
     * ID.
     * </p>
     * 
     * @param subscriptionGlobalId
     *            The subscription ID for which to retrieve the status, not <code>null</code>.
     * @return The status for the specified subscription ID, nor <code>null</code> if no
     *         subscription with specified ID exists.
     */
    public SubscriptionStatus getAggregationSubscription(String subscriptionGlobalId) {
        Validate.notNull(subscriptionGlobalId, "subscriptionGlobalId must not be null");
        EntityManager entityManager = getEntityManager();
        TypedQuery<SubscriptionStatus> query = entityManager
                .createQuery(
                        "SELECT st FROM SubscriptionStatus st left join st.subscription s WHERE s.globalId = :subscriptionId",
                        SubscriptionStatus.class);
        query.setParameter("subscriptionId", subscriptionGlobalId);
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        SubscriptionStatus aggregationStatus = null;
        try {
            aggregationStatus = query.getSingleResult();
        } catch (NoResultException e) {
            LOGGER.warn("No AgregationStatus for Subscription ID {}", subscriptionGlobalId);
        }
        transaction.commit();
        entityManager.close();
        return aggregationStatus;
    }

    /**
     * <p>
     * Gets all {@link SubscriptionStatus}.
     * </p>
     * 
     * @return A list with all {@link SubscriptionStatus}, or an empty list. Never <code>null</code>
     *         .
     */
    public List<SubscriptionStatus> getAggregationSubscriptions() {
        return getAll(SubscriptionStatus.class);
    }

    public SubscriptionStatus saveAggregationSubscription(SubscriptionStatus subscription) {
        Validate.notNull(subscription, "status must not be null");
        return save(subscription);
    }

    public SubscriptionStatus updateAggregationSubscription(SubscriptionStatus subscriptionStatus) {
        Validate.notNull(subscriptionStatus, "status must not be null");

        SubscriptionStatus persistentStatus = getAggregationSubscription(subscriptionStatus
                .getSubscription().getGlobalId());
        if (persistentStatus == null) {
            throw new IllegalStateException(
                    "The supplied AggregationSubscription does not exist; cannot update.");
        }
        subscriptionStatus.setId(persistentStatus.getId());
        return this.save(subscriptionStatus);
    }

}
