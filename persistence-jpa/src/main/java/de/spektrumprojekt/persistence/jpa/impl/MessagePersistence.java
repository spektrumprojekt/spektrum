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
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.Validate;

import de.spektrumprojekt.datamodel.identifiable.SpektrumEntity;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageFilter;
import de.spektrumprojekt.datamodel.message.MessageFilter.OrderDirection;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.MessagePattern;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.message.Term.TermCategory;
import de.spektrumprojekt.datamodel.message.TermFrequency;
import de.spektrumprojekt.datamodel.message.UserMessageScore;
import de.spektrumprojekt.datamodel.observation.Observation;
import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.datamodel.subscription.Subscription;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.datamodel.user.UserModelEntryTimeBin;
import de.spektrumprojekt.persistence.UserMessageScoreVisitor;
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
public class MessagePersistence extends AbstractPersistenceLayer {

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
                statistics.setMessageScoreCount(getEntityCount(entityManager,
                        UserMessageScore.class));

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

    public UserMessageScore getMessageRank(final String userGlobalId, final String messageGlobalId) {

        Transaction<UserMessageScore> transaction = new Transaction<UserMessageScore>() {

            @Override
            protected UserMessageScore doTransaction(EntityManager entityManager) {
                CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                CriteriaQuery<UserMessageScore> query = criteriaBuilder
                        .createQuery(UserMessageScore.class);
                Root<UserMessageScore> entity = query.from(UserMessageScore.class);

                ParameterExpression<String> userGlobalIdParameter = criteriaBuilder
                        .parameter(String.class);
                ParameterExpression<String> messageGlobalIdParameter = criteriaBuilder
                        .parameter(String.class);

                query.where(criteriaBuilder.and(criteriaBuilder.equal(entity.get("userGlobalId"),
                        userGlobalIdParameter), criteriaBuilder.equal(
                        entity.get("messageGlobalId"), messageGlobalIdParameter)));

                TypedQuery<UserMessageScore> typedQuery = entityManager.createQuery(query);
                typedQuery.setParameter(userGlobalIdParameter, userGlobalId);
                typedQuery.setParameter(messageGlobalIdParameter, messageGlobalId);

                UserMessageScore messageRank;
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

    public MessageRelation getMessageRelation(Message message) {
        Validate.notNull(message, "message must not be null");
        EntityManager entityManager = getEntityManager();
        TypedQuery<MessageRelation> query = entityManager.createQuery(
                "SELECT mr FROM MessageRelation mr WHERE mr.globalId = '" + message.getGlobalId()
                        + "'", MessageRelation.class);
        try {
            MessageRelation result = query.getSingleResult();
            return result;
        } catch (NoResultException e) {
            return null;
        } finally {
            entityManager.close();
        }
    }

    public List<Message> getMessages(final MessageFilter messageFilter) {

        Transaction<List<Message>> transaction = new Transaction<List<Message>>() {

            @Override
            protected List<Message> doTransaction(EntityManager entityManager) {
                final List<Predicate> predicates = new ArrayList<Predicate>();

                CriteriaBuilder cb = entityManager.getCriteriaBuilder();
                CriteriaQuery<Message> query = cb.createQuery(Message.class);

                From<?, Message> messageEntity;

                if (messageFilter.getPattern() != null) {
                    // if we filter for a pattern, use it as "root from" and join the messages
                    Root<MessagePattern> messagePatternEntity = query.from(MessagePattern.class);
                    messageEntity = messagePatternEntity.join("message");
                    query.select(query.from(Message.class));

                    Predicate patternPred = cb.equal(messagePatternEntity.get("pattern"),
                            messageFilter.getPattern());

                    predicates.add(patternPred);
                } else {
                    messageEntity = query.from(Message.class);
                    query.select(query.from(Message.class));
                }

                // we want the messages
                query.select(messageEntity);

                Path<Date> publicationDate = messageEntity.get("publicationDate");
                Path<Long> messageLongId = messageEntity.get("id");

                // filter for publication date
                if (messageFilter.getMinPublicationDate() != null) {

                    Predicate datePred = cb.greaterThanOrEqualTo(publicationDate,
                            messageFilter.getMinPublicationDate());
                    predicates.add(datePred);
                }

                // filter for message group
                if (messageFilter.getMessageGroupGlobalId() != null
                        || messageFilter.getMessageGroupId() != null) {
                    Join<Message, MessageGroup> messageGroupEntity = messageEntity
                            .join("messageGroup");

                    if (messageFilter.getMessageGroupGlobalId() != null) {
                        Predicate mgPred = cb.equal(messageGroupEntity.get("globalId"),
                                messageFilter.getMessageGroupGlobalId());

                        predicates.add(mgPred);
                    }
                    if (messageFilter.getMessageGroupId() != null) {
                        Predicate mgPred = cb.equal(messageGroupEntity.get("id"),
                                messageFilter.getMessageGroupId());

                        predicates.add(mgPred);
                    }
                }

                // filter for source id
                if (messageFilter.getSourceGlobalId() != null) {
                    Predicate sourcePred = cb.equal(messageEntity.get("sourceGlobalId"),
                            messageFilter.getSourceGlobalId());

                    predicates.add(sourcePred);
                }

                query.where(predicates.toArray(new Predicate[predicates.size()]));

                // sort
                if (OrderDirection.ASC.equals(messageFilter.getMessageIdOrderDirection())) {
                    query.orderBy(cb.asc(messageLongId));
                } else if (OrderDirection.DESC.equals(messageFilter.getMessageIdOrderDirection())) {
                    query.orderBy(cb.desc(messageLongId));
                } else if (OrderDirection.ASC.equals(messageFilter
                        .getPublicationDateOrderDirection())) {
                    query.orderBy(cb.asc(publicationDate));
                } else if (OrderDirection.DESC.equals(messageFilter
                        .getPublicationDateOrderDirection())) {
                    query.orderBy(cb.desc(publicationDate));
                }

                TypedQuery<Message> typedQuery = entityManager.createQuery(query);

                if (messageFilter.getLastMessagesCount() > 0) {
                    typedQuery.setMaxResults(messageFilter.getLastMessagesCount());
                }

                try {
                    return typedQuery.getResultList();
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

                ParameterExpression<String> userParameter = criteriaBuilder.parameter(String.class);
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

    public TermFrequency getTermFrequency() {
        TermFrequency termFrequency = getEntityByGlobalId(TermFrequency.class,
                TermFrequency.SINGLE_GLOBAL_ID);
        if (termFrequency == null) {
            termFrequency = new TermFrequency();
            termFrequency = this.save(termFrequency);
        }
        termFrequency.init();
        return termFrequency;
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

    public void storeMessagePattern(String pattern, Message message) {
        Validate.notNull(pattern, "pattern must not be null");
        Validate.notNull(message, "message must not be null");
        EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        TypedQuery<Message> query = entityManager.createQuery(
                "SELECT m FROM Message m WHERE m.globalId = :globalId", Message.class);
        query.setParameter("globalId", message.getGlobalId());
        message = query.getSingleResult();
        MessagePattern messagePattern = new MessagePattern(message, pattern);
        entityManager.merge(messagePattern);
        transaction.commit();
        entityManager.close();
        // save(messagePattern);
    }

    /**
     * 
     * @param ranks
     *            store the ranks
     */
    public void storeMessageRanks(Collection<UserMessageScore> ranks) {
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

    public void storeMessageRelation(Message message, MessageRelation relatedMessages) {

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

    public void updateMessageRank(UserMessageScore rankToUpdate) {
        this.save(rankToUpdate);
    }

    public void updateTermFrequency(TermFrequency termFrequency) {
        termFrequency.prepareForStore();
        this.save(termFrequency);
    }

    public void updateTerms(Collection<Term> termsChanged) {
        this.saveAll(termsChanged);
    }

    public void visitAllMessageRanks(UserMessageScoreVisitor visitor, Date startDate, Date endDate) {
        throw new UnsupportedOperationException("not yet implemented");
    }

}
