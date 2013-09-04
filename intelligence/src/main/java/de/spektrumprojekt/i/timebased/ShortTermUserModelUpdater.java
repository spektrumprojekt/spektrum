package de.spektrumprojekt.i.timebased;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.i.ranker.RankerConfiguration;
import de.spektrumprojekt.i.ranker.UserModelConfiguration;
import de.spektrumprojekt.i.timebased.config.LongTermMemoryConfiguration;
import de.spektrumprojekt.i.timebased.config.PermanentLongTermInterestDetector;
import de.spektrumprojekt.i.timebased.config.ShortTermMemoryConfiguration;
import de.spektrumprojekt.persistence.Persistence;

public class ShortTermUserModelUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShortTermUserModelUpdater.class);

    private final ShortTermMemoryConfiguration shortTermMemoryConfiguration;

    private final Persistence persistence;

    private final Map<String, UserModelConfiguration> userModelTypes;

    private Date lastModelCalculationDate;

    private final RankerConfiguration rankerConfiguration;

    private Date firstBinStartTime;

    private final double d;

    private final double k;

    private final double G;

    private final int nutritionHistoryLength;

    private final BinAggregatedUserModelEntryDecorator entryDecorator;

    NutritionCalculationStrategy strategy;

    private final int aggregatedCount;

    private final boolean transferTermsToLongTermModel;

    private final List<LongTermInterestDetector> longTermInterestDetectors = new ArrayList<LongTermInterestDetector>();;

    NutritionCalculationStrategy nutritionConverter = new AbsoluteNutritionCalculationStrategy();;

    public ShortTermUserModelUpdater(Persistence persistence, RankerConfiguration configuration) {
        super();
        this.rankerConfiguration = configuration;
        this.persistence = persistence;
        this.userModelTypes = new HashMap<String, UserModelConfiguration>();
        for (Entry<String, UserModelConfiguration> entry : configuration.getUserModelTypes()
                .entrySet()) {
            if (needsToBeCalculated(entry.getValue())) {
                userModelTypes.put(entry.getKey(), entry.getValue());
            }
        }
        this.shortTermMemoryConfiguration = configuration.getShortTermMemoryConfiguration();
        d = shortTermMemoryConfiguration.getEnergyCalculationConfiguration().getD();
        k = shortTermMemoryConfiguration.getEnergyCalculationConfiguration().getK();
        G = shortTermMemoryConfiguration.getEnergyCalculationConfiguration().getG();
        nutritionHistoryLength = shortTermMemoryConfiguration.getEnergyCalculationConfiguration()
                .getNutritionHistLength();
        switch (shortTermMemoryConfiguration.getEnergyCalculationConfiguration().getStrategy()) {
        case RELATIVE:
            strategy = new RelativeNutritionCalculationStrategy(
                    new BinAggregatedUserModelEntryDecorator(shortTermMemoryConfiguration
                            .getEnergyCalculationConfiguration().getBinAggregationCount()));
            break;
        case ABSOLUTE:
            strategy = new AbsoluteNutritionCalculationStrategy();
            break;
        }
        aggregatedCount = shortTermMemoryConfiguration.getEnergyCalculationConfiguration()
                .getBinAggregationCount();
        entryDecorator = new BinAggregatedUserModelEntryDecorator(aggregatedCount);
        transferTermsToLongTermModel = !rankerConfiguration.isCreateUnknownTermsInUsermodel();
        LongTermMemoryConfiguration longTermMemoryConfiguration = configuration
                .getShortTermMemoryConfiguration().getLongTermMemoryConfiguration();
        if (transferTermsToLongTermModel) {
            longTermInterestDetectors.add(new PeriodicLongTermInterestDetector(
                    longTermMemoryConfiguration.getPeriodicInterestScoreThreshold(),
                    longTermMemoryConfiguration.getPeriodicInterestDistanceInBins(),
                    longTermMemoryConfiguration.getPeriodicInterestOccuranceCount()));
            longTermInterestDetectors.add(new PermanentLongTermInterestDetector(
                    longTermMemoryConfiguration.getPermanentInterestScoreThreshold(),
                    longTermMemoryConfiguration.getPermanentInterestOccurenceMinLengthInBins(),
                    longTermMemoryConfiguration.getPermanentInterestBinsFilledPercentage()));
        }
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

    private boolean isInUserModel(UserModel userModel, Term term) {
        Set<Term> terms = new HashSet<Term>();
        terms.add(term);
        Map<Term, UserModelEntry> userModelEntries = persistence.getUserModelEntriesForTerms(
                userModel, terms);
        return userModelEntries.size() > 0;
    }

    public boolean itsTimeToCalculateModels(Date date) {
        if (firstBinStartTime == null) {
            firstBinStartTime = date;
        }
        if (!(date.getTime() - 2
                * rankerConfiguration.getShortTermMemoryConfiguration().getPrecision() > firstBinStartTime
                    .getTime())) {
            return false;
        }
        if (lastModelCalculationDate == null) {
            lastModelCalculationDate = date;
            return true;
        }
        if (date.getTime() - rankerConfiguration.getShortTermMemoryConfiguration().getPrecision() >= lastModelCalculationDate
                .getTime()) {
            lastModelCalculationDate = date;
            return true;
        } else {
            return false;
        }
    }

    /**
     * only UserModelEntryIntegrationStrategy.TIMEBINNED user models with the attribute
     * isCalculateLater are calculated
     * 
     * @param userModelConfiguration
     * @return
     */
    private boolean needsToBeCalculated(UserModelConfiguration userModelConfiguration) {
        return (userModelConfiguration.getUserModelEntryIntegrationStrategy()
                .equals(UserModelConfiguration.UserModelEntryIntegrationStrategy.TIMEBINNED)) ? userModelConfiguration
                .isCalculateLater() : false;
    }

    /**
     * sorts by adding a decorator
     * 
     * @param entry
     * @return
     */
    private UserModelEntry sort(UserModelEntry entry) {
        BinAggregatedUserModelEntryDecorator entryDecorator = new BinAggregatedUserModelEntryDecorator(
                1);
        entryDecorator.setEntry(entry);
        return entryDecorator;
    }

    private void transferTermsToLongTermModel(Collection<UserModelEntry> entries,
            String userGlobalId) {
        for (UserModelEntry entry : entries) {
            UserModel LongTermUserModel = persistence.getOrCreateUserModelByUser(userGlobalId,
                    UserModel.DEFAULT_USER_MODEL_TYPE);
            Term term = entry.getScoredTerm().getTerm();
            if (!isInUserModel(LongTermUserModel, term)) {
                entry = sort(entry);
                float[] nutrition = nutritionConverter.getNutrition(entry, persistence);
                nutrition = addMissingZeros(nutrition, shortTermMemoryConfiguration.getPrecision(),
                        1);
                checkLongTermInterest: for (LongTermInterestDetector detector : longTermInterestDetectors) {
                    if (detector.isLongTermInterest(nutrition)) {
                        transferTermToLongTermUsermodel(LongTermUserModel, entry);
                        break checkLongTermInterest;
                    }
                }
            }
        }
    }

    private void transferTermToLongTermUsermodel(UserModel longTermUserModel, UserModelEntry entry) {
        UserModelEntry longTermUserEntry = new UserModelEntry(longTermUserModel,
                entry.getScoredTerm());
        Collection<UserModelEntry> modelEntries = new HashSet<UserModelEntry>();
        modelEntries.add(longTermUserEntry);
        persistence.storeOrUpdateUserModelEntries(longTermUserModel, modelEntries);
    }

    public void updateUserModels() {
        LOGGER.debug("Starting to update UserModels");
        for (String userModelType : userModelTypes.keySet()) {
            Map<UserModel, Collection<UserModelEntry>> userModelsAndEntries = persistence
                    .getAllUserModelEntries(userModelType);
            for (UserModel userModel : userModelsAndEntries.keySet()) {
                LOGGER.debug("working on modeltype {}", userModel.getUserModelType());
                Collection<UserModelEntry> entries = userModelsAndEntries.get(userModel);
                calculateEntry: for (UserModelEntry entry : entries) {
                    float weight = 0;
                    entryDecorator.setEntry(entry);
                    float[] nutrition = strategy.getNutrition(entryDecorator, persistence);
                    nutrition = addMissingZeros(nutrition,
                            shortTermMemoryConfiguration.getPrecision(), aggregatedCount);
                    int length = nutrition.length - 1;
                    if (length < 0) {
                        continue calculateEntry;
                    }
                    float currentNutrition = nutrition[length];
                    float energy = 0;
                    for (int histNutrIndex = length
                            - shortTermMemoryConfiguration.getEnergyCalculationConfiguration()
                                    .getEnergyHistoryLength(); histNutrIndex < length; histNutrIndex++) {
                        float historicalNutrition;
                        if (histNutrIndex < 0) {
                            historicalNutrition = 0;
                        } else {
                            historicalNutrition = nutrition[histNutrIndex];
                        }
                        energy += (Math.pow(weightedAverage(nutrition, nutritionHistoryLength), 2) - Math
                                .pow(historicalNutrition, 2)) / (length - histNutrIndex);
                    }
                    weight = (float) (G / (1 + d
                            * Math.pow(Math.E, -k * G * currentNutrition * energy)));
                    entry.getScoredTerm().setWeight(weight);
                }
                persistence.storeOrUpdateUserModelEntries(userModel, entries);
                if (transferTermsToLongTermModel) {
                    transferTermsToLongTermModel(entries, userModel.getUser().getGlobalId());
                }
            }
        }
        LOGGER.debug("Finished updating UserModels");
    }

    private float weightedAverage(float[] nutrition, int nutritionHistoryLength2) {
        float result = 0;
        int currentBin = nutrition.length - 1;
        result = nutrition[currentBin];
        if (nutritionHistoryLength > 0) {
            int binsToUse = Math.min(currentBin, nutritionHistoryLength);
            for (int i = 0; i <= binsToUse; i++) {
                result += (float) Math.pow(2, -i - 1) * nutrition[currentBin - i];

            }
        }
        return result;
    }
}
