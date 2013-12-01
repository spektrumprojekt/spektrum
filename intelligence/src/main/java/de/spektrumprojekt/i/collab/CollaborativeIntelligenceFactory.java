package de.spektrumprojekt.i.collab;

import de.spektrumprojekt.callbacks.MessageGroupMemberRunner;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.i.collab.CollaborativeScoreComputer.CollaborativeScoreComputerType;
import de.spektrumprojekt.i.learner.Learner;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;
import de.spektrumprojekt.i.ranker.Ranker;
import de.spektrumprojekt.i.ranker.RankerConfiguration;
import de.spektrumprojekt.i.ranker.special.SpecialRanker;
import de.spektrumprojekt.persistence.Persistence;

public class CollaborativeIntelligenceFactory {

    private Learner learner;
    private Ranker ranker;

    public CollaborativeIntelligenceFactory(
            Persistence persistence,
            Communicator communicator,
            MessageGroupMemberRunner<MessageFeatureContext> memberRunner,
            RankerConfiguration rankerConfiguration,
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

        FullCollRelevanceScoreCommand fullCollabRankCommand = new FullCollRelevanceScoreCommand(
                persistence,
                observationTypesToUseForDataModel,
                collaborativeScoreComputerType,
                useGenericRecommender);

        ranker = new SpecialRanker<FullCollRelevanceScoreCommand>(persistence,
                communicator, memberRunner, rankerConfiguration, fullCollabRankCommand);
        learner = new Learner(persistence, rankerConfiguration, null);
        // learner.getLearnerChain().addCommand(collabLearnCommand);
    }

    public Learner getLearner() {
        return learner;
    }

    public Ranker getRanker() {
        return ranker;
    }
}
