package de.spektrumprojekt.i.timebased;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.time.TimeProviderHolder;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.datamodel.user.UserModelEntryTimeBin;
import de.spektrumprojekt.i.learner.contentbased.UserModelConfiguration;
import de.spektrumprojekt.i.learner.time.TimeBinnedUserModelEntryIntegrationStrategy;
import de.spektrumprojekt.i.ranker.ScorerConfiguration;
import de.spektrumprojekt.i.timebased.config.LongTermMemoryConfiguration;
import de.spektrumprojekt.i.timebased.config.PermanentLongTermInterestDetector;
import de.spektrumprojekt.persistence.Persistence;

/**
 * This user model updater uses a time binned user model detects
 * 
 * @author Torsten
 * 
 */
public class SimpleLongTermUserModelUpdater implements UserModelUpdater {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SimpleLongTermUserModelUpdater.class);

    private final Persistence persistence;

    private final UserModelConfiguration shortTermUserModelConfig;
    private final UserModelConfiguration longTermUserModelConfig;

    private Date lastModelCalculationDate;

    private final ScorerConfiguration scorerConfiguration;
    private final LongTermMemoryConfiguration longTermMemoryConfiguration;

    private Date firstBinStartTime;
    private final List<LongTermInterestDetector> longTermInterestDetectors = new ArrayList<LongTermInterestDetector>();

    private int calculatedLongTermPeriodsAgo;

    private final TimeBinnedUserModelEntryIntegrationStrategy shortTermEntryIntegrationStrategy;

    public SimpleLongTermUserModelUpdater(Persistence persistence,
            ScorerConfiguration configuration,
            TimeBinnedUserModelEntryIntegrationStrategy shortTermEntryIntegrationStrategy) {
        if (shortTermEntryIntegrationStrategy == null) {
            throw new IllegalArgumentException("shortTermEntryIntegrationStrategy cannot be null.");
        }
        if (configuration == null) {
            throw new IllegalArgumentException("configuration cannot be null.");
        }
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (configuration.getShortTermMemoryConfiguration() == null) {
            throw new IllegalArgumentException("shortTermMemoryConfiguration cannot be null.");
        }
        if (configuration.getShortTermMemoryConfiguration()
                .getLongTermMemoryConfiguration() == null) {
            throw new IllegalArgumentException("longTermMemoryConfiguration cannot be null.");
        }
        this.scorerConfiguration = configuration;
        this.persistence = persistence;
        this.shortTermEntryIntegrationStrategy = shortTermEntryIntegrationStrategy;

        this.longTermMemoryConfiguration = configuration.getShortTermMemoryConfiguration()
                .getLongTermMemoryConfiguration();

        this.shortTermUserModelConfig = configuration.getUserModelConfigurations().get(
                UserModel.SHORT_TERM_USER_MODEL_TYPE);
        this.longTermUserModelConfig = configuration.getUserModelConfigurations().get(
                UserModel.DEFAULT_USER_MODEL_TYPE);

        if (shortTermUserModelConfig == null) {
            throw new IllegalArgumentException("shortTermUserModelConfig cannot be null.");
        }
        if (longTermUserModelConfig == null) {
            throw new IllegalArgumentException("longTermUserModelConfig cannot be null.");
        }

        longTermInterestDetectors.add(new PeriodicLongTermInterestDetector(
                longTermMemoryConfiguration.getPeriodicInterestScoreThreshold(),
                longTermMemoryConfiguration.getPeriodicInterestDistanceInBins(),
                longTermMemoryConfiguration.getPeriodicInterestOccuranceCount()));
        longTermInterestDetectors.add(new PermanentLongTermInterestDetector(
                longTermMemoryConfiguration.getPermanentInterestScoreThreshold(),
                longTermMemoryConfiguration.getPermanentInterestOccurenceMinLengthInBins(),
                longTermMemoryConfiguration.getPermanentInterestBinsFilledPercentage()));

    }

    /**
     * adds zeros for bins, in which this term did not occure
     * 
     * @param nutrition
     * @param precision
     * @param aggregatedCount
     * @return
     */
    private float[] addMissingZeros(float[] nutrition, long precision, int aggregatedCount) {
        int bincount = (int) ((lastModelCalculationDate.getTime() - firstBinStartTime.getTime()) / precision);
        bincount = bincount / aggregatedCount;
        float[] result = new float[bincount];
        for (int i = 0; i < bincount; i++) {
            if (i + nutrition.length < bincount) {
                result[i] = 0;
            } else {
                result[i] = nutrition[i - bincount + nutrition.length];
            }
        }
        return result;
    }

    private Map<Term, UserModelEntry> cleanUpUserModelEntriesOfUserModel(UserModel userModel,
            Collection<UserModelEntry> allEntriesInUserModel) {
        Map<Term, UserModelEntry> oldEntries = UserModelEntry
                .createTermToEntryMap(allEntriesInUserModel);
        Map<Term, UserModelEntry> entries = shortTermEntryIntegrationStrategy
                .cleanUpEntries(oldEntries);

        for (Entry<Term, UserModelEntry> oldEntry : oldEntries.entrySet()) {
            if (!entries.containsKey(oldEntry.getKey())) {
                this.persistence.removeUserModelEntry(userModel, oldEntry.getValue());
            }
        }
        this.persistence.storeOrUpdateUserModelEntries(userModel, entries.values());
        return entries;
    }

    public float[] getEntryWeights(UserModelEntry entry) {
        List<UserModelEntryTimeBin> timeBinEntries = new ArrayList<UserModelEntryTimeBin>(
                entry.getTimeBinEntries());
        UserModelEntryTimeBin.sort(timeBinEntries);

        int maxIndex = this.shortTermEntryIntegrationStrategy
                .determineBinIndex(TimeProviderHolder.DEFAULT.getCurrentTime());
        final float[] result = new float[maxIndex + 1];
        for (UserModelEntryTimeBin timeBin : timeBinEntries) {
            int index = this.shortTermEntryIntegrationStrategy.determineBinIndex(timeBin
                    .getTimeBinStart());
            if (timeBin.getScoreSum() > 0) {
                result[index] = timeBin.getScoreCount() / (float) timeBin.getScoreSum();
            } else {
                LOGGER.warn("sum of time bin entry is 0, should be deleted before! entry=" + entry);
            }
        }

        int i = 0;
        for (UserModelEntryTimeBin bin : timeBinEntries) {
            if (bin.getScoreSum() > 0) {
                result[i] = bin.getScoreCount() / (float) bin.getScoreSum();
            } else {
                LOGGER.warn("sum of time bin entry is 0, should be deleted before! entry=" + entry);
            }
            i++;
        }
        return result;
    }

    private boolean isInUserModel(UserModel userModel, Term term) {
        Set<Term> terms = new HashSet<Term>();
        terms.add(term);
        Map<Term, UserModelEntry> userModelEntries = persistence.getUserModelEntriesForTerms(
                userModel, terms);
        return userModelEntries.size() > 0;
    }

    private boolean itsTimeToCalculateLongTermInterests() {
        if (longTermMemoryConfiguration.getLongTermCalculationPeriodInBins() != -1
                && longTermMemoryConfiguration.getLongTermCalculationPeriodInBins() <= calculatedLongTermPeriodsAgo) {
            return true;
        }
        return false;
    }

    private boolean itsTimeToCalculateModels() {
        Date date = new Date(TimeProviderHolder.DEFAULT.getCurrentTime());
        if (firstBinStartTime == null) {
            firstBinStartTime = date;
        }
        if (!(date.getTime() - 2
                * scorerConfiguration.getShortTermMemoryConfiguration().getPrecision() > firstBinStartTime
                    .getTime())) {
            return false;
        }
        if (lastModelCalculationDate == null) {
            lastModelCalculationDate = date;
            return true;
        }
        if (date.getTime() - scorerConfiguration.getShortTermMemoryConfiguration().getPrecision() >= lastModelCalculationDate
                .getTime()) {
            lastModelCalculationDate = date;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "SimpleLongTermUserModelUpdater [shortTermUserModelConfig="
                + shortTermUserModelConfig + ", longTermUserModelConfig=" + longTermUserModelConfig
                + ", lastModelCalculationDate=" + lastModelCalculationDate
                + ", longTermMemoryConfiguration=" + longTermMemoryConfiguration
                + ", firstBinStartTime=" + firstBinStartTime + ", longTermInterestDetectors="
                + longTermInterestDetectors + ", calculatedLongTermPeriodsAgo="
                + calculatedLongTermPeriodsAgo + ", shortTermEntryIntegrationStrategy="
                + shortTermEntryIntegrationStrategy + "]";
    }

    private void transferTermsToLongTermModelIfNecessary(UserModelEntry entry, String userGlobalId) {

        UserModel longTermUserModel = persistence.getOrCreateUserModelByUser(userGlobalId,
                UserModel.DEFAULT_USER_MODEL_TYPE);
        Term term = entry.getScoredTerm().getTerm();

        if (!isInUserModel(longTermUserModel, term)) {

            float[] weights = getEntryWeights(entry);

            checkLongTermInterest: for (LongTermInterestDetector detector : longTermInterestDetectors) {
                if (detector.isLongTermInterest(weights)) {
                    transferTermToLongTermUsermodel(longTermUserModel, entry);
                    break checkLongTermInterest;
                }
            }
        } else {
            // use a standard (plain) user model integration strategy that updates the LT weight as
            // usual but does not create a new one
        }

    }

    private void transferTermToLongTermUsermodel(UserModel longTermUserModel, UserModelEntry entry) {
        UserModelEntry longTermUserEntry = new UserModelEntry(longTermUserModel,
                entry.getScoredTerm());
        Collection<UserModelEntry> modelEntries = new HashSet<UserModelEntry>();
        modelEntries.add(longTermUserEntry);
        persistence.storeOrUpdateUserModelEntries(longTermUserModel, modelEntries);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.spektrumprojekt.i.timebased.UserModelUpdater#updateUserModels(java.util.Date)
     */
    @Override
    public void updateUserModels() {
        if (!itsTimeToCalculateModels()) {
            return;
        }
        boolean calculateLongTermInterests = itsTimeToCalculateLongTermInterests();

        LOGGER.debug("Starting to update UserModels");
        Map<UserModel, Collection<UserModelEntry>> userModelsAndEntries = persistence
                .getAllUserModelEntries(UserModel.SHORT_TERM_USER_MODEL_TYPE);
        for (UserModel userModel : userModelsAndEntries.keySet()) {

            LOGGER.debug("working on modeltype {}", userModel.getUserModelType());

            Map<Term, UserModelEntry> entries = cleanUpUserModelEntriesOfUserModel(userModel,
                    userModelsAndEntries.get(userModel));

            if (calculateLongTermInterests) {
                for (UserModelEntry entry : entries.values()) {

                    transferTermsToLongTermModelIfNecessary(
                            entry,
                            userModel.getUser().getGlobalId());

                }
            }
        }
        if (calculateLongTermInterests) {
            calculatedLongTermPeriodsAgo = 1;
        } else {
            calculatedLongTermPeriodsAgo++;
        }
        LOGGER.debug("Finished updating UserModels");
    }
}
