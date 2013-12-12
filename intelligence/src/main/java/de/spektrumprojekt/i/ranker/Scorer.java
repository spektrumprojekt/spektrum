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
import de.spektrumprojekt.communication.transfer.ScorerCommunicationMessage;
import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.i.informationextraction.InformationExtractionCommand;
import de.spektrumprojekt.i.informationextraction.InformationExtractionConfiguration;
import de.spektrumprojekt.i.learner.adaptation.UserModelAdapterConfiguration.UserModelBasedSimilarityConfiguration;
import de.spektrumprojekt.i.ranker.chain.AdaptMessageScoreByCMFOfSimilarUsersCommand;
import de.spektrumprojekt.i.ranker.chain.ComputeMessageScoreCommand;
import de.spektrumprojekt.i.ranker.chain.DetermineInteractionLevelCommand;
import de.spektrumprojekt.i.ranker.chain.FeatureStatisticsCommand;
import de.spektrumprojekt.i.ranker.chain.InvokeLearnerCommand;
import de.spektrumprojekt.i.ranker.chain.StoreMessageCommand;
import de.spektrumprojekt.i.ranker.chain.StoreMessageScoreCommand;
import de.spektrumprojekt.i.ranker.chain.TriggerUserModelAdaptationCommand;
import de.spektrumprojekt.i.ranker.chain.UpdateInteractionLevelOfMessageScoresCommand;
import de.spektrumprojekt.i.ranker.chain.UserFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.UserSimilarityIntegrationCommand;
import de.spektrumprojekt.i.ranker.chain.features.AuthorFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.features.ContentMatchFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.features.DiscussionMentionFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.features.DiscussionParticipationFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.features.FeatureAggregateCommand;
import de.spektrumprojekt.i.ranker.chain.features.MentionFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.features.MessageSpecificFeaturesCommand;
import de.spektrumprojekt.i.similarity.user.InteractionBasedUserSimilarityComputer;
import de.spektrumprojekt.i.similarity.user.IterativeUserSimilarityComputer;
import de.spektrumprojekt.i.similarity.user.UserModelBasedUserSimilarityComputer;
import de.spektrumprojekt.i.similarity.user.UserSimilarityComputer;
import de.spektrumprojekt.i.similarity.user.UserSimilaritySimType;
import de.spektrumprojekt.i.term.TermSimilarityWeightComputerFactory;
import de.spektrumprojekt.i.term.frequency.TermFrequencyComputer;
import de.spektrumprojekt.i.term.similarity.TermVectorSimilarityComputer;
import de.spektrumprojekt.persistence.Persistence;

/**
 * The scorer takes an incoming message (hence the {@link MessageHandler} implementation and
 * computes a message rank per user
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class Scorer implements MessageHandler<ScorerCommunicationMessage>,
        ConfigurationDescriptable {

    private final static Logger LOGGER = LoggerFactory.getLogger(Scorer.class);

    private final Persistence persistence;
    private final Communicator communicator;

    private final AdaptMessageScoreByCMFOfSimilarUsersCommand adaptMessageScoreByCMFOfSimilarUsersCommand;
    private final FeatureStatisticsCommand featureStatisticsCommand;

    private final CommandChain<MessageFeatureContext> scorerChain;

    private final CommandChain<MessageFeatureContext> reScoringChain;
    private final InformationExtractionCommand<MessageFeatureContext> informationExtractionChain;

    private final TermVectorSimilarityComputer termVectorSimilarityComputer;
    private final TermFrequencyComputer termFrequencyComputer;

    private final UserSimilarityComputer userSimilarityComputer;

    private final ScorerConfiguration scorerConfiguration;

    private final ComputeMessageScoreCommand computeMessageScoreCommand;

    private final InvokeLearnerCommand invokeLearnerCommand;

    private final TriggerUserModelAdaptationCommand triggerUserModelAdaptationCommand;

    private final FeatureAggregateCommand featureAggregateCommand;

    private final UpdateInteractionLevelOfMessageScoresCommand updateInteractionLevelOfMessageScoresCommand;

    private final DetermineInteractionLevelCommand determineInteractionLevelCommand;

    private final StoreMessageScoreCommand storeMessageScoreCommand;

    private final ContentMatchFeatureCommand termMatchFeatureCommand;

    private final StoreMessageCommand storeMessageCommand;

    private final InformationExtractionConfiguration informationExtractionConfiguration;

    private final UserFeatureCommand userFeatureCommand;

    private final UserFeatureCommand reScoringUserFeatureCommand;

    private final UserSimilarityIntegrationCommand userSimilarityIntegrationCommand;

    private int reScoringCount;

    private final DiscussionMentionFeatureCommand discussionMentionFeatureCommand;

    private final DiscussionParticipationFeatureCommand discussionParticipationFeatureCommand;

    private final MentionFeatureCommand mentionFeatureCommand;

    private final AuthorFeatureCommand authorFeatureCommand;

    private final MessageSpecificFeaturesCommand discussionRootFeatureCommand;

    /**
     * 
     * @param persistence
     *            the persistence
     * @param memberRunner
     *            callback runner to get the groups for the user
     */
    public Scorer(
            Persistence persistence,
            Communicator communicator,
            MessageGroupMemberRunner<MessageFeatureContext> memberRunner,
            ScorerConfiguration scorerConfiguration) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (communicator == null) {
            throw new IllegalArgumentException("communicator cannot be null.");
        }
        if (scorerConfiguration == null) {
            throw new IllegalArgumentException("scroerConfiguration cannot be null.");
        }
        if (scorerConfiguration.getInformationExtractionConfiguration() == null) {
            throw new IllegalArgumentException(
                    "scorerConfiguration.informationExtractionConfiguration cannot be null.");
        }
        this.scorerConfiguration = scorerConfiguration;
        this.scorerConfiguration.immutable();
        this.informationExtractionConfiguration = scorerConfiguration
                .getInformationExtractionConfiguration();

        this.persistence = persistence;
        this.communicator = communicator;
        this.termFrequencyComputer = new TermFrequencyComputer(this.persistence,
                this.scorerConfiguration
                        .hasFlag(ScorerConfigurationFlag.USE_MESSAGE_GROUP_SPECIFIC_USER_MODEL));
        if (this.scorerConfiguration.getTermUniquenessLogfile() != null) {
            this.termFrequencyComputer.init(this.scorerConfiguration.getTermUniquenessLogfile());
        }
        this.informationExtractionConfiguration.setTermFrequencyComputer(termFrequencyComputer);

        termVectorSimilarityComputer = TermSimilarityWeightComputerFactory.getInstance()
                .createTermVectorSimilarityComputer(scorerConfiguration, termFrequencyComputer);
        // TODO register termfrequencycomputer in some timer

        scorerChain = new CommandChain<MessageFeatureContext>();
        reScoringChain = new CommandChain<MessageFeatureContext>();

        userFeatureCommand = new UserFeatureCommand(memberRunner);
        reScoringUserFeatureCommand = new UserFeatureCommand(memberRunner);

        this.informationExtractionChain = InformationExtractionCommand.createDefaultGermanEnglish(
                persistence, this.scorerConfiguration.getInformationExtractionConfiguration());

        UserSimilaritySimType userSimilaritySimType = this.scorerConfiguration
                .getUserModelAdapterConfiguration().getUserSimilaritySimType();
        if (userSimilaritySimType == null) {
            userSimilaritySimType = UserSimilaritySimType.VOODOO;
        }
        switch (userSimilaritySimType) {
        case USER_MODEL:
            UserModelBasedSimilarityConfiguration userModelBasedSimilarityConfiguration = this.scorerConfiguration
                    .getUserModelAdapterConfiguration().getUserModelBasedSimilarityConfiguration();
            if (userModelBasedSimilarityConfiguration == null) {
                throw new IllegalArgumentException(
                        "userModelBasedSimilarityConfiguration must be set of userSimilaritySimType=USER_MODEL");
            }
            userSimilarityComputer = new UserModelBasedUserSimilarityComputer(
                    this.persistence,
                    userModelBasedSimilarityConfiguration.getSetSimilarity(),
                    termVectorSimilarityComputer,
                    userModelBasedSimilarityConfiguration.getPrecomputedUserSimilaritesFilename(),
                    false);
            break;
        default:
            userSimilarityComputer = new InteractionBasedUserSimilarityComputer(this.persistence,
                    userSimilaritySimType);
        }

        storeMessageCommand = new StoreMessageCommand(persistence);
        if (userSimilarityComputer instanceof IterativeUserSimilarityComputer) {
            userSimilarityIntegrationCommand = new UserSimilarityIntegrationCommand(
                    (IterativeUserSimilarityComputer) userSimilarityComputer);
        } else {
            userSimilarityIntegrationCommand = null;
        }
        discussionRootFeatureCommand = new MessageSpecificFeaturesCommand();
        authorFeatureCommand = new AuthorFeatureCommand();
        mentionFeatureCommand = new MentionFeatureCommand();
        discussionParticipationFeatureCommand = new DiscussionParticipationFeatureCommand();
        discussionMentionFeatureCommand = new DiscussionMentionFeatureCommand();

        termMatchFeatureCommand = new ContentMatchFeatureCommand(persistence,
                termVectorSimilarityComputer, scorerConfiguration.getInterestTermTreshold(),
                scorerConfiguration);
        determineInteractionLevelCommand = new DetermineInteractionLevelCommand();

        computeMessageScoreCommand = new ComputeMessageScoreCommand(
                this.scorerConfiguration.getFeatureWeights(),
                this.scorerConfiguration.getNonParticipationFactor());
        invokeLearnerCommand = new InvokeLearnerCommand(this.persistence, this.communicator,
                this.scorerConfiguration);
        triggerUserModelAdaptationCommand = new TriggerUserModelAdaptationCommand(
                this.communicator,
                UserModel.DEFAULT_USER_MODEL_TYPE,
                this.scorerConfiguration.getUserModelAdapterConfiguration()
                        .getConfidenceThreshold(),
                this.scorerConfiguration
                        .getUserModelAdapterConfiguration().getScoreThreshold());

        // TODO where to take the configuration values from ?
        adaptMessageScoreByCMFOfSimilarUsersCommand = new AdaptMessageScoreByCMFOfSimilarUsersCommand(
                persistence, this.scorerConfiguration.getMessageScoreThreshold(),
                this.scorerConfiguration.getMinUserSimilarity(),
                this.scorerConfiguration.getMinContentMessageScore());
        updateInteractionLevelOfMessageScoresCommand = new UpdateInteractionLevelOfMessageScoresCommand(
                persistence);

        storeMessageScoreCommand = new StoreMessageScoreCommand(persistence);

        featureAggregateCommand = new FeatureAggregateCommand(this.persistence);

        // add the commands to the chain
        if (!this.scorerConfiguration.hasFlag(ScorerConfigurationFlag.NO_INFORMATION_EXTRACTION)) {
            scorerChain.addCommand(this.informationExtractionChain);
        }

        // store the message after the terms have been extracted
        scorerChain.addCommand(storeMessageCommand);

        if (userSimilarityIntegrationCommand != null && (this.scorerConfiguration
                .hasFlag(ScorerConfigurationFlag.USE_CONTENT_MATCH_FEATURE_OF_SIMILAR_USERS)
                || this.scorerConfiguration
                .hasFlag(ScorerConfigurationFlag.USE_DIRECTED_USER_MODEL_ADAPTATION))) {
            scorerChain.addCommand(userSimilarityIntegrationCommand);
        }

        if (!this.scorerConfiguration
                .hasFlag(ScorerConfigurationFlag.DO_NOT_USE_DISCUSSION_FEATURES)) {

            scorerChain.addCommand(discussionRootFeatureCommand);
        }
        if (!this.scorerConfiguration.hasFlag(ScorerConfigurationFlag.NO_USER_SPECIFIC_COMMANDS)) {
            scorerChain.addCommand(userFeatureCommand);
        }

        userFeatureCommand.addCommand(authorFeatureCommand);
        userFeatureCommand.addCommand(mentionFeatureCommand);

        if (!this.scorerConfiguration
                .hasFlag(ScorerConfigurationFlag.DO_NOT_USE_DISCUSSION_FEATURES)) {

            userFeatureCommand.getUserSpecificCommandChain().addCommand(
                    discussionParticipationFeatureCommand);
            userFeatureCommand.addCommand(discussionMentionFeatureCommand);
        }

        if (!this.scorerConfiguration
                .hasFlag(ScorerConfigurationFlag.DO_NOT_USE_CONTENT_MATCHER_FEATURE)) {
            userFeatureCommand.addCommand(termMatchFeatureCommand);
        }

        userFeatureCommand.addCommand(determineInteractionLevelCommand);
        userFeatureCommand.addCommand(featureAggregateCommand);
        userFeatureCommand.addCommand(updateInteractionLevelOfMessageScoresCommand);
        userFeatureCommand.addCommand(computeMessageScoreCommand);

        if (!this.scorerConfiguration.hasFlag(ScorerConfigurationFlag.NO_LEARNING_ONLY_SCORING)) {
            userFeatureCommand.addCommand(invokeLearnerCommand);
        }

        if (!this.scorerConfiguration
                .hasFlag(ScorerConfigurationFlag.DO_NOT_USE_CONTENT_MATCHER_FEATURE)
                && this.scorerConfiguration
                        .hasFlag(ScorerConfigurationFlag.USE_DIRECTED_USER_MODEL_ADAPTATION)
                && !this.scorerConfiguration
                        .hasFlag(ScorerConfigurationFlag.NO_LEARNING_ONLY_SCORING)) {

            userFeatureCommand.addCommand(triggerUserModelAdaptationCommand);
        }

        featureStatisticsCommand = new FeatureStatisticsCommand();
        userFeatureCommand.addCommand(featureStatisticsCommand);

        if (this.scorerConfiguration
                .hasFlag(ScorerConfigurationFlag.USE_CONTENT_MATCH_FEATURE_OF_SIMILAR_USERS)) {
            scorerChain.addCommand(adaptMessageScoreByCMFOfSimilarUsersCommand);
        }

        scorerChain.addCommand(storeMessageScoreCommand);

        initReScoringChain();

    }

    /**
     * Adds the command to the end of the scorer chain
     * 
     * @param command
     *            the command
     */
    public void addCommand(Command<MessageFeatureContext> command) {
        this.scorerChain.addCommand(command);
    }

    public void close() {
        this.termFrequencyComputer.stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deliverMessage(ScorerCommunicationMessage message) throws Exception {

        score(message.getMessage(), message.getMessageRelation(),
                message.getUserGlobalIdsToScoreFor(), message.isNoScoringOnlyLearning());

    }

    public AdaptMessageScoreByCMFOfSimilarUsersCommand getAdaptMessageScoreByCMFOfSimilarUsersCommand() {
        return adaptMessageScoreByCMFOfSimilarUsersCommand;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append(" scroerConfiguration: " + this.scorerConfiguration.getConfigurationDescription());
        sb.append(" termVectorSimilarityComputer: "
                + this.termVectorSimilarityComputer.getConfigurationDescription());
        sb.append(" termFrequencyComputer: "
                + this.termFrequencyComputer.getConfigurationDescription());
        sb.append(" scorerCommandChain: " + this.scorerChain.getConfigurationDescription());
        sb.append(" reScoringCommandChain: " + this.reScoringChain.getConfigurationDescription());
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

    public int getInvokerLearnerCount() {
        return this.invokeLearnerCommand.getLearnCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<ScorerCommunicationMessage> getMessageClass() {
        return ScorerCommunicationMessage.class;
    }

    public int getReScoringCount() {
        return reScoringCount;
    }

    public ScorerConfiguration getScorerConfiguration() {
        return scorerConfiguration;
    }

    public StoreMessageCommand getStoreMessageCommand() {
        return storeMessageCommand;
    }

    public StoreMessageScoreCommand getStoreMessageScoreCommand() {
        return storeMessageScoreCommand;
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

    /**
     * setup the re-scoring chain
     */
    private void initReScoringChain() {

        if (!this.scorerConfiguration
                .hasFlag(ScorerConfigurationFlag.DO_NOT_USE_DISCUSSION_FEATURES)) {

            reScoringChain.addCommand(discussionRootFeatureCommand);
        }

        reScoringChain.addCommand(reScoringUserFeatureCommand);

        reScoringUserFeatureCommand.addCommand(authorFeatureCommand);
        reScoringUserFeatureCommand.addCommand(mentionFeatureCommand);

        if (!this.scorerConfiguration
                .hasFlag(ScorerConfigurationFlag.DO_NOT_USE_DISCUSSION_FEATURES)) {

            reScoringUserFeatureCommand.getUserSpecificCommandChain().addCommand(
                    discussionParticipationFeatureCommand);
            reScoringUserFeatureCommand.addCommand(discussionMentionFeatureCommand);
        }

        if (!this.scorerConfiguration
                .hasFlag(ScorerConfigurationFlag.DO_NOT_USE_CONTENT_MATCHER_FEATURE)) {
            reScoringUserFeatureCommand.addCommand(termMatchFeatureCommand);
        }

        reScoringUserFeatureCommand.addCommand(determineInteractionLevelCommand);
        reScoringUserFeatureCommand.addCommand(featureAggregateCommand);
        reScoringUserFeatureCommand.addCommand(updateInteractionLevelOfMessageScoresCommand);

        reScoringUserFeatureCommand.addCommand(computeMessageScoreCommand);

        reScoringChain.addCommand(getStoreMessageScoreCommand());
    }

    /**
     * The re-scoreing only uses the term match feature and assumes that the message has been scored
     * before for the user, and hence no information extraction or message storing will be executed.
     * 
     * @param message
     * @param userGlobalId
     * @return
     */
    public MessageFeatureContext rescore(Message message, String userGlobalId) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        reScoringCount++;

        MessageFeatureContext context = new MessageFeatureContext(this.persistence, message, null);
        context.setNoScoreingOnlyLearning(false);

        if (userGlobalId != null) {
            context.setUserGlobalIdsToProcess(Arrays.asList(userGlobalId));
        }

        reScoringChain.process(context);

        stopWatch.stop();

        LOGGER.debug("Scorer processed message {} in {} ms", message.getGlobalId(),
                stopWatch.getTime());
        return context;
    }

    /**
     * Score the message (not using the communicator)
     * 
     * @param message
     *            the message
     * @param messageRelation
     *            the message relation
     * @param noScoringOnlyLearning
     * @param userGlobalIdsToScoreFor
     *            the global ids of users to score for ( however they must be included in the member
     *            runner provided in the constructor )
     * @return the compute context including scores
     */
    public MessageFeatureContext score(Message message, MessageRelation messageRelation,
            String[] userGlobalIdsToScoreFor, boolean noScoringOnlyLearning) {
        // TODO actually this message is needed in two case: 1. for scoreing it, 2. for learning
        // from
        // it. Now should the scorer send a new message (could include the message features) or let
        // the learner do it again, in a much simpler way ?

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        MessageFeatureContext context = new MessageFeatureContext(this.persistence, message,
                messageRelation);
        context.setNoScoreingOnlyLearning(noScoringOnlyLearning);

        if (userGlobalIdsToScoreFor != null) {
            Collection<String> users = Arrays.asList(userGlobalIdsToScoreFor);
            context.setUserGlobalIdsToProcess(users);
        }

        scorerChain.process(context);

        stopWatch.stop();

        LOGGER.debug("Scorer processed message {} in {} ms", message.getGlobalId(),
                stopWatch.getTime());
        return context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(CommunicationMessage message) {
        return message instanceof ScorerCommunicationMessage;
    }

}
