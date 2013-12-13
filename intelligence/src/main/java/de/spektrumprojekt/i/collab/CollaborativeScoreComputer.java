package de.spektrumprojekt.i.collab;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.io.IOUtils;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.common.Weighting;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.slopeone.MemoryDiffStorage;
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
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.i.informationextraction.InformationExtractionCommand;
import de.spektrumprojekt.i.ranker.CollaborativeConfiguration;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.simple.SimplePersistence;

/**
 * Collaborative Ranker. Currently only working with a {@link SimplePersistence}
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public abstract class CollaborativeScoreComputer implements Computer {

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

    private DataModel dataModel;

    private UserSimilarity userSimilarity;

    private UserNeighborhood userNeighborhood;

    private Collection<UserMessageScore> messageScores;

    private int preferenceCount;

    private int nonZeroRanks;

    private final CollaborativeConfiguration collaborativeConfiguration;

    private int positivePreferenceCount, negativePreferenceCount;

    private Recommender recommender;

    private int nanScores = 0;

    private boolean emptyDataModel;

    private Collection<User> users;

    public CollaborativeScoreComputer(Persistence persistence,
            CollaborativeConfiguration collaborativeConfiguration) {
        if (!(persistence instanceof SimplePersistence)) {
            throw new IllegalArgumentException("Can only handle SimplePersistence at the moment.");
        }
        if (collaborativeConfiguration == null) {
            throw new IllegalArgumentException("collaborativeConfiguration cannot be null");
        }
        if (collaborativeConfiguration.getCollaborativeScoreComputerType() == null) {
            throw new IllegalArgumentException(
                    "collaborativeConfiguration.collaborativeScoreComputerType cannot be null");
        }
        this.persistence = (SimplePersistence) persistence;
        this.collaborativeConfiguration = collaborativeConfiguration;
    }

    private DataModel createDataModel() {
        DataModel dataModel = null;
        FileWriter fw = null;
        try {
            if (collaborativeConfiguration.isOutPreferencesToFile()) {
                fw = new FileWriter(collaborativeConfiguration.getPreferencesDebugFile());
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

    protected Collection<User> getAllUsers() {
        if (this.users == null) {
            users = getPersistence().getAllUsers();
            if (users == null) {
                users = Collections.emptySet();
            }
        }

        return users;
    }

    public CollaborativeConfiguration getCollaborativeConfiguration() {
        return collaborativeConfiguration;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName()
                + " " + this.toString();
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    protected Collection<Message> getMessageForEstimation() {
        return persistence.getMessages();
    }

    public Collection<UserMessageScore> getMessageScores() {
        return messageScores;
    }

    public int getNanScores() {
        return nanScores;
    }

    public int getNeg() {
        return negativePreferenceCount;
    }

    public int getNegativePreferenceCount() {
        return negativePreferenceCount;
    }

    public int getNonZeroRanks() {
        return nonZeroRanks;
    }

    public SimplePersistence getPersistence() {
        return persistence;
    }

    public int getPositivePreferenceCount() {
        return positivePreferenceCount;
    }

    public int getPreferenceCount() {
        return preferenceCount;
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
        getAllUsers();

        this.dataModel = createDataModel();

        // dataModel = new FileDataModel(getDataFile());

        userSimilarity = new org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity(
                dataModel);
        userNeighborhood = new ThresholdUserNeighborhood(0, userSimilarity,
                dataModel);

        if (dataModel.getNumItems() == 0) {
            emptyDataModel = true;
        } else {
            if (collaborativeConfiguration.isUseGenericRecommender()) {
                recommender = new CachingRecommender(new GenericUserBasedRecommender(
                        dataModel,
                        userNeighborhood,
                        userSimilarity));
            } else {

                Weighting weighting = this.collaborativeConfiguration.isSlopeOneUseWeighted() ? Weighting.WEIGHTED
                        : Weighting.UNWEIGHTED;
                Weighting stdDevWeighting = this.collaborativeConfiguration
                        .isSlopeOneUseStdDevWeighted() ? Weighting.WEIGHTED : Weighting.UNWEIGHTED;

                recommender = new CachingRecommender(new SlopeOneRecommender(
                        dataModel, weighting, stdDevWeighting, new MemoryDiffStorage(
                                dataModel,
                                stdDevWeighting,
                                this.collaborativeConfiguration.getSlopeOneMaxEntries())));
            }
        }
    }

    @Override
    public void run() throws TasteException {
        run(getMessageForEstimation());
    }

    public void run(Collection<Message> messagesToRun) throws TasteException {

        messageScores = new HashSet<UserMessageScore>();

        if (emptyDataModel) {
            LOGGER.info("Empty datamodel. Skip run.");
            return;
        }
        if (messagesToRun == null) {
            throw new IllegalArgumentException("messagesToRun cannot be null.");
        }

        LongPrimitiveIterator it = dataModel.getUserIDs();
        while (it.hasNext()) {
            Long userId = it.next();
            User user = this.persistence.getUserById(userId);

            for (Message message : messagesToRun) {
                if (!InformationExtractionCommand.isInformationExtractionExecuted(message)) {
                    throw new IllegalStateException("IE should be run here. ");
                }
                final float estimate = estimate(user, message);
                if (estimate != 0) {
                    nonZeroRanks++;
                }
                float rank = convertScoreFromMahoutValue(estimate, true);

                if (!Float.isNaN(rank)) {
                    UserMessageScore messageScore = this.persistence.getMessageRank(
                            user.getGlobalId(), message.getGlobalId());
                    if (messageScore == null) {
                        throw new IllegalStateException(
                                "This is actually a rescore, so the message score should be computed somehow including all the features.");
                    }

                    messageScore.setScore(rank);

                    this.persistence.updateMessageRank(messageScore);
                    messageScores.add(messageScore);
                } else {
                    nanScores++;
                }

            }

        }

    }

    protected void setMessageScores(Collection<UserMessageScore> messageScores) {
        this.messageScores = messageScores;
    }

    protected void setNanScores(int nanScores) {
        this.nanScores = nanScores;
    }

    protected void setNegativePreferenceCount(int negativePreferenceCount) {
        this.negativePreferenceCount = negativePreferenceCount;
    }

    protected void setNonZeroRanks(int nonZeroRanks) {
        this.nonZeroRanks = nonZeroRanks;
    }

    protected void setPositivePreferenceCount(int positivePreferenceCount) {
        this.positivePreferenceCount = positivePreferenceCount;
    }

    protected void setPreferenceCount(int preferenceCount) {
        this.preferenceCount = preferenceCount;
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

    @Override
    public String toString() {
        return "CollaborativeScoreComputer [dataModel=" + dataModel + ", userSimilarity="
                + userSimilarity + ", userNeighborhood=" + userNeighborhood + ", messageScores="
                + messageScores + ", preferenceCount=" + preferenceCount + ", nonZeroRanks="
                + nonZeroRanks + ", collaborativeConfiguration=" + collaborativeConfiguration
                + ", positivePreferenceCount=" + positivePreferenceCount
                + ", negativePreferenceCount=" + negativePreferenceCount + ", recommender="
                + recommender + ", nanScores=" + nanScores + ", emptyDataModel=" + emptyDataModel
                + ", users=" + users + "]";
    }
}