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

import java.util.ArrayList;
import java.util.Collection;
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
import de.spektrumprojekt.datamodel.source.SourceNotFoundException;
import de.spektrumprojekt.datamodel.source.SourceStatus;
import de.spektrumprojekt.persistence.jpa.JPAConfiguration;
import de.spektrumprojekt.persistence.jpa.transaction.Transaction;

/**
 * <p>
 * Persistence implementation for {@link SourceStatus}.
 * </p>
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 */
public class SourcePersistence extends AbstractPersistenceLayer {

    /** The logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(SourcePersistence.class);

    public SourcePersistence(JPAConfiguration jpaConfiguration) {
        super(jpaConfiguration, null);
    }

    public void deleteSource(String sourceGlobalId) {
        Validate.notNull(sourceGlobalId, "sourceGlobalId must not be null");

        EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        TypedQuery<SourceStatus> query = entityManager
                .createQuery(
                        "SELECT st FROM SourceStatus st left join st.source s WHERE s.globalId = :sourceId",
                        SourceStatus.class);
        query.setParameter("sourceId", sourceGlobalId);
        try {
            SourceStatus sourceStatus = query.getSingleResult();
            if (sourceStatus != null) {
                entityManager.remove(sourceStatus);
            } else {
                LOGGER.debug("No SourceStatus for Source Id {}", sourceGlobalId);
            }
        } catch (NoResultException nre) {
            // ignore
        }

        Source source = findByGlobalId(entityManager, Source.class, sourceGlobalId);

        if (source != null) {
            entityManager.remove(source);
        } else {
            LOGGER.debug("No Source for Source Id {}", sourceGlobalId);
        }

        transaction.commit();
        entityManager.close();
    }

    public Source findSource(final String connectorType, final Collection<Property> accessParameters) {
        Transaction<Source> transaction = new Transaction<Source>() {

            @Override
            protected Source doTransaction(EntityManager entityManager) {

                List<Property> accessParametersList = new ArrayList<Property>();
                if (accessParameters != null) {
                    accessParametersList.addAll(accessParameters);
                }

                CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                CriteriaQuery<Source> query = criteriaBuilder.createQuery(Source.class);
                Root<Source> entity = query.from(Source.class);
                query.select(entity);

                String queryString = "SELECT source from Source source ";
                queryString += " WHERE source.connectorType = :connectorType";

                String prefix = " AND ";
                for (int i = 0; i < accessParametersList.size(); i++) {
                    queryString += prefix
                            + "exists (SELECT innerSource from Source innerSource left join innerSource.accessParameters param ";
                    queryString += " WHERE innerSource.id = source.id";
                    queryString += " AND param.propertyKey = :propertyKey" + i;
                    queryString += " AND param.propertyValue = :propertyValue" + i;
                    queryString += " )";
                }

                queryString += " AND SIZE(source.accessParameters) = :sizeOfParameters ";

                TypedQuery<Source> typedQuery = entityManager
                        .createQuery(queryString, Source.class);

                typedQuery.setParameter("connectorType", connectorType);
                typedQuery.setParameter("sizeOfParameters", accessParametersList.size());

                int i = 0;
                for (Property prop : accessParametersList) {
                    typedQuery.setParameter("propertyKey" + i, prop.getPropertyKey());
                    typedQuery.setParameter("propertyValue" + i, prop.getPropertyValue());
                    i++;
                }

                List<Source> sources = typedQuery.getResultList();
                if (sources != null && sources.size() > 1) {
                    LOGGER.error("Got more sources, but there should only be one! sources={}",
                            sources);
                }
                if (sources != null && sources.size() > 0) {
                    return sources.iterator().next();
                }
                return null;
            }
        };
        Source source = transaction.executeTransaction(getEntityManager());

        return source;
    }

    /**
     * <p>
     * Retrieve the {@link Source} for specified source ID.
     * </p>
     * 
     * @param sourceGlobalId
     *            The source ID for which to retrieve the status, not <code>null</code>.
     * @return The {@link Source} for the global id
     * @throws SourceNotFoundException
     */
    public Source getSourceByGlobalId(String sourceGlobalId) throws SourceNotFoundException {
        Validate.notNull(sourceGlobalId, "sourceGlobalId must not be null");

        Source source = this.getEntityByGlobalId(Source.class, sourceGlobalId);
        if (source == null) {
            throw new SourceNotFoundException("Source not found for sourceGlobalId="
                    + sourceGlobalId, sourceGlobalId);
        }
        return source;
    }

    public Source saveSource(Source source) {
        Validate.notNull(source, "source must not be null");

        return save(source);
    }

    public Source updateSource(Source source) throws SourceNotFoundException {
        Validate.notNull(source, "source must not be null");

        Source persistentSource = this.getSourceByGlobalId(source.getGlobalId());

        source.setId(persistentSource.getId());

        return this.save(source);
    }

}
