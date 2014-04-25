package de.spektrumprojekt.persistence;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.UserMessageScore;

public interface MessageRankVisitor {

    public void visit(UserMessageScore messageRank, Message message) throws Exception;
}