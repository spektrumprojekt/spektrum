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
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.configuration.SpektrumConfiguration;
import de.spektrumprojekt.datamodel.identifiable.Identifiable;
import de.spektrumprojekt.datamodel.identifiable.SpektrumEntity;
import de.spektrumprojekt.persistence.jpa.JPAConfiguration;
import de.spektrumprojekt.persistence.jpa.transaction.Transaction;

/**
 * <p>
 * Base class for persistence layer implementations with common functionality.
 * </p>
 * 
 * @author Philipp Katz
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 */
public abstract class AbstractPersistenceLayer {

    /**
     * <p>
     * The logger for this class. It can be configured using the <code>log4j.xml</code> file.
     * </p>
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPersistenceLayer.class);

    /**
     * <p>
     * Name of the persistence unit. This is configured in the <code>persistence.xml</code> file.
     * </p>
     */
    public static final String PERSISTENCE_UNIT_NAME = "de.spektrumprojekt.datamodel";

    /**
     * <p>
     * The factory used to create {@link EntityManager}s.
     * </p>
     */
    private final EntityManagerFactory entityManagerFactory;

    /**
     * <p>
     * Initialize a new {@link AbstractPersistenceLayer}.
     * </p>
     * 
     * @param entityManagerFactory
     */
    protected AbstractPersistenceLayer(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * <p>
     * Initialize a new {@link AbstractPersistenceLayer} with the specified persistence unit.
     * </p>
     * 
     * @param persistenceUnit
     * @param javaxPersistenceJdbcUrlSuffix
     */
    protected AbstractPersistenceLayer(JPAConfiguration jpaConfiguration,
            String javaxPersistenceJdbcUrlSuffix) {
        Properties properties = jpaConfiguration.getJpaProperties();
        if (javaxPersistenceJdbcUrlSuffix != null
                && properties.containsKey(SpektrumConfiguration.JAVAX_PERSISTENCE_JDBC_URL)) {
            Properties propertiesTmp = new Properties();
            propertiesTmp.putAll(properties);
            properties = propertiesTmp;
            String url = (String) properties.get(SpektrumConfiguration.JAVAX_PERSISTENCE_JDBC_URL);
            url += "_" + javaxPersistenceJdbcUrlSuffix;
            properties.put(SpektrumConfiguration.JAVAX_PERSISTENCE_JDBC_URL, url);
        }
        entityManagerFactory = Persistence.createEntityManagerFactory(
                jpaConfiguration.getPersistenceUnitName(), properties);

        // check the entity manager
        EntityManager em = entityManagerFactory.createEntityManager();
        em.close();
    }

    private <T extends Identifiable> T findByGlobalId(EntityManager entityManager,
            final Class<T> clazz, final String globalId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = criteriaBuilder.createQuery(clazz);
        Root<T> entity = query.from(clazz);
        query.where(criteriaBuilder.equal(entity.get("globalId"), globalId));

        try {
            return entityManager.createQuery(query).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * <p>
     * Return all entities of a specific type.
     * </p>
     * 
     * @param type
     *            The type of entities to retrieve.
     * @return
     */
    protected final <T> List<T> getAll(Class<T> type) {
        LOGGER.trace(">getAll " + type);
        EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = criteriaBuilder.createQuery(type);
        transaction.begin();
        List<T> result = entityManager.createQuery(query).getResultList();
        transaction.commit();
        entityManager.close();
        LOGGER.trace("<getAll {}", result.size());
        return result;
    }

    /**
     * Get the given identifiable identity by using the global id
     * 
     * @param clazz
     *            the type of the identifiable to get
     * @param globalId
     *            the id to match
     * @return the found entity with the id or null
     */
    protected <T extends Identifiable> T getEntityByGlobalId(final Class<T> clazz,
            final String globalId) {
        Validate.notNull(globalId, "globalId must not be null");

        final EntityManager entityManager = getEntityManager();

        Transaction<T> t = new Transaction<T>() {

            @Override
            protected T doTransaction(EntityManager entityManager) {
                return findByGlobalId(entityManager, clazz, globalId);
            }

        };

        return t.executeTransaction(entityManager);

    }

    protected <T extends Identifiable> T getEntityById(final Class<T> type, final Long id) {
        Transaction<T> transaction = new Transaction<T>() {

            @Override
            protected T doTransaction(EntityManager entityManager) {
                T result = entityManager.find(type, id);
                return result;
            }
        };
        return transaction.executeTransaction(getEntityManager());
    }

    protected final EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    protected final <T extends Identifiable> void remove(final T object) {
        if (object == null) {
            throw new IllegalArgumentException("cannot remove null object.");
        }
        Transaction<Object> transaction = new Transaction<Object>() {

            @Override
            protected Object doTransaction(EntityManager entityManager) {
                Object managed = entityManager.find(object.getClass(), object.getId());
                entityManager.remove(managed);
                return null;
            }
        };
        transaction.executeTransaction(getEntityManager());
    }

    /**
     * <p>
     * Saves an arbitrary entity.
     * </p>
     * 
     * @param object
     *            The entity to save.
     * @return The saved entity.
     */
    protected final <T extends SpektrumEntity> T save(T object) {
        Validate.notNull(object, "object cannot be null");
        LOGGER.trace(">save {}", object);
        EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        T result = entityManager.merge(object);
        transaction.commit();
        entityManager.close();
        LOGGER.trace("<save {}", result);
        return result;
    }

    /**
     * Saves all objects
     * 
     * @param objects
     *            the to save
     * @return the resulting objects
     */
    protected <T extends SpektrumEntity> List<T> saveAll(final Collection<T> objects) {
        Validate.notNull(objects, "objects cannot be null");

        Transaction<List<T>> transaction = new Transaction<List<T>>() {

            @Override
            protected List<T> doTransaction(EntityManager entityManager) {
                final List<T> result = new ArrayList<T>(objects.size());
                for (T obj : objects) {
                    obj = entityManager.merge(obj);
                    result.add(obj);
                }
                return result;
            }
        };
        return transaction.executeTransaction(getEntityManager());
    }

    /**
     * <p>
     * Shuts the persistence layer down. Call this method when you are done using an instance of
     * this class.
     * </p>
     * 
     */
    public final void shutdown() {
        if (entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

}
