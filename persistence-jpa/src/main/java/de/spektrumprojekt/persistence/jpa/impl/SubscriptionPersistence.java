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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.datamodel.subscription.Subscription;
import de.spektrumprojekt.datamodel.subscription.SubscriptionFilter;
import de.spektrumprojekt.datamodel.subscription.SubscriptionSourceStatus;
import de.spektrumprojekt.exceptions.SubscriptionNotFoundException;
import de.spektrumprojekt.persistence.jpa.JPAConfiguration;
import de.spektrumprojekt.persistence.jpa.transaction.Transaction;

/**
 * 
 * @author Philipp Katz
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public final class SubscriptionPersistence extends AbstractPersistenceLayer {

    /** The logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionPersistence.class);

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

    public void deleteSubscription(final String subscriptionGlobalId) {
        Transaction<Object> transaction = new Transaction<Object>() {

            @Override
            protected Object doTransaction(EntityManager entityManager) {
                Subscription subscription = findByGlobalId(entityManager, Subscription.class,
                        subscriptionGlobalId);

                if (subscription != null) {
                    entityManager.remove(subscription);
                } else {
                    LOGGER.warn("No Subscription for subscriptionId {}", subscriptionGlobalId);
                }
                return null;
            }
        };

        transaction.executeTransaction(getEntityManager());
    }

    public int getNumberOfSubscriptionsBySourceGlobalId(final String sourceGlobalId) {
        Transaction<Number> transaction = new Transaction<Number>() {

            @Override
            protected Number doTransaction(EntityManager entityManager) {
                TypedQuery<Number> query = entityManager
                        .createQuery(
                                "SELECT count(subscription.id)"
                                        + " FROM Subscription subscription left join subscription.source source"
                                        + " WHERE source.globalId = :sourceGlobalId", Number.class);
                query.setParameter("sourceGlobalId", sourceGlobalId);

                Number result = query.getSingleResult();
                return result;
            }
        };

        Number result = transaction.executeTransaction(getEntityManager());

        return result == null ? 0 : result.intValue();
    }

    /**
     * <p>
     * Get the {@link Subscription} with the specified subscriptionId.
     * </p>
     * 
     * @param subscriptionId
     *            The {@link Subscription}, or <code>null</code> if no such Subscription.
     * @return
     * @throws SubscriptionNotFoundException
     */
    public Subscription getSubscriptionByGlobalId(final String subscriptionGlobalId)
            throws SubscriptionNotFoundException {

        Transaction<Subscription> transaction = new Transaction<Subscription>() {

            @Override
            protected Subscription doTransaction(EntityManager entityManager) {
                Subscription subscription = null;
                try {

                    TypedQuery<Subscription> query = entityManager
                            .createQuery(
                                    "SELECT s FROM Subscription s WHERE s.globalId = :subscriptionGlobalId",
                                    Subscription.class);
                    query.setParameter("subscriptionGlobalId", subscriptionGlobalId);

                    subscription = query.getSingleResult();

                } catch (NoResultException e) {
                }
                return subscription;
            }
        };

        Subscription subscription = transaction.executeTransaction(getEntityManager());
        if (subscription == null) {
            throw new SubscriptionNotFoundException(
                    "No subscription found for subscriptionGlobalId=" + subscriptionGlobalId,
                    subscriptionGlobalId);
        }
        return subscription;
    }

    public List<Subscription> getSubscriptions() {
        return getAll(Subscription.class);
    }

    public List<Subscription> getSubscriptions(final SubscriptionFilter subscriptionFilter) {
        Transaction<List<Subscription>> transaction = new Transaction<List<Subscription>>() {

            @Override
            protected List<Subscription> doTransaction(EntityManager entityManager) {
                Map<String, Object> params = new HashMap<String, Object>();

                String select = "subscription ";
                StringBuilder from = new StringBuilder();
                StringBuilder where = new StringBuilder();

                from.append("Subscription subscription ");

                String wherePrefix = "";

                if (renderSourceGlobalIdCondition(subscriptionFilter, where, wherePrefix, params)) {
                    from.append("left join subscription.source source ");
                    wherePrefix = " AND ";
                }
                renderSubscriptionPropertyCondition(subscriptionFilter, from, where, wherePrefix, params);

                StringBuilder q = new StringBuilder();
                q.append("SELECT ");
                q.append(select);
                q.append("FROM ");
                q.append(from);
                q.append("WHERE ");
                q.append(where);
                q.append(" ORDER BY subscription.id desc");

                TypedQuery<Subscription> query = entityManager.createQuery(q.toString(),
                        Subscription.class);

                for (Entry<String, Object> param : params.entrySet()) {
                    query.setParameter(param.getKey(), param.getValue());
                }

                List<Subscription> subscriptions = query.getResultList();
                return subscriptions;
            }
        };

        List<Subscription> result = transaction.executeTransaction(getEntityManager());

        return result;
    }
    
    public List<SubscriptionSourceStatus> getSubscriptionsWithSourceStatus(final SubscriptionFilter subscriptionFilter) {
        Transaction<List<SubscriptionSourceStatus>> transaction = new Transaction<List<SubscriptionSourceStatus>>() {

            @Override
            protected List<SubscriptionSourceStatus> doTransaction(EntityManager entityManager) {
                Map<String, Object> params = new HashMap<String, Object>();
                StringBuilder from = new StringBuilder();
                StringBuilder where = new StringBuilder();
                
                from.append("FROM SourceStatus sourceStatus left join sourceStatus.source source, ");
                from.append("Subscription subscription ");
                where.append("subscription.source.id = source.id");
                String wherePrefix = " AND ";

                renderSourceGlobalIdCondition(subscriptionFilter, where, wherePrefix, params);
                renderSubscriptionPropertyCondition(subscriptionFilter, from, where, wherePrefix, params);
                
                StringBuilder q = new StringBuilder();
                q.append("SELECT new ");
                q.append(SubscriptionSourceStatus.class.getName());
                q.append("(subscription, sourceStatus) ");
                q.append(from);
                q.append("WHERE ");
                q.append(where);
                q.append(" ORDER BY subscription.id desc");
                Query query = entityManager.createQuery(q.toString());
                for (Entry<String, Object> param : params.entrySet()) {
                    query.setParameter(param.getKey(), param.getValue());
                }

                return (List<SubscriptionSourceStatus>)query.getResultList();
            }
        };
        return transaction.executeTransaction(getEntityManager());
    }
    
    private boolean renderSourceGlobalIdCondition(SubscriptionFilter subscriptionFilter,  
        StringBuilder wherePart, String wherePrefix, Map<String, Object> queryParams) {
        if (subscriptionFilter.getSourceGlobalId() != null) {
            wherePart.append(wherePrefix);
            wherePart.append("source.globalId = :sourceGlobalId ");
            queryParams.put("sourceGlobalId", subscriptionFilter.getSourceGlobalId());
            return true;
        }
        return false;
    }
    
    private boolean renderSubscriptionPropertyCondition(SubscriptionFilter subscriptionFilter,  
        StringBuilder fromPart, StringBuilder wherePart, String wherePrefix, 
        Map<String, Object> queryParams) {
        if (subscriptionFilter.getSubscriptionProperty() != null) {
            fromPart.append("left join subscription.subscriptionParameters params ");
            wherePart.append(wherePrefix);
            wherePart.append("params.propertyKey = :propertyKey AND params.propertyValue = :propertyValue ");

            queryParams.put("propertyKey", subscriptionFilter.getSubscriptionProperty().getPropertyKey());
            queryParams.put("propertyValue", subscriptionFilter.getSubscriptionProperty().getPropertyValue());
            return true;
        }
        return false;
    }

    public Subscription storeSubscription(Subscription subscription) {
        return save(subscription);
    }

    public Subscription updateSubscription(Subscription subscription)
            throws SubscriptionNotFoundException {

        EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        Subscription persistentSubscription = findByGlobalId(entityManager, Subscription.class,
                subscription.getGlobalId());
        if (persistentSubscription == null) {
            throw new SubscriptionNotFoundException("Subscription not found for "
                    + subscription.getGlobalId(), subscription.getGlobalId());
        }
        subscription.setId(persistentSubscription.getId());

        persistentSubscription = entityManager.merge(subscription);

        transaction.commit();
        entityManager.close();

        return subscription;
    }
}
