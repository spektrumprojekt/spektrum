package de.spektrumprojekt.i.collab;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.UserMessageScore;
import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.scorer.CollaborativeConfiguration;
import de.spektrumprojekt.persistence.Persistence;

/**
 * Combines several {@link UserToMessageGroupSpecificTermCollaborativeScoreComputer} for each
 * message group.
 * 
 * 
 */
public class CombiningUserToMessageGroupSpecificMessageCollaborativeScoreComputer extends
        CollaborativeScoreComputer {

    private ObservationType[] observationTypesToUseForDataModel;

    public CombiningUserToMessageGroupSpecificMessageCollaborativeScoreComputer(
            Persistence persistence,
            CollaborativeConfiguration collaborativeConfiguration,
            ObservationType[] observationTypesToUseForDataModel) {
        super(persistence, collaborativeConfiguration);

        if (observationTypesToUseForDataModel == null) {
            throw new IllegalArgumentException("observationTypesToUseForDataModel cannot be null.");
        }
        this.observationTypesToUseForDataModel = observationTypesToUseForDataModel;
    }

    @Override
    protected void createUserPreference(FastByIDMap<PreferenceArray> userData, User user,
            FileWriter fw) throws IOException {
        throw new IllegalStateException("should not be called.");
    }

    @Override
    protected float estimate(User user, Message message) throws TasteException {
        throw new IllegalStateException("should not be called.");
    }

    @Override
    public void init() throws InconsistentDataException, TasteException {
        // nothing to do
    }

    @Override
    public void run(Collection<Message> messagesToRun) throws TasteException {

        this.setMessageScores(new HashSet<UserMessageScore>());

        Map<MessageGroup, Collection<Message>> mg2messages = MessageHelper
                .splitByMessageGroup(messagesToRun);

        for (Entry<MessageGroup, Collection<Message>> mg2messageEntry : mg2messages.entrySet()) {

            UserToMessageGroupSpecificMessageTermCollaborativeScoreComputer mgCollabComputer = new UserToMessageGroupSpecificMessageTermCollaborativeScoreComputer(
                    getPersistence(),
                    getCollaborativeConfiguration(),
                    observationTypesToUseForDataModel,
                    mg2messageEntry.getKey());

            mgCollabComputer.init();
            mgCollabComputer.run(mg2messageEntry.getValue());

            this.setNanScores(this.getNanScores() + mgCollabComputer.getNanScores());
            this.setNegativePreferenceCount(this.getNegativePreferenceCount()
                    + mgCollabComputer.getNegativePreferenceCount());
            this.setNonZeroRanks(this.getNonZeroRanks() + mgCollabComputer.getNonZeroRanks());
            this.setPositivePreferenceCount(this.getPositivePreferenceCount()
                    + mgCollabComputer.getPositivePreferenceCount());
            this.setPreferenceCount(this.getPreferenceCount()
                    + mgCollabComputer.getPreferenceCount());

            this.getMessageScores().addAll(mgCollabComputer.getMessageScores());
        }
    }

}