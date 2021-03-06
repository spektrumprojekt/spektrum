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
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.i.informationextraction.InformationExtractionCommand;
import de.spektrumprojekt.i.informationextraction.InformationExtractionConfiguration;
import de.spektrumprojekt.i.ranker.chain.AdaptMessageRankByCMFOfSimilarUsersCommand;
import de.spektrumprojekt.i.ranker.chain.ComputeMessageRankCommand;
import de.spektrumprojekt.i.ranker.chain.DetermineInteractionLevelCommand;
import de.spektrumprojekt.i.ranker.chain.FeatureStatisticsCommand;
import de.spektrumprojekt.i.ranker.chain.InvokeLearnerCommand;
import de.spektrumprojekt.i.ranker.chain.StoreMessageCommand;
import de.spektrumprojekt.i.ranker.chain.StoreMessageRankCommand;
import de.spektrumprojekt.i.ranker.chain.TriggerUserModelAdaptationCommand;
import de.spektrumprojekt.i.ranker.chain.UpdateInteractionLevelOfMessageRanksCommand;
import de.spektrumprojekt.i.ranker.chain.UserFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.UserSimilarityIntegrationCommand;
import de.spektrumprojekt.i.ranker.chain.features.AuthorFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.features.ContentMatchFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.features.DiscussionMentionFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.features.DiscussionParticipationFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.features.DiscussionRootFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.features.FeatureAggregateCommand;
import de.spektrumprojekt.i.ranker.chain.features.MentionFeatureCommand;
import de.spektrumprojekt.i.term.TermSimilarityWeightComputerFactory;
import de.spektrumprojekt.i.term.frequency.TermFrequencyComputer;
import de.spektrumprojekt.i.term.similarity.TermVectorSimilarityComputer;
import de.spektrumprojekt.i.user.similarity.UserSimilarityComputer;
import de.spektrumprojekt.i.user.similarity.UserSimilarityComputer.UserSimilaritySimType;
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

    private final Persistence persistence;
    private final Communicator communicator;

    private final AdaptMessageRankByCMFOfSimilarUsersCommand adaptMessageRankByCMFOfSimilarUsersCommand;
    private final FeatureStatisticsCommand featureStatisticsCommand;

    private final CommandChain<MessageFeatureContext> rankerChain;

    private final CommandChain<MessageFeatureContext> rerankerChain;
    private final InformationExtractionCommand<MessageFeatureContext> informationExtractionChain;

    private final TermVectorSimilarityComputer termVectorSimilarityComputer;
    private final TermFrequencyComputer termFrequencyComputer;

    private final UserSimilarityComputer userSimilarityComputer;

    private final RankerConfiguration rankerConfiguration;

    private final ComputeMessageRankCommand computeMessageRankCommand;

    private final InvokeLearnerCommand invokeLearnerCommand;

    private final TriggerUserModelAdaptationCommand triggerUserModelAdaptationCommand;

    private final FeatureAggregateCommand featureAggregateCommand;

    private final UpdateInteractionLevelOfMessageRanksCommand updateInteractionLevelOfMessageRanksCommand;

    private final DetermineInteractionLevelCommand determineInteractionLevelCommand;

    private final StoreMessageRankCommand storeMessageRankCommand;

    private final ContentMatchFeatureCommand termMatchFeatureCommand;

    private final StoreMessageCommand storeMessageCommand;

    private final InformationExtractionConfiguration informationExtractionConfiguration;

    private final UserFeatureCommand userFeatureCommand;

    private final UserFeatureCommand reRankUserFeatureCommand;

    private final UserSimilarityIntegrationCommand userSimilarityIntegrationCommand;

    private int reRankCount;

    /**
     * 
     * @param persistence
     *            the persistence
     * @param memberRunner
     *            callback runner to get the groups for the user
     */
    public Ranker(Persistence persistence, Communicator communicator,
            MessageGroupMemberRunner<MessageFeatureContext> memberRunner,
            RankerConfiguration rankerConfiguration) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (communicator == null) {
            throw new IllegalArgumentException("communicator cannot be null.");
        }
        if (rankerConfiguration == null) {
            throw new IllegalArgumentException("rankerConfiguration cannot be null.");
        }
        if (rankerConfiguration.getInformationExtractionConfiguration() == null) {
            throw new IllegalArgumentException(
                    "rankerConfiguration.informationExtractionConfiguration cannot be null.");
        }
        this.rankerConfiguration = rankerConfiguration;
        this.rankerConfiguration.immutable();
        this.informationExtractionConfiguration = rankerConfiguration
                .getInformationExtractionConfiguration();

        this.persistence = persistence;
        this.communicator = communicator;
        this.termFrequencyComputer = new TermFrequencyComputer(this.persistence,
                this.rankerConfiguration
                        .hasFlag(RankerConfigurationFlag.USE_MESSAGE_GROUP_SPECIFIC_USER_MODEL));
        if (this.rankerConfiguration.getTermUniquenessLogfile() != null) {
            this.termFrequencyComputer.init(this.rankerConfiguration.getTermUniquenessLogfile());
        }
        this.informationExtractionConfiguration.setTermFrequencyComputer(termFrequencyComputer);

        termVectorSimilarityComputer = TermSimilarityWeightComputerFactory.getInstance()
                .createTermVectorSimilarityComputer(rankerConfiguration, termFrequencyComputer);
        // TODO register termfrequencycomputer in some timer

        rankerChain = new CommandChain<MessageFeatureContext>();
        rerankerChain = new CommandChain<MessageFeatureContext>();

        userFeatureCommand = new UserFeatureCommand(memberRunner);
        reRankUserFeatureCommand = new UserFeatureCommand(memberRunner);

        this.informationExtractionChain = InformationExtractionCommand.createDefaultGermanEnglish(
                persistence, this.rankerConfiguration.getInformationExtractionConfiguration());

        UserSimilaritySimType userSimilaritySimType = this.rankerConfiguration
                .getUserModelAdapterConfiguration().getUserSimilaritySimType();
        if (userSimilaritySimType == null) {
            userSimilaritySimType = UserSimilaritySimType.VOODOO;
        }
        userSimilarityComputer = new UserSimilarityComputer(this.persistence, userSimilaritySimType);

        storeMessageCommand = new StoreMessageCommand(persistence);
        userSimilarityIntegrationCommand = new UserSimilarityIntegrationCommand(
                userSimilarityComputer);
        DiscussionRootFeatureCommand discussionRootFeatureCommand = new DiscussionRootFeatureCommand();
        AuthorFeatureCommand authorFeatureCommand = new AuthorFeatureCommand();
        MentionFeatureCommand mentionFeatureCommand = new MentionFeatureCommand();
        DiscussionParticipationFeatureCommand discussionParticipationFeatureCommand = new DiscussionParticipationFeatureCommand();
        DiscussionMentionFeatureCommand discussionMentionFeatureCommand = new DiscussionMentionFeatureCommand();

        termMatchFeatureCommand = new ContentMatchFeatureCommand(persistence,
                termVectorSimilarityComputer, rankerConfiguration.getInterestTermTreshold(),
                rankerConfiguration);
        determineInteractionLevelCommand = new DetermineInteractionLevelCommand();
        computeMessageRankCommand = new ComputeMessageRankCommand(
                this.rankerConfiguration
                        .hasFlag(RankerConfigurationFlag.ONLY_USE_TERM_MATCHER_FEATURE_BUT_LEARN_FROM_FEATURES)
                        || this.rankerConfiguration
                                .hasFlag(RankerConfigurationFlag.ONLY_USE_TERM_MATCHER_FEATURE),
                this.rankerConfiguration.getNonParticipationFactor());
        invokeLearnerCommand = new InvokeLearnerCommand(this.persistence, this.communicator,
                this.rankerConfiguration);
        triggerUserModelAdaptationCommand = new TriggerUserModelAdaptationCommand(
                this.communicator,
                UserModel.DEFAULT_USER_MODEL_TYPE,
                this.rankerConfiguration.getUserModelAdapterConfiguration()
                        .getConfidenceThreshold(),
                this.rankerConfiguration
                        .getUserModelAdapterConfiguration().getRankThreshold());

        // TODO where to take the configuration values from ?
        adaptMessageRankByCMFOfSimilarUsersCommand = new AdaptMessageRankByCMFOfSimilarUsersCommand(
                persistence, this.rankerConfiguration.getMessageRankThreshold(),
                this.rankerConfiguration.getMinUserSimilarity(),
                this.rankerConfiguration.getMinContentMessageScore());
        updateInteractionLevelOfMessageRanksCommand = new UpdateInteractionLevelOfMessageRanksCommand(
                persistence);
        storeMessageRankCommand = new StoreMessageRankCommand(persistence);

        // add the commands to the chain
        if (!this.rankerConfiguration.hasFlag(RankerConfigurationFlag.NO_INFORMATION_EXTRACTION)) {
            rankerChain.addCommand(this.informationExtractionChain);
        }

        // store the message after the terms have been extracted
        rankerChain.addCommand(storeMessageCommand);

        if (this.rankerConfiguration
                .hasFlag(RankerConfigurationFlag.USE_CONTENT_MATCH_FEATURE_OF_SIMILAR_USERS)
                || this.rankerConfiguration
                        .hasFlag(RankerConfigurationFlag.USE_DIRECTED_USER_MODEL_ADAPTATION)) {
            rankerChain.addCommand(userSimilarityIntegrationCommand);
        }

        if (!this.rankerConfiguration
                .hasFlag(RankerConfigurationFlag.ONLY_USE_TERM_MATCHER_FEATURE)
                && !this.rankerConfiguration
                        .hasFlag(RankerConfigurationFlag.DO_NOT_USE_DISCUSSION_FEATURES)) {

            rankerChain.addCommand(discussionRootFeatureCommand);
        }
        if (!this.rankerConfiguration.hasFlag(RankerConfigurationFlag.NO_USER_SPECIFIC_COMMANDS)) {
            rankerChain.addCommand(userFeatureCommand);
        }

        if (!this.rankerConfiguration
                .hasFlag(RankerConfigurationFlag.ONLY_USE_TERM_MATCHER_FEATURE)) {

            userFeatureCommand.addCommand(authorFeatureCommand);
            userFeatureCommand.addCommand(mentionFeatureCommand);
        }

        if (!this.rankerConfiguration
                .hasFlag(RankerConfigurationFlag.ONLY_USE_TERM_MATCHER_FEATURE)
                && !this.rankerConfiguration
                        .hasFlag(RankerConfigurationFlag.DO_NOT_USE_DISCUSSION_FEATURES)) {

            userFeatureCommand.getUserSpecificCommandChain().addCommand(
                    discussionParticipationFeatureCommand);
            userFeatureCommand.addCommand(discussionMentionFeatureCommand);
        }

        if (!this.rankerConfiguration
                .hasFlag(RankerConfigurationFlag.DO_NOT_USE_CONTENT_MATCHER_FEATURE)) {
            userFeatureCommand.addCommand(termMatchFeatureCommand);
        }
        featureAggregateCommand = new FeatureAggregateCommand(this.persistence);

        userFeatureCommand.addCommand(determineInteractionLevelCommand);
        userFeatureCommand.addCommand(featureAggregateCommand);
        userFeatureCommand.addCommand(updateInteractionLevelOfMessageRanksCommand);
        userFeatureCommand.addCommand(computeMessageRankCommand);

        if (!this.rankerConfiguration.hasFlag(RankerConfigurationFlag.NO_LEARNING_ONLY_RANKING)) {
            userFeatureCommand.addCommand(invokeLearnerCommand);
        }

        if (!this.rankerConfiguration
                .hasFlag(RankerConfigurationFlag.DO_NOT_USE_CONTENT_MATCHER_FEATURE)
                && this.rankerConfiguration
                        .hasFlag(RankerConfigurationFlag.USE_DIRECTED_USER_MODEL_ADAPTATION)
                && !this.rankerConfiguration
                        .hasFlag(RankerConfigurationFlag.NO_LEARNING_ONLY_RANKING)) {

            userFeatureCommand.addCommand(triggerUserModelAdaptationCommand);
        }

        featureStatisticsCommand = new FeatureStatisticsCommand();
        userFeatureCommand.addCommand(featureStatisticsCommand);

        if (this.rankerConfiguration
                .hasFlag(RankerConfigurationFlag.USE_CONTENT_MATCH_FEATURE_OF_SIMILAR_USERS)) {
            rankerChain.addCommand(adaptMessageRankByCMFOfSimilarUsersCommand);
        }

        rankerChain.addCommand(storeMessageRankCommand);

        initReRankChain();

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

    public void close() {
        this.termFrequencyComputer.stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deliverMessage(RankingCommunicationMessage message) throws Exception {

        rank(message.getMessage(), message.getMessageRelation(),
                message.getUserGlobalIdsToRankFor(), message.isNoRankingOnlyLearning());

    }

    public AdaptMessageRankByCMFOfSimilarUsersCommand getAdaptMessageRankByCMFOfSimilarUsersCommand() {
        return adaptMessageRankByCMFOfSimilarUsersCommand;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append(" rankerConfiguration: " + this.rankerConfiguration.getConfigurationDescription());
        sb.append(" termVectorSimilarityComputer: "
                + this.termVectorSimilarityComputer.getConfigurationDescription());
        sb.append(" termFrequencyComputer: "
                + this.termFrequencyComputer.getConfigurationDescription());
        sb.append(" rankerCommandChain: " + this.rankerChain.getConfigurationDescription());
        sb.append(" rerankerCommandChain: " + this.rerankerChain.getConfigurationDescription());
        sb.append(" userSimilarityComputer: "
                + this.userSimilarityComputer.getConfigurationDescription());

        return sb.toString();
    }

    public FeatureStatisticsCommand getFeatureStatisticsCommand() {
        return featureStatisticsCommand;
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

    public RankerConfiguration getRankerConfiguration() {
        return rankerConfiguration;
    }

    public int getReRankCount() {
        return reRankCount;
    }

    public StoreMessageCommand getStoreMessageCommand() {
        return storeMessageCommand;
    }

    public StoreMessageRankCommand getStoreMessageRankCommand() {
        return storeMessageRankCommand;
    }

    public TermFrequencyComputer getTermFrequencyComputer() {
        return termFrequencyComputer;
    }

    public TermVectorSimilarityComputer getTermVectorSimilarityComputer() {
        return termVectorSimilarityComputer;
    }

    public UserSimilarityComputer getUserSimilarityComputer() {
        return userSimilarityComputer;
    }

    private void initReRankChain() {
        // setup the reranker chain. the reranker only uses the term match feature and assumes the
        // message has been ranked before
        rerankerChain.addCommand(reRankUserFeatureCommand);
        reRankUserFeatureCommand.addCommand(determineInteractionLevelCommand);
        reRankUserFeatureCommand.addCommand(termMatchFeatureCommand);
        reRankUserFeatureCommand.addCommand(computeMessageRankCommand);
        rerankerChain.addCommand(getStoreMessageRankCommand());
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

        MessageFeatureContext context = new MessageFeatureContext(this.persistence, message,
                messageRelation);
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

        reRankCount++;

        MessageFeatureContext context = new MessageFeatureContext(this.persistence, message, null);
        context.setNoRankingOnlyLearning(false);

        if (userGlobalId != null) {
            context.setUserGlobalIdsToProcess(Arrays.asList(userGlobalId));
        }

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
