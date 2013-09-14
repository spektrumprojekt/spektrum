package de.spektrumprojekt.i.ranker.special;

import java.util.Arrays;
import java.util.Collection;

import de.spektrumprojekt.callbacks.MessageGroupMemberRunner;
import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.commons.chain.CommandChain;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;
import de.spektrumprojekt.i.ranker.Ranker;
import de.spektrumprojekt.i.ranker.RankerConfiguration;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.i.ranker.chain.DetermineInteractionLevelCommand;
import de.spektrumprojekt.i.ranker.chain.UserFeatureCommand;
import de.spektrumprojekt.persistence.Persistence;

public class SpecialRanker<T extends Command<UserSpecificMessageFeatureContext>> extends Ranker {

    private final CommandChain<MessageFeatureContext> rankerChain;
    private final Persistence persistence;
    private final UserFeatureCommand userFeatureCommand;
    private final T specificCommand;

    public SpecialRanker(
            Persistence persistence,
            Communicator communicator,
            MessageGroupMemberRunner<MessageFeatureContext> memberRunner,
            RankerConfiguration rankerConfiguration,
            T specificCommand) {
        super(persistence, communicator, memberRunner, rankerConfiguration);
        this.persistence = persistence;
        this.specificCommand = specificCommand;

        rankerChain = new CommandChain<MessageFeatureContext>();
        userFeatureCommand = new UserFeatureCommand(memberRunner);

        rankerChain.addCommand(getStoreMessageCommand());
        rankerChain.addCommand(userFeatureCommand);

        userFeatureCommand.addCommand(new DetermineInteractionLevelCommand());
        userFeatureCommand.addCommand(this.specificCommand);

        userFeatureCommand.addCommand(getFeatureStatisticsCommand());
        rankerChain.addCommand(getStoreMessageRankCommand());
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " rankerChain: "
                + rankerChain.getConfigurationDescription();
    }

    @Override
    public MessageFeatureContext rank(Message message, MessageRelation messageRelation,
            String[] userGlobalIdsToRankFor, boolean noRankingOnlyLearning) {

        MessageFeatureContext context = new MessageFeatureContext(this.persistence, message,
                messageRelation);
        context.setNoRankingOnlyLearning(noRankingOnlyLearning);

        if (userGlobalIdsToRankFor != null) {
            Collection<String> users = Arrays.asList(userGlobalIdsToRankFor);
            context.setUserGlobalIdsToProcess(users);
        }

        rankerChain.process(context);

        return context;
    }

    @Override
    public MessageFeatureContext rerank(Message message, String userGlobalId) {

        throw new UnsupportedOperationException("not supported in special ranker.");
    }

}