package de.spektrumprojekt.i.collab;

import de.spektrumprojekt.callbacks.MessageGroupMemberRunner;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.i.learner.Learner;
import de.spektrumprojekt.i.scorer.CollaborativeConfiguration;
import de.spektrumprojekt.i.scorer.MessageFeatureContext;
import de.spektrumprojekt.i.scorer.Scorer;
import de.spektrumprojekt.i.scorer.ScorerConfiguration;
import de.spektrumprojekt.i.scorer.special.SpecialScorer;
import de.spektrumprojekt.persistence.Persistence;

public class CollaborativeIntelligenceFactory {

    private Learner learner;
    private Scorer scorer;

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

        CollaborativeConfiguration collaborativeConfiguration = scorerConfiguration
                .getCollaborativeConfiguration();

        if (!CollaborativeScoreComputerType.USER2MESSAGE.equals(collaborativeConfiguration
                .getCollaborativeScoreComputerType())) {
            throw new UnsupportedOperationException(
                    "Cannot handle this "
                            + collaborativeConfiguration.getCollaborativeScoreComputerType()
                            + ".");
        }

        FullCollRelevanceScoreCommand fullCollabRankCommand = new FullCollRelevanceScoreCommand(
                persistence,
                observationTypesToUseForDataModel,
                collaborativeConfiguration,
                null);

        scorer = new SpecialScorer<FullCollRelevanceScoreCommand>(persistence,
                communicator, memberRunner, scorerConfiguration, fullCollabRankCommand);
        learner = new Learner(persistence, scorerConfiguration, null);
        // learner.getLearnerChain().addCommand(collabLearnCommand);
    }

    public Learner getLearner() {
        return learner;
    }

    public Scorer getScorer() {
        return scorer;
    }
}
