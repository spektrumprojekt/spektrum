package de.spektrumprojekt.i.collab;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
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
import de.spektrumprojekt.datamodel.message.UserMessageScore;
import de.spektrumprojekt.datamodel.observation.Interest;
import de.spektrumprojekt.datamodel.observation.Observation;
import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.i.term.similarity.TermVectorSimilarityComputer;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.simple.SimplePersistence;
import de.spektrumprojekt.persistence.simple.SimplePersistence.ObservationKey;

/**
 * Collaborative Ranker. Currently only working with a {@link SimplePersistence}
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public abstract class CollaborativeScoreComputer implements Computer {

    public static enum CollaborativeScoreComputerType {
        USER2MESSAGE,
        USER2TERM;

        public CollaborativeScoreComputer createComputer(
                Persistence persistence,
                ObservationType[] observationTypesToUseForDataModel,
                TermVectorSimilarityComputer termVectorSimilarityComputer,
                boolean useGenericRecommender) {
            CollaborativeScoreComputer collaborativeRankerComputer;
            switch (this) {
            case USER2MESSAGE:
                collaborativeRankerComputer = new UserToMessageCollaborativeScoreComputer(
                        persistence,
                        observationTypesToUseForDataModel,
                        useGenericRecommender);
                break;
            case USER2TERM:
                collaborativeRankerComputer = new UserToTermCollaborativeScoreComputer(
                        persistence,
                        observationTypesToUseForDataModel,
                        termVectorSimilarityComputer,
                        useGenericRecommender);
                break;
            default:
                throw new IllegalArgumentException(this + " is unhandled.");

            }
            return collaborativeRankerComputer;
        }
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(CollaborativeScoreComputer.class);

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

    private Collection<UserMessageScore> messageScores;

    private int preferenceCount;

    private int nonZeroRanks;

    private final boolean useGenericRecommender;

    private int positivePreferenceCount, negativePreferenceCount;

    private File preferencesDebugFile = new File("preferences.txt");

    private boolean outPreferencesToFile;

    private Recommender recommender;

    public final static ObservationType[] OT_ONLY_RATINGS = new ObservationType[] { ObservationType.RATING };

    public final static ObservationType[] OT_ONLY_MESSAGE = new ObservationType[] { ObservationType.MESSAGE };

    private final ObservationType[] observationTypesToUseForDataModel;

    private int nanScores = 0;
    private boolean emptyDataModel;

    public CollaborativeScoreComputer(Persistence persistence,
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

    private DataModel createDataModel() {
        DataModel dataModel = null;
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

                createUserPreference(userData, user, fw);

            }

            dataModel = new GenericDataModel(userData);
        } catch (IOException io) {
            throw new RuntimeException(io);
        } finally {
            IOUtils.closeQuietly(fw);
        }
        return dataModel;
    }

    protected abstract void createUserPreference(FastByIDMap<PreferenceArray> userData, User user,
            FileWriter fw) throws IOException;

    protected abstract float estimate(User user, Message message) throws TasteException;

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
                + " useGenericRecommender: " + this.useGenericRecommender
                + " observationTypesToUseForDataModel: "
                + Arrays.toString(this.observationTypesToUseForDataModel);
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

    public Collection<UserMessageScore> getMessageScores() {
        return messageScores;
    }

    public Collection<Message> getMessagesWithOberservations() {
        return messagesWithOberservations;
    }

    public int getNanScores() {
        return nanScores;
    }

    public int getNeg() {
        return negativePreferenceCount;
    }

    public int getNonZeroRanks() {
        return nonZeroRanks;
    }

    public Map<ObservationKey, Collection<Observation>> getObservations() {
        return observations;
    }

    public ObservationType[] getObservationTypesToUseForDataModel() {
        return observationTypesToUseForDataModel;
    }

    public SimplePersistence getPersistence() {
        return persistence;
    }

    public int getPos() {
        return positivePreferenceCount;
    }

    public int getPrefCount() {
        return preferenceCount;
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

    public Recommender getRecommender() {
        return recommender;
    }

    protected void incrementNegativePreferenceCount() {
        this.negativePreferenceCount++;
    }

    protected void incrementPositivePreferenceCount() {
        this.positivePreferenceCount++;
    }

    protected void incrementPreferenceCount() {
        this.preferenceCount++;
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

        this.dataModel = createDataModel();

        // dataModel = new FileDataModel(getDataFile());

        userSimilarity = new org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity(
                dataModel);
        userNeighborhood = new ThresholdUserNeighborhood(0, userSimilarity,
                dataModel);

        if (dataModel.getNumItems() == 0) {
            emptyDataModel = true;
        } else {
            if (useGenericRecommender) {
                recommender = new GenericUserBasedRecommender(
                        dataModel,
                        userNeighborhood,
                        userSimilarity);
            } else {
                recommender = new CachingRecommender(new SlopeOneRecommender(dataModel));
            }
        }
    }

    public boolean isOutPreferencesToFile() {
        return outPreferencesToFile;
    }

    @Override
    public void run() throws TasteException {
        run(null);
    }

    public void run(Collection<Message> messagesToRun) throws TasteException {

        messageScores = new HashSet<UserMessageScore>();

        if (emptyDataModel) {
            LOGGER.info("Empty datamodel. Skip run.");
            return;
        }

        Collection<Message> needEstimation = messagesToRun == null ? messagesWithOberservations
                : messagesToRun;
        LongPrimitiveIterator it = dataModel.getUserIDs();
        while (it.hasNext()) {
            Long userId = it.next();
            User user = this.persistence.getUserById(userId);

            for (Message message : needEstimation) {
                final float estimate = estimate(user, message);
                if (estimate != 0) {
                    nonZeroRanks++;
                }
                float rank = convertScoreFromMahoutValue(estimate, true);

                if (!Float.isNaN(rank)) {
                    UserMessageScore messageRank = new UserMessageScore(message.getGlobalId(),
                            user.getGlobalId());

                    messageRank.setScore(rank);

                    messageScores.add(messageRank);
                } else {
                    nanScores++;
                }

            }

        }

        this.persistence.storeMessageRanks(messageScores);
    }

    public String someStats() throws TasteException {
        String stats =
                "prefs: " + preferenceCount
                        + " pos prefs: " + positivePreferenceCount
                        + " neg prefs: " + negativePreferenceCount;

        if (dataModel != null) {
            stats += " dm.max: " + dataModel.getMaxPreference()
                    + " dm.min: " + dataModel.getMinPreference()
                    + " dm.items: " + dataModel.getNumItems()
                    + " dm.users: " + dataModel.getNumUsers();
        }
        return stats;

    }
}