package de.spektrumprojekt.i.similarity.user;

import de.spektrumprojekt.datamodel.message.Message;

public interface IterativeUserSimilarityComputer extends UserSimilarityComputer {

    public void runForMessage(Message message);
}