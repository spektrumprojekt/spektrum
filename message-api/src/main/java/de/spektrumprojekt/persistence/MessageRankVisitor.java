package de.spektrumprojekt.persistence;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageRank;

public interface MessageRankVisitor {

    public void visit(MessageRank messageRank, Message message) throws Exception;
}