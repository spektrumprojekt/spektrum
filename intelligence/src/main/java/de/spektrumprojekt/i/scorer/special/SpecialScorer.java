package de.spektrumprojekt.i.scorer.special;

import java.util.Arrays;
import java.util.Collection;

import de.spektrumprojekt.callbacks.MessageGroupMemberRunner;
import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.commons.chain.CommandChain;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.i.scorer.MessageFeatureContext;
import de.spektrumprojekt.i.scorer.Scorer;
import de.spektrumprojekt.i.scorer.ScorerConfiguration;
import de.spektrumprojekt.i.scorer.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.i.scorer.chain.DetermineInteractionLevelCommand;
import de.spektrumprojekt.i.scorer.chain.UserFeatureCommand;
import de.spektrumprojekt.persistence.Persistence;

public class SpecialScorer<T extends Command<UserSpecificMessageFeatureContext>> extends Scorer {

    private final CommandChain<MessageFeatureContext> scorerChain;
    private final Persistence persistence;
    private final UserFeatureCommand userFeatureCommand;
    private final T specificCommand;

    public SpecialScorer(
            Persistence persistence,
            Communicator communicator,
            MessageGroupMemberRunner<MessageFeatureContext> memberRunner,
            ScorerConfiguration rankerConfiguration,
            T specificCommand) {
        super(persistence, communicator, memberRunner, rankerConfiguration);
        this.persistence = persistence;
        this.specificCommand = specificCommand;

        scorerChain = new CommandChain<MessageFeatureContext>();
        userFeatureCommand = new UserFeatureCommand(memberRunner);

        scorerChain.addCommand(getStoreMessageCommand());
        scorerChain.addCommand(userFeatureCommand);

        userFeatureCommand.addCommand(new DetermineInteractionLevelCommand());
        userFeatureCommand.addCommand(this.specificCommand);

        userFeatureCommand.addCommand(getFeatureStatisticsCommand());
        scorerChain.addCommand(getStoreMessageScoreCommand());
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " rankerChain: "
                + scorerChain.getConfigurationDescription();
    }

    @Override
    public MessageFeatureContext rescore(Message message, String userGlobalId) {

        throw new UnsupportedOperationException("not supported in special ranker.");
    }

    @Override
    public MessageFeatureContext score(Message message, MessageRelation messageRelation,
            String[] userGlobalIdsToRankFor, boolean noRankingOnlyLearning) {

        MessageFeatureContext context = new MessageFeatureContext(this.persistence, message,
                messageRelation);
        context.setNoScoreingOnlyLearning(noRankingOnlyLearning);

        if (userGlobalIdsToRankFor != null) {
            Collection<String> users = Arrays.asList(userGlobalIdsToRankFor);
            context.setUserGlobalIdsToProcess(users);
        }

        scorerChain.process(context);

        return context;
    }

}