package de.spektrumprojekt.i.learner.adaptation;

import de.spektrumprojekt.commons.event.Event;
import de.spektrumprojekt.i.scorer.MessageFeatureContext;

public class UserModelAdaptationReScoreEvent implements Event {

    private MessageFeatureContext messageFeatureContextOfReScore;
    private DirectedUserModelAdaptationMessage adaptationMessage;

    public DirectedUserModelAdaptationMessage getAdaptationMessage() {
        return adaptationMessage;
    }

    public MessageFeatureContext getMessageFeatureContextOfReScore() {
        return messageFeatureContextOfReScore;
    }

    public void setAdaptationMessage(DirectedUserModelAdaptationMessage adaptationMessage) {
        this.adaptationMessage = adaptationMessage;
    }

    public void setMessageFeatureContextOfReScore(MessageFeatureContext messageFeatureContextOfReScore) {
        this.messageFeatureContextOfReScore = messageFeatureContextOfReScore;
    }
}