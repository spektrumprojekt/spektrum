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

package de.spektrumprojekt.persistence.jpa.transaction;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 * @param <T>
 */
public abstract class Transaction<T extends Object> {

    protected abstract T doTransaction(EntityManager entityManager);

    /**
     * 
     * @param entityManager
     *            the entity manager to use
     * @return the result
     */
    public T executeTransaction(EntityManager entityManager) {
        EntityTransaction transaction = null;
        boolean success = false;
        T result = null;
        try {
            transaction = entityManager.getTransaction();
            transaction.begin();

            result = doTransaction(entityManager);

            success = true;
            transaction.commit();
        } finally {
            if (!success) {
                transaction.rollback();
            }
            // TODO do we have to close it every time ?
            entityManager.close();
        }
        return result;
    }
}
