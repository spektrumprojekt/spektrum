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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.spektrumprojekt.datamodel.identifiable.SpektrumEntity;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.MessageRank;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.message.Term.TermCategory;
import de.spektrumprojekt.datamodel.observation.Observation;
import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.datamodel.subscription.Subscription;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.datamodel.user.UserModelEntryTimeBin;
import de.spektrumprojekt.persistence.Statistics;
import de.spektrumprojekt.persistence.jpa.JPAConfiguration;
import de.spektrumprojekt.persistence.jpa.transaction.Transaction;

// http://www.objectdb.com/java/jpa/query/jpql/logical
/**
 * Persistence layer for messages within spektrum.
 * 
 * @author Philipp Katz
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 */
public final class MessagePersistence extends AbstractPersistenceLayer {

    public MessagePersistence(JPAConfiguration jpaConfiguration) {
        super(jpaConfiguration, null);
    }

    public Statistics computeStatistics() {

        Transaction<Statistics> transaction = new Transaction<Statistics>() {

            @Override
            protected Statistics doTransaction(EntityManager entityManager) {
                Statistics statistics = new Statistics();

                statistics.setSubscriptionCount(getEntityCount(entityManager, Subscription.class));

                statistics.setMessageCount(getEntityCount(entityManager, Message.class));
                statistics.setMessageRankCount(getEntityCount(entityManager, MessageRank.class));

                statistics.setScoredTermCount(getEntityCount(entityManager, ScoredTerm.class));
                statistics.setTermCount(getEntityCount(entityManager, Term.class));

                statistics.setUserCount(getEntityCount(entityManager, User.class));
                statistics.setUserModelCount(getEntityCount(entityManager, UserModel.class));
                statistics.setUserModelEntryCount(getEntityCount(entityManager,
                        UserModelEntry.class));
                statistics.setUserModelEntryTimeBinCount(getEntityCount(entityManager,
                        UserModelEntryTimeBin.class));

                return statistics;
            }

            private Long getEntityCount(EntityManager entityManager,
                    Class<? extends SpektrumEntity> entityClazz) {
                CriteriaBuilder qb = entityManager.getCriteriaBuilder();
                CriteriaQuery<Long> cq = qb.createQuery(Long.class);
                cq.select(qb.count(cq.from(entityClazz)));
                return entityManager.createQuery(cq).getSingleResult();
            }
        };
        Statistics statistics = transaction.executeTransaction(getEntityManager());
        return statistics;
    }

    public Collection<MessageGroup> getAllMessageGroups() {
        Transaction<List<MessageGroup>> transaction = new Transaction<List<MessageGroup>>() {

            @Override
            protected List<MessageGroup> doTransaction(EntityManager entityManager) {
                CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                CriteriaQuery<MessageGroup> query = criteriaBuilder.createQuery(MessageGroup.class);
                Root<MessageGroup> entity = query.from(MessageGroup.class);
                query.select(entity);

                try {
                    return entityManager.createQuery(query).getResultList();
                } catch (NoResultException e) {
                    return null;
                }
            }
        };
        List<MessageGroup> messageGroups = transaction.executeTransaction(getEntityManager());

        return messageGroups;
    }

    public Collection<Term> getAllTerms() {
        Transaction<Collection<Term>> transaction = new Transaction<Collection<Term>>() {

            @Override
            protected Collection<Term> doTransaction(EntityManager entityManager) {
                TypedQuery<Term> query = entityManager.createQuery("select * from " + Term.class,
                        Term.class);
                return query.getResultList();
            }
        };

        return transaction.executeTransaction(getEntityManager());
    }

    /**
     * 
     * @param messageGlobalId
     *            the message global id
     * @return the message for the id
     */
    public Message getMessageByGlobalId(String messageGlobalId) {
        return this.getEntityByGlobalId(Message.class, messageGlobalId);
    }

    /**
     * Get message groups for global id
     * 
     * @param messageGroupGlobalId
     *            global id
     * @return the message group
     */
    public MessageGroup getMessageGroupByGlobalId(String messageGroupGlobalId) {
        return this.getEntityByGlobalId(MessageGroup.class, messageGroupGlobalId);
    }

    public MessageRank getMessageRank(final String userGlobalId, final String messageGlobalId) {

        Transaction<MessageRank> transaction = new Transaction<MessageRank>() {

            @Override
            protected MessageRank doTransaction(EntityManager entityManager) {
                CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                CriteriaQuery<MessageRank> query = criteriaBuilder.createQuery(MessageRank.class);
                Root<MessageRank> entity = query.from(MessageRank.class);

                ParameterExpression<String> userGlobalIdParameter = criteriaBuilder
                        .parameter(String.class);
                ParameterExpression<String> messageGlobalIdParameter = criteriaBuilder
                        .parameter(String.class);

                query.where(criteriaBuilder.and(
                        criteriaBuilder.equal(entity.get("userGlobalId"), userGlobalIdParameter),
                        criteriaBuilder.equal(entity.get("messageGlobalId"),
                                messageGlobalIdParameter)));

                TypedQuery<MessageRank> typedQuery = entityManager.createQuery(query);
                typedQuery.setParameter(userGlobalIdParameter, userGlobalId);
                typedQuery.setParameter(messageGlobalIdParameter, messageGlobalId);

                MessageRank messageRank;
                try {
                    messageRank = typedQuery.getSingleResult();
                } catch (NoResultException e) {
                    // ignore
                    messageRank = null;
                }
                return messageRank;
            }
        };

        return transaction.executeTransaction(getEntityManager());
    }

    public Collection<Message> getMessagesSince(Date fromDate) {
        return this.getMessagesSince(null, fromDate);
    }

    public List<Message> getMessagesSince(final String messageGroupGlobalId, final Date fromDate) {
        Transaction<List<Message>> transaction = new Transaction<List<Message>>() {

            @Override
            protected List<Message> doTransaction(EntityManager entityManager) {
                CriteriaBuilder cb = entityManager.getCriteriaBuilder();
                CriteriaQuery<Message> query = cb
                        .createQuery(Message.class);
                Root<Message> messageEntity = query.from(Message.class);
                query.select(messageEntity);

                Path<Date> publicationDate = messageEntity.get("publicationDate");
                Predicate one = cb.greaterThanOrEqualTo(publicationDate, fromDate);

                if (messageGroupGlobalId != null) {
                    Join<Message, MessageGroup> messageGroup = messageEntity.join("messageGroup");
                    Predicate two = cb.equal(messageGroup.get("globalId"), messageGroupGlobalId);

                    query.where(one, two);
                } else {
                    query.where(one);
                }
                query.orderBy(cb.asc(publicationDate));

                try {
                    return entityManager.createQuery(query).getResultList();
                } catch (NoResultException e) {
                    return Collections.emptyList();
                }
            }
        };
        return transaction.executeTransaction(getEntityManager());
    }

    public Collection<Observation> getObservations(final String userGlobalId,
            final String messageGlobalId, final ObservationType observationType) {
        Transaction<Collection<Observation>> transaction = new Transaction<Collection<Observation>>() {

            @Override
            protected Collection<Observation> doTransaction(EntityManager entityManager) {
                CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                CriteriaQuery<Observation> query = criteriaBuilder.createQuery(Observation.class);
                Root<Observation> entity = query.from(Observation.class);

                ParameterExpression<String> userParameter = criteriaBuilder
                        .parameter(String.class);
                ParameterExpression<String> messageParameter = criteriaBuilder
                        .parameter(String.class);
                ParameterExpression<ObservationType> otParameter = criteriaBuilder
                        .parameter(ObservationType.class);

                query.where(criteriaBuilder.and(
                        criteriaBuilder.equal(entity.get("userGlobalId"), userParameter),
                        criteriaBuilder.equal(entity.get("messageGlobalId"), messageParameter),
                        criteriaBuilder.equal(entity.get("observationType"), otParameter)));

                TypedQuery<Observation> typedQuery = entityManager.createQuery(query);
                typedQuery.setParameter(userParameter, userGlobalId);
                typedQuery.setParameter(messageParameter, messageGlobalId);
                typedQuery.setParameter(otParameter, observationType);

                return typedQuery.getResultList();
            }
        };
        return transaction.executeTransaction(getEntityManager());
    }

    public Term getOrCreateTerm(final TermCategory termCategory, final String name) {

        Transaction<Term> transaction = new Transaction<Term>() {

            @Override
            protected Term doTransaction(EntityManager entityManager) {
                CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                CriteriaQuery<Term> query = criteriaBuilder.createQuery(Term.class);
                Root<Term> entity = query.from(Term.class);

                ParameterExpression<String> valueParameter = criteriaBuilder
                        .parameter(String.class);
                ParameterExpression<TermCategory> termCategoryParameter = criteriaBuilder
                        .parameter(TermCategory.class);

                query.where(criteriaBuilder.and(
                        criteriaBuilder.equal(entity.get("value"), valueParameter),
                        criteriaBuilder.equal(entity.get("category"), termCategoryParameter)));

                TypedQuery<Term> typedQuery = entityManager.createQuery(query);
                typedQuery.setParameter(valueParameter, name);
                typedQuery.setParameter(termCategoryParameter, termCategory);

                try {
                    return typedQuery.getSingleResult();
                } catch (NoResultException e) {

                }
                // create
                Term term = new Term(termCategory, name);
                entityManager.persist(term);
                return term;
            }
        };

        return transaction.executeTransaction(getEntityManager());
    }

    public void resetTermCount() {
        Transaction<Integer> transaction = new Transaction<Integer>() {

            @Override
            protected Integer doTransaction(EntityManager entityManager) {
                Query query = entityManager.createQuery("update " + Term.class
                        + " set count = :count");
                query.setParameter("count", 0);
                int result = query.executeUpdate();
                return result;
            }
        };

        transaction.executeTransaction(getEntityManager());
    }

    public Message storeMessage(Message message) {
        Message existing = this.getMessageByGlobalId(message.getGlobalId());
        if (existing != null) {
            this.remove(existing);
        }
        return this.save(message);
    }

    /**
     * Stores the message group
     * 
     * @param messageGroup
     *            the message group
     * @return the stored message group
     */
    public MessageGroup storeMessageGroup(MessageGroup messageGroup) {
        return this.save(messageGroup);
    }

    /**
     * 
     * @param ranks
     *            store the ranks
     */
    public void storeMessageRanks(Collection<MessageRank> ranks) {
        if (ranks == null) {
            throw new IllegalArgumentException("ranks cannot be null.");
        }
        if (ranks.contains(null)) {
            throw new IllegalArgumentException("ranks cannot contain a null value.");
        }
        this.saveAll(ranks);
    }

    /**
     * Stores the message relation
     * 
     * TODO do we need this ?
     * 
     * @param message
     *            the personalization message
     * @param relatedMessages
     *            the relation
     */

    public void storeMessageRelation(Message message,
            MessageRelation relatedMessages) {

        MessageRelation persistedMessageRelation = this.getEntityByGlobalId(MessageRelation.class,
                relatedMessages.getGlobalId());
        if (persistedMessageRelation != null) {
            relatedMessages.setId(persistedMessageRelation.getId());
        }
        this.save(relatedMessages);
    }

    public void storeObservation(Observation observation) {
        this.save(observation);
    }

    public void updateTerms(Collection<Term> termsChanged) {
        this.saveAll(termsChanged);
    }

}
