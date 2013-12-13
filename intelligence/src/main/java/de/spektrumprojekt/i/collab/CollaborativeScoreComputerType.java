package de.spektrumprojekt.i.collab;

import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.i.ranker.CollaborativeConfiguration;
import de.spektrumprojekt.i.term.similarity.TermVectorSimilarityComputer;
import de.spektrumprojekt.persistence.Persistence;

public enum CollaborativeScoreComputerType {
    USER2MESSAGE,
    USER2TERM,
    USER2TERM_PER_MESSAGE_GROUP;

    public CollaborativeScoreComputer createComputer(
            Persistence persistence,
            CollaborativeConfiguration collaborativeConfiguration,
            ObservationType[] observationTypesToUseForDataModel,
            TermVectorSimilarityComputer termVectorSimilarityComputer) {
        CollaborativeScoreComputer collaborativeRankerComputer;
        switch (this) {
        case USER2MESSAGE:
            collaborativeRankerComputer = new UserToMessageCollaborativeScoreComputer(
                    persistence,
                    collaborativeConfiguration,
                    observationTypesToUseForDataModel);
            break;
        case USER2TERM:
            collaborativeRankerComputer = new UserToTermCollaborativeScoreComputer(
                    persistence,
                    collaborativeConfiguration,
                    termVectorSimilarityComputer);
            break;
        case USER2TERM_PER_MESSAGE_GROUP:
            collaborativeRankerComputer = new CombiningUserToMessageGroupSpecificTermCollaborativeScoreComputer(
                    persistence,
                    collaborativeConfiguration,
                    termVectorSimilarityComputer);
            break;
        default:
            throw new IllegalArgumentException(this + " is unhandled.");

        }
        return collaborativeRankerComputer;
    }
}