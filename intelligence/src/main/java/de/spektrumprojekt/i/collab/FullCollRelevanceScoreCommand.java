package de.spektrumprojekt.i.collab;

import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.recommender.Recommender;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.commons.chain.CommandException;
import de.spektrumprojekt.datamodel.message.UserMessageScore;
import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.i.collab.CollaborativeScoreComputer.CollaborativeScoreComputerType;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.i.term.similarity.TermVectorSimilarityComputer;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.simple.SimplePersistence;

public class FullCollRelevanceScoreCommand implements Command<UserSpecificMessageFeatureContext> {

    private final SimplePersistence persistence;

    private CollaborativeScoreComputer collaborativeRankerComputer;

    private final boolean useGenericRecommender;

    private int lastObservationsSize = 0;

    private final ObservationType[] observationTypesToUseForDataModel;

    private final CollaborativeScoreComputerType collaborativeScoreComputerType;

    private final TermVectorSimilarityComputer termVectorSimilarityComputer;

    public FullCollRelevanceScoreCommand(
            Persistence persistence,
            ObservationType[] observationTypesToUseForDataModel,
            CollaborativeScoreComputerType collaborativeScoreComputerType,
            TermVectorSimilarityComputer termVectorSimilarityComputer,
            boolean useGenericRecommender) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (collaborativeScoreComputerType == null) {
            throw new IllegalArgumentException("collaborativeScoreComputerType cannot be null.");
        }
        if (!(persistence instanceof SimplePersistence)) {
            throw new IllegalArgumentException("Can only handle SimplePersistence at the moment.");
        }
        if (observationTypesToUseForDataModel == null
                || observationTypesToUseForDataModel.length == 0) {
            throw new IllegalArgumentException(
                    "observationTypesToUseForDataModel cannot be null or empty.");
        }
        this.persistence = (SimplePersistence) persistence;
        this.observationTypesToUseForDataModel = observationTypesToUseForDataModel;
        this.useGenericRecommender = useGenericRecommender;
        this.collaborativeScoreComputerType = collaborativeScoreComputerType;
        this.termVectorSimilarityComputer = termVectorSimilarityComputer;
    }

    @Override
    public String getConfigurationDescription() {
        return getClass().getSimpleName()
                + " observationTypesToUseForDataModel: " + observationTypesToUseForDataModel
                + " useGenericRecommender: " + useGenericRecommender
                + " collaborativeScoreComputerType: " + collaborativeScoreComputerType
                + " termVectorSimilarityComputer: " + termVectorSimilarityComputer;
    }

    private Recommender getRecommender() throws Exception {

        int currentObs = persistence.getObservations(ObservationType.RATING).size();
        if (currentObs > lastObservationsSize) {

            collaborativeRankerComputer = this.collaborativeScoreComputerType.createComputer(
                    persistence,
                    observationTypesToUseForDataModel,
                    termVectorSimilarityComputer,
                    useGenericRecommender);
            collaborativeRankerComputer.init();

            lastObservationsSize = currentObs;

        }

        return collaborativeRankerComputer == null ? null : collaborativeRankerComputer
                .getRecommender();
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
            Recommender recommender = getRecommender();
            if (recommender != null) {
                float estimate = recommender.estimatePreference(userId, messageId);
                float rank = CollaborativeScoreComputer
                        .convertScoreFromMahoutValue(estimate, true);

                UserMessageScore messageRank = new UserMessageScore(context.getMessage()
                        .getGlobalId(),
                        context.getUserGlobalId());
                messageRank.setScore(rank);

                context.setMessageRank(messageRank);
            }
        } catch (NoSuchUserException e) {
            // ignore
        } catch (Exception e) {
            throw new CommandException("Error estimating preference. " + context, e);
        }
    }

}
