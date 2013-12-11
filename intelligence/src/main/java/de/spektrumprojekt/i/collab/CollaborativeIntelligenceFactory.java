package de.spektrumprojekt.i.collab;

import de.spektrumprojekt.callbacks.MessageGroupMemberRunner;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.i.learner.Learner;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;
import de.spektrumprojekt.i.ranker.Scorer;
import de.spektrumprojekt.i.ranker.ScorerConfiguration;
import de.spektrumprojekt.i.ranker.special.SpecialRanker;
import de.spektrumprojekt.persistence.Persistence;

public class CollaborativeIntelligenceFactory {

    private Learner learner;
    private Scorer ranker;

    public CollaborativeIntelligenceFactory(
            Persistence persistence,
            Communicator communicator,
            MessageGroupMemberRunner<MessageFeatureContext> memberRunner,
            ScorerConfiguration scorerConfiguration,
            ObservationType[] observationTypesToUseForDataModel,
            boolean useGenericRecommender)
            throws Exception {
        // collaborativeRankerComputer.init();
        /*
         * IterativeCollRelevanceScoreCommand collabRankCommand = new
         * IterativeCollRelevanceScoreCommand( persistence,
         * collaborativeRankerComputer.getRecommender()); CollLearnerCommand collabLearnCommand =
         * new CollLearnerCommand(persistence, collaborativeRankerComputer.getRecommender());
         */

        if (!CollaborativeScoreComputerType.USER2MESSAGE.equals(scorerConfiguration
                .getCollaborativeScoreComputerType())) {
            throw new UnsupportedOperationException(
                    "Cannot handle this " + scorerConfiguration.getCollaborativeScoreComputerType()
                            + ".");
        }

        FullCollRelevanceScoreCommand fullCollabRankCommand = new FullCollRelevanceScoreCommand(
                persistence,
                observationTypesToUseForDataModel,
                scorerConfiguration.getCollaborativeScoreComputerType(),
                null,
                useGenericRecommender);

        ranker = new SpecialRanker<FullCollRelevanceScoreCommand>(persistence,
                communicator, memberRunner, scorerConfiguration, fullCollabRankCommand);
        learner = new Learner(persistence, scorerConfiguration, null);
        // learner.getLearnerChain().addCommand(collabLearnCommand);
    }

    public Learner getLearner() {
        return learner;
    }

    public Scorer getRanker() {
        return ranker;
    }
}
