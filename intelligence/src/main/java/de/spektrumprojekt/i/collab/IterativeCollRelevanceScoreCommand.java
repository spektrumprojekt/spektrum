package de.spektrumprojekt.i.collab;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.recommender.Recommender;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.commons.chain.CommandException;
import de.spektrumprojekt.datamodel.message.UserMessageScore;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.persistence.Persistence;

public class IterativeCollRelevanceScoreCommand implements Command<UserSpecificMessageFeatureContext> {

    private final Recommender recommender;
    private final Persistence persistence;

    public IterativeCollRelevanceScoreCommand(Persistence persistence, Recommender recommender) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (recommender == null) {
            throw new IllegalArgumentException("recommender cannot be null.");
        }
        this.persistence = persistence;
        this.recommender = recommender;
    }

    @Override
    public String getConfigurationDescription() {
        return getClass().getSimpleName() + " recommender: " + recommender.toString();
    }

    @Override
    public void process(UserSpecificMessageFeatureContext context) throws CommandException {
        User user = this.persistence.getUserByGlobalId(context.getUserGlobalId());
        if (user == null) {
            throw new RuntimeException("user cannot be null. " + context.getUserGlobalId());
        }
        Long messageId = context.getMessage().getId();
        Long userId = user.getId();
        if (messageId == null) {
            throw new RuntimeException("messageId cannot be null. " + context.getMessage());
        }
        if (userId == null) {
            throw new RuntimeException("userId cannot be null. " + context.getMessage());
        }
        try {
            float estimate = recommender.estimatePreference(userId, messageId);
            float rank = CollaborativeRankerComputer.convertScoreFromMahoutValue(estimate, true);

            UserMessageScore messageRank = new UserMessageScore(context.getMessage().getGlobalId(),
                    context.getUserGlobalId());
            messageRank.setScore(rank);

            context.setMessageRank(messageRank);

        } catch (TasteException e) {
            throw new CommandException("Error estimating preference. " + context);
        }
    }

}
