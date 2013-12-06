package de.spektrumprojekt.i.user.similarity;

import de.spektrumprojekt.datamodel.message.Message;

public interface IterativeUserSimilarityComputer extends UserSimilarityComputer {

    public void runForMessage(Message message);
}