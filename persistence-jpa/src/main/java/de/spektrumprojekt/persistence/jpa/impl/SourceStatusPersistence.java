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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.source.Source;
import de.spektrumprojekt.datamodel.source.SourceStatus;
import de.spektrumprojekt.persistence.jpa.JPAConfiguration;
import de.spektrumprojekt.persistence.jpa.transaction.Transaction;

/**
 * <p>
 * Persistence implementation for {@link SourceStatus}.
 * </p>
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * @author Philipp Katz
 */
public class SourceStatusPersistence extends AbstractPersistenceLayer {

    /** The logger for this class. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(SourceStatusPersistence.class);

    public SourceStatusPersistence(JPAConfiguration jpaConfiguration) {
        super(jpaConfiguration, null);
    }

    public void deleteAll() {
        EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.createQuery("DELETE FROM SourceStatus").executeUpdate();
        entityManager.createQuery("DELETE FROM Source").executeUpdate();
        transaction.commit();
        entityManager.close();
    }

    public void deleteSourceStatus(String sourceId) {
        Validate.notNull(sourceId, "sourceId must not be null");

        EntityManager entityManager = getEntityManager();
        TypedQuery<SourceStatus> query = entityManager
                .createQuery(
                        "SELECT status FROM SourceStatus status left join status.source s WHERE s.globalId = :sourceId",
                        SourceStatus.class);
        query.setParameter("sourceId", sourceId);
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        try {
            SourceStatus result = query.getSingleResult();
            entityManager.remove(result);
        } catch (NoResultException e) {
            LOGGER.warn("No SourceStatus for Source ID {}", sourceId);
        }
        transaction.commit();
        entityManager.close();
    }

    public List<SourceStatus> findSourceStatusByProperty(final Property property) {
        Transaction<List<SourceStatus>> transaction = new Transaction<List<SourceStatus>>() {

            @Override
            protected List<SourceStatus> doTransaction(EntityManager entityManager) {

                CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                CriteriaQuery<Source> query = criteriaBuilder.createQuery(Source.class);
                Root<Source> entity = query.from(Source.class);
                query.select(entity);

                StringBuilder q = new StringBuilder();
                q.append("SELECT sourceStatus ");
                q.append("from SourceStatus sourceStatus ");
                q.append("left join sourceStatus.properties property ");
                q.append("WHERE ");
                q.append("property.propertyKey = :propertyKey ");
                q.append("AND property.propertyValue = :propertyValue ");

                TypedQuery<SourceStatus> typedQuery = entityManager
                        .createQuery(q.toString(), SourceStatus.class);

                typedQuery.setParameter("propertyKey", property.getPropertyKey());
                typedQuery.setParameter("propertyValue", property.getPropertyValue());

                List<SourceStatus> sourceStatus = typedQuery.getResultList();

                return sourceStatus;
            }
        };
        return transaction.executeTransaction(getEntityManager());
    }

    /**
     * <p>
     * Retrieve the {@link SourceStatus} for the source with the specified source ID.
     * </p>
     * 
     * @param sourceGlobalId
     *            The source ID for which to retrieve the status, not <code>null</code>.
     * @return The status for the specified source ID, nor <code>null</code> if no source with
     *         specified ID exists.
     */
    public SourceStatus getSourceStatusBySourceGlobalId(String sourceGlobalId) {
        Validate.notNull(sourceGlobalId, "sourceGlobalId must not be null");
        EntityManager entityManager = getEntityManager();
        TypedQuery<SourceStatus> query = entityManager
                .createQuery(
                        "SELECT st FROM SourceStatus st left join st.source s WHERE s.globalId = :sourceId",
                        SourceStatus.class);
        query.setParameter("sourceId", sourceGlobalId);
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        SourceStatus sourceStatus = null;
        try {
            sourceStatus = query.getSingleResult();
        } catch (NoResultException e) {
            LOGGER.warn("No SourceStatus for Source ID {}", sourceGlobalId);
        }
        transaction.commit();
        entityManager.close();
        return sourceStatus;
    }

    /**
     * <p>
     * Gets all {@link SourceStatus}.
     * </p>
     * 
     * @return A list with all {@link SourceStatus}, or an empty list. Never <code>null</code> .
     */
    public List<SourceStatus> getSourceStatusList() {
        return getAll(SourceStatus.class);
    }

    public SourceStatus storeSourceStatus(SourceStatus source) {
        Validate.notNull(source, "status must not be null");
        return save(source);
    }

    public SourceStatus updateSourceStatus(SourceStatus sourceStatus) {
        Validate.notNull(sourceStatus, "status must not be null");

        SourceStatus persistentStatus = getSourceStatusBySourceGlobalId(sourceStatus.getSource()
                .getGlobalId());
        if (persistentStatus == null) {
            throw new IllegalStateException(
                    "The supplied AggregationSubscription does not exist; cannot update.");
        }
        sourceStatus.setId(persistentStatus.getId());
        return this.save(sourceStatus);
    }

}
