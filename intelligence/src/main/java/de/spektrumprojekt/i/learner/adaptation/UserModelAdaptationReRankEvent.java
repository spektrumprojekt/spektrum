package de.spektrumprojekt.i.learner.adaptation;

import de.spektrumprojekt.commons.event.Event;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;

public class UserModelAdaptationReRankEvent implements Event {

    private MessageFeatureContext messageFeatureContextOfReRank;
    private DirectedUserModelAdaptationMessage adaptationMessage;

    public DirectedUserModelAdaptationMessage getAdaptationMessage() {
        return adaptationMessage;
    }

    public MessageFeatureContext getMessageFeatureContextOfReRank() {
        return messageFeatureContextOfReRank;
    }

    public void setAdaptationMessage(DirectedUserModelAdaptationMessage adaptationMessage) {
        this.adaptationMessage = adaptationMessage;
    }

    public void setMessageFeatureContextOfReRank(MessageFeatureContext messageFeatureContextOfReRank) {
        this.messageFeatureContextOfReRank = messageFeatureContextOfReRank;
    }
}