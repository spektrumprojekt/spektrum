package de.spektrumprojekt.i.collab;

import java.util.Collection;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.observation.Observation;
import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.i.scorer.CollaborativeConfiguration;
import de.spektrumprojekt.persistence.Persistence;

public class UserToMessageGroupSpecificMessageTermCollaborativeScoreComputer extends
        UserToMessageCollaborativeScoreComputer {

    private final MessageGroup messageGroup;

    public UserToMessageGroupSpecificMessageTermCollaborativeScoreComputer(
            Persistence persistence,
            CollaborativeConfiguration collaborativeConfiguration,
            ObservationType[] observationTypesToUseForDataModel,
            MessageGroup messageGroup) {
        super(persistence, collaborativeConfiguration, observationTypesToUseForDataModel);
        if (messageGroup == null) {
            throw new IllegalArgumentException("messageGroup cannot be null.");
        }
        this.messageGroup = messageGroup;
    }

    @Override
    protected boolean addMessage(Message message, Collection<Observation> collection) {
        return this.messageGroup.getId().equals(message.getMessageGroup().getId());
    }

    @Override
    public String getConfigurationDescription() {
        return super.getConfigurationDescription() + " messageGroup: " + messageGroup;
    }

}