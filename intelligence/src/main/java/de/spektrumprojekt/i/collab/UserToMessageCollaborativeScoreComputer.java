package de.spektrumprojekt.i.collab;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.observation.Interest;
import de.spektrumprojekt.datamodel.observation.Observation;
import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.simple.SimplePersistence;
import de.spektrumprojekt.persistence.simple.SimplePersistence.ObservationKey;

/**
 * Collaborative Ranker. Currently only working with a {@link SimplePersistence}
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class UserToMessageCollaborativeScoreComputer extends CollaborativeScoreComputer {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(UserToMessageCollaborativeScoreComputer.class);

    private Map<ObservationKey, Collection<Observation>> observations;
    private Collection<Message> messagesWithOberservations;

    public final static ObservationType[] OT_ONLY_RATINGS = new ObservationType[] { ObservationType.RATING };

    public final static ObservationType[] OT_ONLY_MESSAGE = new ObservationType[] { ObservationType.MESSAGE };

    private final ObservationType[] observationTypesToUseForDataModel;

    public UserToMessageCollaborativeScoreComputer(Persistence persistence,
            ObservationType[] observationTypesToUseForDataModel, boolean useGenericRecommender) {
        super(persistence, useGenericRecommender);

        if (observationTypesToUseForDataModel == null
                || observationTypesToUseForDataModel.length == 0) {
            throw new IllegalArgumentException(
                    "observationTypesToUseForDataModel cannot be null or empty.");
        }
        this.observationTypesToUseForDataModel = observationTypesToUseForDataModel;
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

    public Interest getBestInterestOfObservations(
            Map<ObservationKey, Collection<Observation>> allObservations, String userId,
            String messageGlobalId, ObservationType observationType) {
        Collection<Observation> observations = allObservations.get(new ObservationKey(userId,
                messageGlobalId, observationType));

        Observation maxPrioObs = getMaxPriorityObservation(observations);

        Interest interest = maxPrioObs == null ? null : maxPrioObs.getInterest();
        return interest;
    }

    @Override
    public String getConfigurationDescription() {
        return super.getConfigurationDescription()
                + " observationTypesToUseForDataModel: "
                + Arrays.toString(this.observationTypesToUseForDataModel);

    }

    public Observation getMaxPriorityObservation(Collection<Observation> observations) {
        Observation maxPrioObs = null;

        if (observations != null) {
            for (Observation observation : observations) {
                if (maxPrioObs == null
                        || observation.getPriority().hasHigherPriorityAs(
                                maxPrioObs.getPriority())) {
                    maxPrioObs = observation;
                } else if (!observation.getPriority().hasLowerPriorityAs(
                        maxPrioObs.getPriority())) {
                    if (maxPrioObs.getInterest() == null && observation.getInterest() != null) {
                        maxPrioObs = observation;
                    }
                    else if (maxPrioObs.getInterest() != null
                            && observation.getInterest() != null
                            && maxPrioObs.getInterest().getScore() < observation.getInterest()
                                    .getScore()) {
                        maxPrioObs = observation;
                    }
                }
            }
        }
        return maxPrioObs;
    }

    @Override
    protected Collection<Message> getMessageForEstimation() {
        return messagesWithOberservations;
    }

    public Collection<Message> getMessagesWithOberservations() {
        return messagesWithOberservations;
    }

    public Map<ObservationKey, Collection<Observation>> getObservations() {
        return observations;
    }

    public ObservationType[] getObservationTypesToUseForDataModel() {
        return observationTypesToUseForDataModel;
    }

    protected float getPreferenceValue(
            Map<ObservationKey, Collection<Observation>> allObservations, String userId,
            String messageGlobalId) {

        Interest interest = null;
        for (ObservationType observationType : getObservationTypesToUseForDataModel()) {

            interest = getBestInterestOfObservations(allObservations, userId, messageGlobalId,
                    observationType);
            if (interest != null) {
                break;
            }
        }

        float pref = 0;
        if (interest != null) {
            // range it from -1 to 1
            pref = convertScoreToMahoutValue(interest.getScore());
        }
        return pref;

    }

    @Override
    public void init() throws InconsistentDataException, TasteException {
        observations = getPersistence().getObservations();

        if (this.observations.size() == 0) {
            LOGGER.warn("Got no observations to learn from!");
        }

        Collection<Message> messages = new HashSet<Message>();

        for (ObservationKey key : observations.keySet()) {
            Message message = getPersistence().getMessageByGlobalId(key.getMessageGlobalId());
            if (message == null) {
                throw new InconsistentDataException("message cannot be null for globalId="
                        + key.getMessageGlobalId());
            }
            if (message.getId() == null) {
                throw new InconsistentDataException("message.id cannot be null for globalId="
                        + key.getMessageGlobalId() + " message=" + message);
            }
            messages.add(message);
        }
        this.messagesWithOberservations = Collections.unmodifiableCollection(messages);

        super.init();
    }

}