package de.spektrumprojekt.persistence;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.UserMessageScore;

public interface UserMessageScoreVisitor {

    public void visit(UserMessageScore userMessageScore, Message message) throws Exception;
}