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
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.datamodel.duplicationdetection.HashWithDate;
import de.spektrumprojekt.persistence.jpa.JPAConfiguration;

/**
 * 
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 * 
 */
public class DuplicationDetectionPersistence extends AbstractPersistenceLayer {

    private static final Logger LOG = LoggerFactory
            .getLogger(DuplicationDetectionPersistence.class);

    public DuplicationDetectionPersistence(JPAConfiguration jpaConfiguration) {
        super(jpaConfiguration, null);
    }

    public void deleteHashWithDates(List<HashWithDate> hashesToDelete) {
        EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        for (HashWithDate hash : hashesToDelete) {
            LOG.trace(">remove {}", hash);
            hash = entityManager.merge(hash);
            entityManager.remove(hash);
            LOG.trace("<removed {}", hash);
        }
        transaction.commit();
        entityManager.close();

    }

    public List<HashWithDate> getHashsByGlobalSubscriptionId(String subscriptionGlobalId) {
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<HashWithDate> query = criteriaBuilder.createQuery(HashWithDate.class);
        Root<HashWithDate> hashes = query.from(HashWithDate.class);
        query.where(criteriaBuilder.equal(hashes.get("subscriptionGlobalId"), subscriptionGlobalId));
        List<HashWithDate> result;
        try {
            result = entityManager.createQuery(query).getResultList();
        } catch (NoResultException e) {
            result = new ArrayList<HashWithDate>();
        }
        entityManager.close();
        return result;
    }

    public HashWithDate saveHashWithDate(HashWithDate hashWithDate) {
        return save(hashWithDate);
    }

}
