package de.spektrumprojekt.i.ranker.special;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.UserMessageScore;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;

public class RandomRankerCommand implements Command<UserSpecificMessageFeatureContext> {

    private final Double thresholdOfRankingRelevant;

    public RandomRankerCommand(Double thresholdOfRankingRelevant) {
        if (thresholdOfRankingRelevant != null
                && (thresholdOfRankingRelevant < 0 || thresholdOfRankingRelevant > 1)) {
            throw new IllegalArgumentException("thresholdOfRankingRelevant must be in [0..1]");
        }
        this.thresholdOfRankingRelevant = thresholdOfRankingRelevant;
    }

    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " RandomRanking.";
    }

    public void process(UserSpecificMessageFeatureContext context) {
        UserMessageScore messageRank = new UserMessageScore(context.getMessage().getGlobalId(),
                context.getUserGlobalId());
        double rnd = Math.random();
        if (thresholdOfRankingRelevant == null) {
            messageRank.setScore((float) rnd);
        } else {
            messageRank.setScore(rnd >= thresholdOfRankingRelevant ? 1 : 0);
        }
        messageRank.setInteractionLevel(context.getInteractionLevel());
        context.setMessageRank(messageRank);

    }
}