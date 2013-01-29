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

package de.spektrumprojekt.i.ranker;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.callbacks.MessageGroupMemberRunner;
import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.commons.chain.CommandChain;
import de.spektrumprojekt.communication.CommunicationMessage;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.communication.MessageHandler;
import de.spektrumprojekt.communication.transfer.RankingCommunicationMessage;
import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.i.informationextraction.InformationExtractionCommand;
import de.spektrumprojekt.i.informationextraction.frequency.TermFrequencyComputer;
import de.spektrumprojekt.i.ranker.chain.ComputeMessageRankCommand;
import de.spektrumprojekt.i.ranker.chain.InvokeLearnerCommand;
import de.spektrumprojekt.i.ranker.chain.StoreMessageCommand;
import de.spektrumprojekt.i.ranker.chain.StoreMessageRankCommand;
import de.spektrumprojekt.i.ranker.chain.TriggerUserModelAdaptationCommand;
import de.spektrumprojekt.i.ranker.chain.UserFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.features.AuthorFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.features.ContentMatchFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.features.ContentMatchFeatureCommand.TermWeightAggregation;
import de.spektrumprojekt.i.ranker.chain.features.ContentMatchFeatureCommand.TermWeightStrategy;
import de.spektrumprojekt.i.ranker.chain.features.DiscussionMentionFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.features.DiscussionParticipationFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.features.DiscussionRootFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.features.MentionFeatureCommand;
import de.spektrumprojekt.persistence.Persistence;

/**
 * The ranker takes an incoming message (hence the {@link MessageHandler} implementation and
 * computes a message rank per user
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class Ranker implements MessageHandler<RankingCommunicationMessage>,
        ConfigurationDescriptable {

    private final static Logger LOGGER = LoggerFactory.getLogger(Ranker.class);

    private final Collection<RankerConfigurationFlag> flags = new HashSet<RankerConfigurationFlag>();

    private final Persistence persistence;
    private final Communicator communicator;
    private final TermFrequencyComputer termFrequencyComputer;

    private CommandChain<MessageFeatureContext> rankerChain;
    private CommandChain<MessageFeatureContext> rerankerChain;

    private InformationExtractionCommand<MessageFeatureContext> informationExtractionChain;

    /**
     * 
     * @param persistence
     *            the persistence
     * @param memberRunner
     *            callback runner to get the groups for the user
     */
    public Ranker(Persistence persistence, Communicator communicator,
            MessageGroupMemberRunner<MessageFeatureContext> memberRunner,
            RankerConfigurationFlag... flags) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (communicator == null) {
            throw new IllegalArgumentException("communicator cannot be null.");
        }
        if (flags != null) {
            this.flags.addAll(Arrays.asList(flags));
        }
        this.persistence = persistence;
        this.communicator = communicator;
        this.termFrequencyComputer = new TermFrequencyComputer(this.persistence);
        // TODO register termfrequencycomputer in some timer

        rankerChain = new CommandChain<MessageFeatureContext>();
        rerankerChain = new CommandChain<MessageFeatureContext>();

        TermWeightStrategy termWeightStrategy = TermWeightStrategy.NONE;
        if (this.flags.contains(RankerConfigurationFlag.USE_INVERSE_TERM_FREQUENCY)) {
            termWeightStrategy = TermWeightStrategy.INVERSE_TERM_FREQUENCY;
        }

        // create the commands
        UserFeatureCommand userFeatureCommand = new UserFeatureCommand(memberRunner);
        UserFeatureCommand reRankUserFeatureCommand = new UserFeatureCommand(memberRunner);

        this.informationExtractionChain = InformationExtractionCommand
                .createDefaultGermanEnglish(
                        this.persistence,
                        this.termFrequencyComputer,
                        false,
                        this.flags
                                .contains(RankerConfigurationFlag.USER_MESSAGE_GROUP_SPECIFIC_USER_MODEL));

        StoreMessageCommand storeMessageCommand = new StoreMessageCommand(persistence);
        DiscussionRootFeatureCommand discussionRootFeatureCommand = new DiscussionRootFeatureCommand();
        AuthorFeatureCommand authorFeatureCommand = new AuthorFeatureCommand();
        MentionFeatureCommand mentionFeatureCommand = new MentionFeatureCommand();
        DiscussionParticipationFeatureCommand discussionParticipationFeatureCommand = new DiscussionParticipationFeatureCommand();
        DiscussionMentionFeatureCommand discussionMentionFeatureCommand = new DiscussionMentionFeatureCommand();
        ContentMatchFeatureCommand termMatchFeatureCommand = new ContentMatchFeatureCommand(
                persistence,
                TermWeightAggregation.AVG, termWeightStrategy, 0.75f);
        ComputeMessageRankCommand computeMessageRankCommand = new ComputeMessageRankCommand(
                this.flags
                        .contains(RankerConfigurationFlag.ONLY_USE_TERM_MATCHER_FEATURE_BUT_LEARN_FROM_FEATURES)
                        || this.flags
                                .contains(RankerConfigurationFlag.ONLY_USE_TERM_MATCHER_FEATURE),
                this.flags
                        .contains(RankerConfigurationFlag.USE_HALF_SCORE_ON_NON_PARTICIPATING_ANSWERS));
        InvokeLearnerCommand invokeLearnerCommand = new InvokeLearnerCommand(
                this.communicator, this.flags.contains(RankerConfigurationFlag.LEARN_NEGATIVE));
        TriggerUserModelAdaptationCommand triggerUserModelAdaptationCommand = new TriggerUserModelAdaptationCommand(
                this.communicator);
        StoreMessageRankCommand storeMessageRankCommand = new StoreMessageRankCommand(persistence);

        // add the commands to the chain
        rankerChain.addCommand(this.informationExtractionChain);

        // store the message after the terms have been extracted
        rankerChain.addCommand(storeMessageCommand);

        if (!this.flags.contains(RankerConfigurationFlag.ONLY_USE_TERM_MATCHER_FEATURE)
                && !this.flags.contains(RankerConfigurationFlag.DO_NOT_USE_DISCUSSION_FEATURES)) {

            rankerChain.addCommand(discussionRootFeatureCommand);
        }
        rankerChain.addCommand(userFeatureCommand);

        if (!this.flags.contains(RankerConfigurationFlag.ONLY_USE_TERM_MATCHER_FEATURE)) {

            userFeatureCommand.addCommand(authorFeatureCommand);
            userFeatureCommand.addCommand(mentionFeatureCommand);
        }

        if (!this.flags.contains(RankerConfigurationFlag.ONLY_USE_TERM_MATCHER_FEATURE) &&
                !this.flags.contains(RankerConfigurationFlag.DO_NOT_USE_DISCUSSION_FEATURES)) {

            userFeatureCommand.getUserSpecificCommandChain().addCommand(
                    discussionParticipationFeatureCommand);
            userFeatureCommand.getUserSpecificCommandChain().addCommand(
                    discussionMentionFeatureCommand);
        }
        if (!this.flags.contains(RankerConfigurationFlag.DO_NOT_USE_TERM_MATCHER_FEATURE)) {
            userFeatureCommand.addCommand(termMatchFeatureCommand);
        }
        userFeatureCommand.addCommand(computeMessageRankCommand);
        userFeatureCommand.addCommand(invokeLearnerCommand);

        if (!this.flags.contains(RankerConfigurationFlag.DO_NOT_USE_TERM_MATCHER_FEATURE)
                && this.flags.contains(RankerConfigurationFlag.USE_DIRECTED_USER_MODEL_ADAPTATION)) {

            userFeatureCommand.addCommand(triggerUserModelAdaptationCommand);
        }

        rankerChain.addCommand(storeMessageRankCommand);

        // setup the reranker chain. the reranker only uses the term match feature and assumes the
        // message has been ranked before
        rerankerChain.addCommand(reRankUserFeatureCommand);
        reRankUserFeatureCommand.addCommand(termMatchFeatureCommand);
        reRankUserFeatureCommand.addCommand(computeMessageRankCommand);
        rerankerChain.addCommand(storeMessageRankCommand);

        // TODO store the features ?! (or only necessary for evaluation?)

    }

    /**
     * Adds the command to the end of the ranker chain
     * 
     * @param command
     *            the command
     */
    public void addCommand(Command<MessageFeatureContext> command) {
        this.rankerChain.addCommand(command);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deliverMessage(RankingCommunicationMessage message)
            throws Exception {

        rank(message.getMessage(), message.getMessageRelation(),
                message.getUserGlobalIdsToRankFor(), message.isNoRankingOnlyLearning());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append(" termFrequencyComputer: "
                + this.termFrequencyComputer.getConfigurationDescription());
        sb.append(" flags: {" + StringUtils.join(this.flags, ", ") + "}");
        sb.append(" rankerCommandChain: " + this.rankerChain.getConfigurationDescription());
        sb.append(" rerankerCommandChain: " + this.rerankerChain.getConfigurationDescription());

        return sb.toString();
    }

    /**
     * the flags used to setup the ranker
     * 
     * @return returns an unmodifiable collection of the flags
     */
    public Collection<RankerConfigurationFlag> getFlags() {
        return Collections.unmodifiableCollection(flags);
    }

    public InformationExtractionCommand<MessageFeatureContext> getInformationExtractionChain() {
        return informationExtractionChain;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<RankingCommunicationMessage> getMessageClass() {
        return RankingCommunicationMessage.class;
    }

    public TermFrequencyComputer getTermFrequencyComputer() {
        return termFrequencyComputer;
    }

    /**
     * Rank the message (not using the communicator)
     * 
     * @param message
     *            the message
     * @param messageRelation
     *            the message relation
     * @param noRankingOnlyLearning
     * @param userGlobalIdsToRankFor
     *            the global ids of users to rank for ( however they must be included in the member
     *            runner provided in the constructor )
     * @return the compute context including ranks
     */
    public MessageFeatureContext rank(Message message, MessageRelation messageRelation,
            String[] userGlobalIdsToRankFor, boolean noRankingOnlyLearning) {
        // TODO actually this message is needed in two case: 1. for ranking it, 2. for learning from
        // it. Now should the ranker send a new message (could include the message features) or let
        // the learner do it again, in a much simpler way ?

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        MessageFeatureContext context = new MessageFeatureContext(this.persistence,
                message, messageRelation);
        context.setNoRankingOnlyLearning(noRankingOnlyLearning);

        if (userGlobalIdsToRankFor != null) {
            Collection<String> users = Arrays.asList(userGlobalIdsToRankFor);
            context.setUserGlobalIdsToProcess(users);
        }

        rankerChain.process(context);

        stopWatch.stop();

        LOGGER.debug("Ranker processed message {} in {} ms", message.getGlobalId(),
                stopWatch.getTime());
        return context;
    }

    /**
     * The reranking only uses the term match feature and assumes that the message has been ranked
     * before for the user, and hence no information extraction or message storing will be executed.
     * 
     * @param message
     * @param userGlobalId
     * @return
     */
    public MessageFeatureContext rerank(Message message, String userGlobalId) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        MessageFeatureContext context = new MessageFeatureContext(this.persistence,
                message, null);
        context.setNoRankingOnlyLearning(false);

        context.setUserGlobalIdsToProcess(Arrays.asList(userGlobalId));

        rerankerChain.process(context);

        stopWatch.stop();

        LOGGER.debug("Ranker processed message {} in {} ms", message.getGlobalId(),
                stopWatch.getTime());
        return context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(CommunicationMessage message) {
        return message instanceof RankingCommunicationMessage;
    }

}
