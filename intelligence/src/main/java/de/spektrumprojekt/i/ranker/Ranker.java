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

import java.util.ArrayList;
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
import de.spektrumprojekt.i.ranker.chain.ComputeMessageRankCommand;
import de.spektrumprojekt.i.ranker.chain.InvokeLearnerCommand;
import de.spektrumprojekt.i.ranker.chain.StoreMessageCommand;
import de.spektrumprojekt.i.ranker.chain.StoreMessageRankCommand;
import de.spektrumprojekt.i.ranker.chain.TriggerUserModelAdaptationCommand;
import de.spektrumprojekt.i.ranker.chain.UserFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.features.AuthorFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.features.DiscussionMentionFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.features.DiscussionParticipationFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.features.DiscussionRootFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.features.MentionFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.features.TermMatchFeatureCommand;
import de.spektrumprojekt.i.ranker.chain.features.TermMatchFeatureCommand.TermWeightAggregation;
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
    public enum RankerConfigurationFlag {
        ONLY_USE_TERM_MATCHER_FEATURE,
        DO_NOT_USE_DISCUSSION_FEATURES,
        USE_DIRECTED_USER_MODEL_ADAPTATION;
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(Ranker.class);

    /**
     * TODO move {@link ConfigurationDescriptable} to spektrum and let {@link CommandChain}
     * implement this
     * 
     * @param chain
     * @return
     */
    public static String getCommandsToString(CommandChain<?> chain) {
        Collection<String> names = new ArrayList<String>(chain.getCommands().size());
        for (Command<?> command : chain.getCommands()) {
            names.add(command.getClass().getSimpleName());
        }
        return StringUtils.join(names, ", ");
    }

    private final Persistence persistence;

    private final Communicator communicator;
    private CommandChain<MessageFeatureContext> rankerChain;

    private final Collection<RankerConfigurationFlag> flags = new HashSet<RankerConfigurationFlag>();

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

        rankerChain = new CommandChain<MessageFeatureContext>();
        InformationExtractionCommand<MessageFeatureContext> ieCommand = InformationExtractionCommand
                .createDefaultGermanEnglish(this.persistence, false, false);
        UserFeatureCommand userFeatureCommand = new UserFeatureCommand(memberRunner);

        rankerChain.addCommand(ieCommand);

        // store the message after the terms have been extracted
        rankerChain.addCommand(new StoreMessageCommand(persistence));

        if (!this.flags.contains(RankerConfigurationFlag.ONLY_USE_TERM_MATCHER_FEATURE)
                && !this.flags.contains(RankerConfigurationFlag.DO_NOT_USE_DISCUSSION_FEATURES)) {
            rankerChain.addCommand(new DiscussionRootFeatureCommand());
        }
        rankerChain.addCommand(userFeatureCommand);

        if (!this.flags.contains(RankerConfigurationFlag.ONLY_USE_TERM_MATCHER_FEATURE)) {
            userFeatureCommand.getUserSpecificCommandChain().addCommand(new AuthorFeatureCommand());
            userFeatureCommand.getUserSpecificCommandChain()
                    .addCommand(new MentionFeatureCommand());
        }

        if (!this.flags.contains(RankerConfigurationFlag.ONLY_USE_TERM_MATCHER_FEATURE) &&
                !this.flags.contains(RankerConfigurationFlag.DO_NOT_USE_DISCUSSION_FEATURES)) {
            userFeatureCommand.getUserSpecificCommandChain().addCommand(
                    new DiscussionParticipationFeatureCommand());
            userFeatureCommand.getUserSpecificCommandChain().addCommand(
                    new DiscussionMentionFeatureCommand());
        }

        userFeatureCommand.getUserSpecificCommandChain().addCommand(
                new TermMatchFeatureCommand(persistence, TermWeightAggregation.AVG, 0.75f));
        userFeatureCommand.getUserSpecificCommandChain().addCommand(
                new ComputeMessageRankCommand());
        userFeatureCommand.getUserSpecificCommandChain().addCommand(
                new InvokeLearnerCommand(this.persistence, this.communicator));
        if (this.flags.contains(RankerConfigurationFlag.USE_DIRECTED_USER_MODEL_ADAPTATION)) {
            userFeatureCommand.getUserSpecificCommandChain().addCommand(
                    new TriggerUserModelAdaptationCommand());
        }
        rankerChain.addCommand(new StoreMessageRankCommand(persistence));

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
        sb.append(" flags: {" + StringUtils.join(this.flags, ", ") + "}");
        sb.append(" commandChain: " + this.rankerChain.getConfigurationDescription());

        return sb.toString();
    }

    public Collection<RankerConfigurationFlag> getFlags() {
        return Collections.unmodifiableCollection(flags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<RankingCommunicationMessage> getMessageClass() {
        return RankingCommunicationMessage.class;
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
     * {@inheritDoc}
     */
    @Override
    public boolean supports(CommunicationMessage message) {
        return message instanceof RankingCommunicationMessage;
    }

}
