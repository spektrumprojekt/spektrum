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

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.datamodel.source.Source;
import de.spektrumprojekt.datamodel.source.SourceStatus;
import de.spektrumprojekt.persistence.jpa.JPAConfiguration;

/**
 * <p>
 * Persistence implementation for {@link SourceStatus}.
 * </p>
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 */
public class SourcePersistence extends AbstractPersistenceLayer {

    /** The logger for this class. */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(SourcePersistence.class);

    public SourcePersistence(JPAConfiguration jpaConfiguration) {
        super(jpaConfiguration, null);
    }

    public void deleteSource(String sourceGlobalId) {
        Validate.notNull(sourceGlobalId, "sourceGlobalId must not be null");

        EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();

        Source source = findByGlobalId(entityManager, Source.class, sourceGlobalId);

        if (source != null) {
            entityManager.remove(source);
        } else {
            LOGGER.warn("No SourceStatus for Source ID {}", sourceGlobalId);
        }

        transaction.commit();
        entityManager.close();
    }

    /**
     * <p>
     * Retrieve the {@link Source} for specified source ID.
     * </p>
     * 
     * @param sourceGlobalId
     *            The source ID for which to retrieve the status, not <code>null</code>.
     * @return The {@link Source} for the global id
     */
    public Source getSourceByGlobalId(String sourceGlobalId) {
        Validate.notNull(sourceGlobalId, "sourceGlobalId must not be null");

        return this.getEntityByGlobalId(Source.class, sourceGlobalId);
    }

    public Source saveSource(Source source) {
        Validate.notNull(source, "source must not be null");

        return save(source);
    }

    public Source updateSource(Source source) {
        Validate.notNull(source, "source must not be null");

        Source persistentSource = this.getSourceByGlobalId(source.getGlobalId());
        if (persistentSource == null) {
            throw new IllegalStateException(
                    "The supplied Source does not exist; cannot update.");
        }
        source.setId(persistentSource.getId());

        return this.save(source);
    }

}
