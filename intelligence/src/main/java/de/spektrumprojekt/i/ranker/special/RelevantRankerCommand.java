package de.spektrumprojekt.i.ranker.special;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.MessageRank;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;

public class RelevantRankerCommand implements Command<UserSpecificMessageFeatureContext> {
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " RandomRanking.";
    }

    public void process(UserSpecificMessageFeatureContext context) {
        MessageRank messageRank = new MessageRank(context.getMessage().getGlobalId(),
                context.getUserGlobalId());
        messageRank.setRank(1f);
        messageRank.setInteractionLevel(context.getInteractionLevel());
        context.setMessageRank(messageRank);
    }
}