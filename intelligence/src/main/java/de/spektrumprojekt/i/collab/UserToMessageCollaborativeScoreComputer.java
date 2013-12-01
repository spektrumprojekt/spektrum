package de.spektrumprojekt.i.collab;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.simple.SimplePersistence;

/**
 * Collaborative Ranker. Currently only working with a {@link SimplePersistence}
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class UserToMessageCollaborativeScoreComputer extends CollaborativeScoreComputer {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(UserToMessageCollaborativeScoreComputer.class);

    public UserToMessageCollaborativeScoreComputer(Persistence persistence,
            ObservationType[] observationTypesToUseForDataModel, boolean useGenericRecommender) {
        super(persistence, observationTypesToUseForDataModel, useGenericRecommender);
    }

    @Override
    protected void createUserPreference(FastByIDMap<PreferenceArray> userData, User user,
            FileWriter fw) throws IOException {
        GenericUserPreferenceArray genericUserPreferenceArray = new GenericUserPreferenceArray(
                getMessagesWithOberservations().size());

        int index = 0;

        for (Message message : getMessagesWithOberservations()) {
            index = createUserPreferences(user, message, genericUserPreferenceArray, fw,
                    index);
        }

        if (index > 0) {
            userData.put(user.getId(), genericUserPreferenceArray);
        }
    }

    protected int createUserPreferences(User user, Message message,
            GenericUserPreferenceArray genericUserPreferenceArray, FileWriter fw, int index)
            throws IOException {
        boolean add = false;
        float pref = getPreferenceValue(getObservations(), user.getGlobalId(),
                message.getGlobalId());

        if (pref != 0) {
            add = true;
            incrementPreferenceCount();
        }
        if (pref == 1) {
            incrementPositivePreferenceCount();
        }
        if (pref == -1) {
            incrementNegativePreferenceCount();
        }
        if (pref < -1 || pref > 1) {
            throw new RuntimeException(message + " " + user.getGlobalId() + " "
                    + pref);
        }

        if (add) {

            genericUserPreferenceArray.setItemID(index, message.getId());
            genericUserPreferenceArray.setUserID(index, user.getId());
            genericUserPreferenceArray.setValue(index, pref);

            index++;

            if (isOutPreferencesToFile()) {
                fw.write(index + " " + message.getGlobalId() + " " + user.getId() + " "
                        + pref
                        + "\n");
            }
        }
        return index;
    }

    @Override
    protected float estimate(User user, Message message) throws TasteException {
        final float estimate = getRecommender().estimatePreference(user.getId(),
                message.getId());
        return estimate;
    }

    @Override
    public String getConfigurationDescription() {
        return super.getConfigurationDescription();
    }

}