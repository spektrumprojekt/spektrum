package de.spektrumprojekt.i.collab;

import de.spektrumprojekt.callbacks.MessageGroupMemberRunner;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.i.collab.CollaborativeScoreComputer.CollaborativeScoreComputerType;
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
            ScorerConfiguration rankerConfiguration,
            ObservationType[] observationTypesToUseForDataModel,
            CollaborativeScoreComputerType collaborativeScoreComputerType,
            boolean useGenericRecommender)
            throws Exception {
        // collaborativeRankerComputer.init();
        /*
         * IterativeCollRelevanceScoreCommand collabRankCommand = new
         * IterativeCollRelevanceScoreCommand( persistence,
         * collaborativeRankerComputer.getRecommender()); CollLearnerCommand collabLearnCommand =
         * new CollLearnerCommand(persistence, collaborativeRankerComputer.getRecommender());
         */

        if (CollaborativeScoreComputerType.USER2TERM.equals(collaborativeScoreComputerType)) {
            throw new UnsupportedOperationException(
                    "Cannot handle this way, must be working together with user model.");
        }

        FullCollRelevanceScoreCommand fullCollabRankCommand = new FullCollRelevanceScoreCommand(
                persistence,
                observationTypesToUseForDataModel,
                collaborativeScoreComputerType,
                null,
                useGenericRecommender);

        ranker = new SpecialRanker<FullCollRelevanceScoreCommand>(persistence,
                communicator, memberRunner, rankerConfiguration, fullCollabRankCommand);
        learner = new Learner(persistence, rankerConfiguration, null);
        // learner.getLearnerChain().addCommand(collabLearnCommand);
    }

    public Learner getLearner() {
        return learner;
    }

    public Scorer getRanker() {
        return ranker;
    }
}
