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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.datamodel.user.UserSimilarity;
import de.spektrumprojekt.persistence.jpa.JPAConfiguration;
import de.spektrumprojekt.persistence.jpa.transaction.Transaction;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class UserPersistence extends AbstractPersistenceLayer {

    public UserPersistence(JPAConfiguration jpaConfiguration) {
        super(jpaConfiguration, null);
    }

    public void deleteAndCreateUserSimilarities(final Collection<UserSimilarity> userSimilarities) {
        Transaction<Object> transaction = new Transaction<Object>() {

            @Override
            protected Object doTransaction(EntityManager entityManager) {
                Query query = entityManager.createQuery("DELETE from UserSimilarity");
                query.executeUpdate();

                for (UserSimilarity userSimilarity : userSimilarities) {
                    entityManager.persist(userSimilarity);
                }
                return null;
            }
        };
        transaction.executeTransaction(getEntityManager());

    }

    public Collection<User> getAllUsers() {

        Transaction<List<User>> transaction = new Transaction<List<User>>() {

            @Override
            protected List<User> doTransaction(EntityManager entityManager) {
                CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                CriteriaQuery<User> query = criteriaBuilder.createQuery(User.class);
                Root<User> entity = query.from(User.class);
                query.select(entity);

                try {
                    return entityManager.createQuery(query).getResultList();
                } catch (NoResultException e) {
                    return null;
                }
            }
        };
        List<User> users = transaction.executeTransaction(getEntityManager());

        return users;
    }

    public User getOrCreateUser(final String userGlobalId) {
        User user = getEntityByGlobalId(User.class, userGlobalId);
        if (user == null) {

            Transaction<User> transaction = new Transaction<User>() {

                @Override
                protected User doTransaction(EntityManager entityManager) {
                    User user = new User(userGlobalId);
                    entityManager.persist(user);
                    return user;
                }
            };
            user = transaction.executeTransaction(getEntityManager());
        }
        return user;
    }

    /**
     * 
     * @param userGlobalId
     *            the users id
     * @return the user model
     */
    public UserModel getOrCreateUserModelByUser(final String userGlobalId,
            final String userModelType) {
        Transaction<UserModel> transaction = new Transaction<UserModel>() {

            @Override
            protected UserModel doTransaction(EntityManager entityManager) {
                CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                CriteriaQuery<UserModel> query = criteriaBuilder.createQuery(UserModel.class);
                Root<UserModel> entity = query.from(UserModel.class);

                query.where(
                        criteriaBuilder.and(
                                criteriaBuilder.equal(entity.get("user").get("globalId"),
                                        userGlobalId),
                                criteriaBuilder.equal(entity.get("userModelType"), userModelType)
                                )
                        );

                try {
                    return entityManager.createQuery(query).getSingleResult();
                } catch (NoResultException e) {
                    return null;
                }
            }
        };

        UserModel result = transaction.executeTransaction(getEntityManager());
        if (result == null) {

            final User user = getOrCreateUser(userGlobalId);

            Transaction<UserModel> createUserModelTransaction = new Transaction<UserModel>() {

                @Override
                protected UserModel doTransaction(EntityManager entityManager) {
                    UserModel userModel = new UserModel(user, userModelType);
                    entityManager.persist(userModel);
                    return userModel;
                }
            };

            result = createUserModelTransaction.executeTransaction(getEntityManager());
        }

        return result;
    }

    /**
     * get the user model entry for the term
     * 
     * @param userModel
     *            the user model
     * @param term
     *            the term
     * @return the user model entry
     */
    public Map<Term, UserModelEntry> getUserModelEntriesForTerms(final UserModel userModel,
            final Collection<Term> terms) {
        if (terms == null || terms.size() == 0) {
            return Collections.emptyMap();
        }
        Transaction<Map<Term, UserModelEntry>> transaction = new Transaction<Map<Term, UserModelEntry>>() {

            /**
             * {@inheritedDoc}
             */
            @Override
            protected Map<Term, UserModelEntry> doTransaction(EntityManager entityManager) {

                Collection<Long> termIds = new ArrayList<Long>(terms.size());
                for (Term t : terms) {
                    termIds.add(t.getId());
                }

                String qlString = "";
                qlString += "select entries ";
                qlString += "from UserModelEntry entries ";
                qlString += "left join entries.scoredTerm sTerm ";
                qlString += "left join sTerm.term term ";
                qlString += "left join entries.userModel userModel ";
                qlString += "where userModel.id = :userModelId ";
                qlString += "and term.id in :termIds";

                TypedQuery<UserModelEntry> typedQuery = entityManager.createQuery(qlString,
                        UserModelEntry.class);
                typedQuery.setParameter("userModelId", userModel.getId());
                typedQuery.setParameter("termIds", termIds);

                // CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                // CriteriaQuery<UserModelEntry> query = criteriaBuilder
                // .createQuery(UserModelEntry.class);
                // Root<UserModelEntry> entity = query.from(UserModelEntry.class);
                // Join<Object, Object> userModel = entity.join("userModel");
                // Join<Object, Object> scoredTerms = entity.join("scoredTerm");
                // Join<Object, Object> term = scoredTerms.join("term");
                // criteriaBuilder.isTrue(term.in(terms));
                //
                // TODO parameters must be set differently
                // query.where(criteriaBuilder.equal(userModel, userModel));
                // TypedQuery<UserModelEntry> typedQuery = entityManager.createQuery(query);

                Map<Term, UserModelEntry> resultMap = new HashMap<Term, UserModelEntry>();
                try {
                    List<UserModelEntry> userModelEntries = typedQuery.getResultList();

                    for (UserModelEntry entry : userModelEntries) {
                        resultMap.put(entry.getScoredTerm().getTerm(), entry);
                    }

                } catch (NoResultException e) {
                }
                return resultMap;

            }
        };

        return transaction.executeTransaction(getEntityManager());
    }

    public Collection<UserSimilarity> getUserSimilarities(final String messageGroupGlobalId) {

        Transaction<Collection<UserSimilarity>> transaction = new Transaction<Collection<UserSimilarity>>() {

            @Override
            protected Collection<UserSimilarity> doTransaction(EntityManager entityManager) {
                CriteriaBuilder cb = entityManager.getCriteriaBuilder();
                CriteriaQuery<UserSimilarity> query = cb
                        .createQuery(UserSimilarity.class);
                Root<UserSimilarity> userSim = query.from(UserSimilarity.class);
                if (messageGroupGlobalId != null) {
                    Predicate mg = cb.equal(userSim.get("messageGroupGlobalId"),
                            messageGroupGlobalId);
                    query.where(mg);
                }

                try {
                    return entityManager.createQuery(query).getResultList();
                } catch (NoResultException e) {
                    return Collections.emptyList();
                }
            }
        };
        return transaction.executeTransaction(getEntityManager());
    }

    public Collection<UserSimilarity> getUserSimilarities(final String userGlobalId,
            final Collection<String> users, final String messageGroupGlobalId,
            final double userSimilarityThreshold) {
        if (userGlobalId == null) {
            throw new IllegalArgumentException("userGlobalId cannot be null.");
        }
        if (users == null || users.isEmpty()) {
            throw new IllegalArgumentException("users cannot be null or empty.");
        }
        Transaction<Collection<UserSimilarity>> transaction = new Transaction<Collection<UserSimilarity>>() {

            @Override
            protected Collection<UserSimilarity> doTransaction(EntityManager entityManager) {
                CriteriaBuilder cb = entityManager.getCriteriaBuilder();
                CriteriaQuery<UserSimilarity> query = cb
                        .createQuery(UserSimilarity.class);
                Root<UserSimilarity> userSim = query.from(UserSimilarity.class);

                Path<Number> sim = userSim.get("similarity");

                Predicate zero = cb.equal(userSim.get("userGlobalIdFrom"), userGlobalId);
                Predicate one = userSim.get("userGlobalIdTo").in(users);
                Predicate two = cb.gt(sim, userSimilarityThreshold);

                if (messageGroupGlobalId != null) {
                    Predicate three = cb.equal(userSim.get("messageGroupGlobalId"),
                            messageGroupGlobalId);
                    query.where(zero, one, two, three);
                } else {
                    query.where(zero, one, two);
                }

                try {
                    return entityManager.createQuery(query).getResultList();
                } catch (NoResultException e) {
                    return Collections.emptyList();
                }
            }
        };
        return transaction.executeTransaction(getEntityManager());
    }

    public UserSimilarity getUserSimilarity(final String userGlobalIdFrom,
            final String userGlobalIdTo,
            final String messageGroupGlobalId) {
        if (userGlobalIdFrom == null) {
            throw new IllegalArgumentException("userGlobalIdFrom cannot be null.");
        }
        if (userGlobalIdTo == null) {
            throw new IllegalArgumentException("userGlobalIdTo cannot be null or empty.");
        }
        Transaction<UserSimilarity> transaction = new Transaction<UserSimilarity>() {

            @Override
            protected UserSimilarity doTransaction(EntityManager entityManager) {
                CriteriaBuilder cb = entityManager.getCriteriaBuilder();
                CriteriaQuery<UserSimilarity> query = cb
                        .createQuery(UserSimilarity.class);
                Root<UserSimilarity> userSim = query.from(UserSimilarity.class);

                Predicate zero = cb.equal(userSim.get("userGlobalIdFrom"), userGlobalIdFrom);
                Predicate one = cb.equal(userSim.get("userGlobalIdTo"), userGlobalIdTo);

                if (messageGroupGlobalId != null) {
                    Predicate two = cb.equal(userSim.get("messageGroupGlobalId"),
                            messageGroupGlobalId);
                    query.where(zero, one, two);
                } else {
                    query.where(zero, one);
                }

                try {
                    return entityManager.createQuery(query).getSingleResult();
                } catch (NoResultException e) {
                    return null;
                }
            }
        };
        return transaction.executeTransaction(getEntityManager());
    }

    public Collection<UserModel> getUsersWithUserModel(final Collection<Term> terms,
            final String userModelType) {
        if (terms == null || terms.isEmpty()) {
            throw new IllegalArgumentException("terms cannot be null or empty.");
        }
        Transaction<Collection<UserModel>> transaction = new Transaction<Collection<UserModel>>() {

            @Override
            protected Collection<UserModel> doTransaction(EntityManager entityManager) {
                CriteriaBuilder cb = entityManager.getCriteriaBuilder();
                CriteriaQuery<UserModel> query = cb
                        .createQuery(UserModel.class);
                Root<UserModelEntry> userModelEntry = query.from(UserModelEntry.class);
                Join<UserModelEntry, UserModel> userModel = userModelEntry.join("userModel");
                Join<UserModelEntry, ScoredTerm> scoredTerm = userModelEntry.join("scoredTerm");
                Join<ScoredTerm, Term> term = scoredTerm.join("term");

                query.distinct(true);
                query.select(userModel);
                query.where(cb.and(
                        cb.equal(userModel.get("userModelType"), userModelType),
                        term.in(terms))
                        );

                try {
                    return entityManager.createQuery(query).getResultList();
                } catch (NoResultException e) {
                    return Collections.emptyList();
                }
            }
        };
        return transaction.executeTransaction(getEntityManager());
    }

    public void removeUserModelEntry(UserModel userModel, UserModelEntry userModelEntry) {
        this.getEntityManager().remove(userModelEntry);
    }

    /**
     * Store(create) or update the changed user model
     * 
     * @param userModel
     *            the user model
     * @param changedEntries
     *            the changed entries
     */
    public Collection<UserModelEntry> storeOrUpdateUserModelEntries(UserModel userModel,
            Collection<UserModelEntry> changedEntries) {
        return this.saveAll(changedEntries);
    }

    public void storeUserSimilarity(UserSimilarity stat) {
        this.save(stat);
    }

}
