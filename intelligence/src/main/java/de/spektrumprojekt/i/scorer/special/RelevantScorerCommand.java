package de.spektrumprojekt.i.scorer.special;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.UserMessageScore;
import de.spektrumprojekt.i.scorer.UserSpecificMessageFeatureContext;

public class RelevantScorerCommand implements Command<UserSpecificMessageFeatureContext> {
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " RandomRanking.";
    }

    public void process(UserSpecificMessageFeatureContext context) {
        UserMessageScore messageRank = new UserMessageScore(context.getMessage().getGlobalId(),
                context.getUserGlobalId());
        messageRank.setScore(1f);
        messageRank.setInteractionLevel(context.getInteractionLevel());
        context.setMessageRank(messageRank);
    }
}