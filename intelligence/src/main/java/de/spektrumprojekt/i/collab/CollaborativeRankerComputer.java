package de.spektrumprojekt.i.collab;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.computer.Computer;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageRank;
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
public class CollaborativeRankerComputer implements Computer {

    public static float convertScoreFromMahoutValue(float score, boolean minmax) {
        float val = (score + 1) / 2f;
        if (minmax) {
            val = Math.max(Math.min(1f, val), 0f);
        }
        return val;
    }

    public static float convertScoreToMahoutValue(float score) {
        return score * 2f - 1f;
    }

    private final SimplePersistence persistence;
    private Map<ObservationKey, Collection<Observation>> observations;

    private Collection<User> users;

    private Collection<Message> messagesWithOberservations;

    private DataModel dataModel;

    private UserSimilarity userSimilarity;

    private UserNeighborhood userNeighborhood;

    private Collection<MessageRank> messageRanks;

    private final static Logger LOGGER = LoggerFactory.getLogger(CollaborativeRankerComputer.class);

    private int prefCount;

    private int nonZeroRanks;

    private final boolean useGenericRecommender;

    private int pos, neg;

    private File preferencesDebugFile = new File("preferences.txt");

    private boolean outPreferencesToFile;

    private Recommender recommender;

    public final static ObservationType[] OT_ONLY_RATINGS = new ObservationType[] { ObservationType.RATING };
    public final static ObservationType[] OT_ONLY_MESSAGE = new ObservationType[] { ObservationType.MESSAGE };

    private final ObservationType[] observationTypesToUseForDataModel;

    private int nanRanks = 0;

    public CollaborativeRankerComputer(Persistence persistence,
            ObservationType[] observationTypesToUseForDataModel, boolean useGenericRecommender) {
        if (!(persistence instanceof SimplePersistence)) {
            throw new IllegalArgumentException("Can only handle SimplePersistence at the moment.");
        }
        if (observationTypesToUseForDataModel == null
                || observationTypesToUseForDataModel.length == 0) {
            throw new IllegalArgumentException(
                    "observationTypesToUseForDataModel cannot be null or empty.");
        }
        this.persistence = (SimplePersistence) persistence;
        this.observationTypesToUseForDataModel = observationTypesToUseForDataModel;
        this.useGenericRecommender = useGenericRecommender;
    }

    private void createDataModel() {

        FileWriter fw = null;
        try {
            if (outPreferencesToFile) {
                fw = new FileWriter(preferencesDebugFile);
            }

            FastByIDMap<PreferenceArray> userData = new FastByIDMap<PreferenceArray>(users.size());
            for (User user : users) {
                if (user.getId() == null) {
                    throw new IllegalStateException("userId cannot be null: " + user);
                }

                GenericUserPreferenceArray genericUserPreferenceArray = new GenericUserPreferenceArray(
                        messagesWithOberservations.size());

                int index = 0;

                for (Message message : messagesWithOberservations) {
                    boolean add = false;
                    float pref = getPreferenceValue(observations, user.getGlobalId(),
                            message.getGlobalId());

                    if (pref != 0) {
                        add = true;
                        prefCount++;
                    }
                    if (pref == 1) {
                        pos++;
                    }
                    if (pref == -1) {
                        neg++;
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

                        if (outPreferencesToFile) {
                            fw.write(index + " " + message.getGlobalId() + " " + user.getId() + " "
                                    + pref
                                    + "\n");
                        }
                    }
                }

                if (index > 0) {
                    userData.put(user.getId(), genericUserPreferenceArray);
                }

            }

            dataModel = new GenericDataModel(userData);
        } catch (IOException io) {
            throw new RuntimeException(io);
        } finally {
            IOUtils.closeQuietly(fw);
        }

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
        return this.getClass().getSimpleName()
                + " useGenericRecommender: " + this.useGenericRecommender;
    }

    public DataModel getDataModel() {
        return dataModel;
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

    public Collection<MessageRank> getMessageRanks() {
        return messageRanks;
    }

    public int getNanRanks() {
        return nanRanks;
    }

    public int getNeg() {
        return neg;
    }

    public int getNonZeroRanks() {
        return nonZeroRanks;
    }

    public int getPos() {
        return pos;
    }

    public int getPrefCount() {
        return prefCount;
    }

    private float getPreferenceValue(
            Map<ObservationKey, Collection<Observation>> allObservations, String userId,
            String messageGlobalId) {

        Interest interest = null;
        for (ObservationType observationType : observationTypesToUseForDataModel) {

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

    public Recommender getRecommender() {
        return recommender;
    }

    public void init() throws InconsistentDataException, TasteException {
        observations = persistence.getObservations();
        users = persistence.getAllUsers();

        if (this.observations.size() == 0) {
            LOGGER.warn("Got no observations to learn from!");
        }

        Collection<Message> messages = new HashSet<Message>();

        for (ObservationKey key : observations.keySet()) {
            Message message = persistence.getMessageByGlobalId(key.getMessageGlobalId());
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

        createDataModel();

        // dataModel = new FileDataModel(getDataFile());

        userSimilarity = new org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity(
                dataModel);
        userNeighborhood = new ThresholdUserNeighborhood(0, userSimilarity,
                dataModel);

        if (useGenericRecommender) {
            recommender = new GenericUserBasedRecommender(
                    dataModel,
                    userNeighborhood,
                    userSimilarity);
        } else {
            recommender = new CachingRecommender(new SlopeOneRecommender(dataModel));
        }

    }

    @Override
    public void run() throws TasteException {
        run(null);
    }

    public void run(Collection<Message> messagesToRun) throws TasteException {

        messageRanks = new HashSet<MessageRank>();

        Collection<Message> needEstimation = messagesToRun == null ? messagesWithOberservations
                : messagesToRun;
        LongPrimitiveIterator it = dataModel.getUserIDs();
        while (it.hasNext()) {
            Long userId = it.next();
            User user = this.persistence.getUserById(userId);

            for (Message message : needEstimation) {
                final float estimate = recommender.estimatePreference(userId,
                        message.getId());
                if (estimate != 0) {
                    nonZeroRanks++;
                }
                float rank = convertScoreFromMahoutValue(estimate, true);

                if (!Float.isNaN(rank)) {
                    MessageRank messageRank = new MessageRank(message.getGlobalId(),
                            user.getGlobalId());

                    messageRank.setRank(rank);

                    messageRanks.add(messageRank);
                } else {
                    nanRanks++;
                }

            }

        }

        this.persistence.storeMessageRanks(messageRanks);
    }

    public String someStats() throws TasteException {
        String stats =
                "prefs: " + prefCount
                        + " pos prefs: " + pos
                        + " neg prefs: " + neg;

        if (dataModel != null) {
            stats += " dm.max: " + dataModel.getMaxPreference()
                    + " dm.min: " + dataModel.getMinPreference()
                    + " dm.items: " + dataModel.getNumItems()
                    + " dm.users: " + dataModel.getNumUsers();
        }
        return stats;

    }
}