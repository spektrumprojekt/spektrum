package de.spektrumprojekt.i.collab;

import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.i.ranker.CollaborativeConfiguration;
import de.spektrumprojekt.i.term.similarity.TermVectorSimilarityComputer;
import de.spektrumprojekt.persistence.Persistence;

public enum CollaborativeScoreComputerType {
    USER2MESSAGE("U2M"),
    USER2MESSAGE_PER_MESSAGE_GROUP("U2MGM"),
    USER2TERM("U2T"),
    USER2TERM_PER_MESSAGE_GROUP("U2TMG");

    private final String shortName;

    private CollaborativeScoreComputerType(String shortName) {
        this.shortName = shortName;
    }

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
        case USER2MESSAGE_PER_MESSAGE_GROUP:
            collaborativeRankerComputer = new CombiningUserToMessageGroupSpecificMessageCollaborativeScoreComputer(
                    persistence,
                    collaborativeConfiguration,
                    observationTypesToUseForDataModel);
            break;
        default:
            throw new IllegalArgumentException(this + " is unhandled.");

        }
        return collaborativeRankerComputer;
    }

    public String getShortName() {
        return shortName;
    }
}