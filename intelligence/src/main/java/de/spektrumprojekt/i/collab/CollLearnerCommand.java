package de.spektrumprojekt.i.collab;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.recommender.Recommender;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.commons.chain.CommandException;
import de.spektrumprojekt.datamodel.observation.Observation;
import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.i.learner.LearnerMessageContext;
import de.spektrumprojekt.persistence.Persistence;

public class CollLearnerCommand implements Command<LearnerMessageContext> {

    private final Recommender recommender;
    private final Persistence persistence;

    public CollLearnerCommand(Persistence persistence, Recommender recommender) {
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
    public void process(LearnerMessageContext context) throws CommandException {
        Observation obs = context.getObservation();
        if (obs != null) {
            if (ObservationType.RATING.equals(obs.getObservationType())
                    && obs.getInterest() != null) {
                User user = this.persistence.getUserByGlobalId(obs.getUserGlobalId());
                if (user == null) {
                    throw new RuntimeException("user cannot be null. " + obs.getUserGlobalId());
                }
                if (!obs.getMessageGlobalId().equals(context.getMessage().getGlobalId())) {
                    throw new RuntimeException(
                            "messageId of observation not equal with message id of context. "
                                    + context.getMessage() + " " + obs);
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
                    recommender.setPreference(userId, messageId,
                            CollaborativeRankerComputer.convertScoreToMahoutValue(obs
                                    .getInterest().getScore()));
                } catch (TasteException e) {
                    throw new CommandException(false, "Error setting preferences. ", e);
                }
            }
        }
    }

}
