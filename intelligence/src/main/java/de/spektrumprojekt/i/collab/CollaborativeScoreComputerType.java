package de.spektrumprojekt.i.collab;

import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.i.term.similarity.TermVectorSimilarityComputer;
import de.spektrumprojekt.persistence.Persistence;

public enum CollaborativeScoreComputerType {
    USER2MESSAGE,
    USER2TERM,
    USER2TERM_PER_MESSAGE_GROUP;

    public CollaborativeScoreComputer createComputer(
            Persistence persistence,
            ObservationType[] observationTypesToUseForDataModel,
            TermVectorSimilarityComputer termVectorSimilarityComputer,
            boolean useGenericRecommender) {
        CollaborativeScoreComputer collaborativeRankerComputer;
        switch (this) {
        case USER2MESSAGE:
            collaborativeRankerComputer = new UserToMessageCollaborativeScoreComputer(
                    persistence,
                    observationTypesToUseForDataModel,
                    useGenericRecommender);
            break;
        case USER2TERM:
            collaborativeRankerComputer = new UserToTermCollaborativeScoreComputer(
                    persistence,
                    termVectorSimilarityComputer,
                    useGenericRecommender);
            break;
        case USER2TERM_PER_MESSAGE_GROUP:
            collaborativeRankerComputer = new CombiningUserToMessageGroupSpecificTermCollaborativeScoreComputer(
                    persistence, termVectorSimilarityComputer, useGenericRecommender);
            break;
        default:
            throw new IllegalArgumentException(this + " is unhandled.");

        }
        return collaborativeRankerComputer;
    }
}