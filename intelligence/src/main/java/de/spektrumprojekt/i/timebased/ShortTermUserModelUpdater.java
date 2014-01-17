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

import de.spektrumprojekt.commons.time.TimeProviderHolder;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.i.learner.contentbased.UserModelConfiguration;
import de.spektrumprojekt.i.learner.contentbased.UserModelEntryIntegrationStrategyType;
import de.spektrumprojekt.i.ranker.ScorerConfiguration;
import de.spektrumprojekt.i.timebased.config.EnergyCalculationConfiguration;
import de.spektrumprojekt.i.timebased.config.LongTermMemoryConfiguration;
import de.spektrumprojekt.i.timebased.config.PermanentLongTermInterestDetector;
import de.spektrumprojekt.i.timebased.config.ShortTermMemoryConfiguration;
import de.spektrumprojekt.persistence.Persistence;

public class ShortTermUserModelUpdater implements UserModelUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShortTermUserModelUpdater.class);

    private final ShortTermMemoryConfiguration shortTermMemoryConfiguration;

    private final Persistence persistence;

    private final Map<String, UserModelConfiguration> userModelTypes;

    private Date lastModelCalculationDate;

    private final ScorerConfiguration scorerConfiguration;

    private Date firstBinStartTime;

    private final BinAggregatedUserModelEntryDecorator entryDecorator;

    private final NutritionCalculationStrategy strategy;

    private final int aggregatedCount;

    private final List<String> modelsToTransferTermsFrom = new ArrayList<String>();

    private final List<LongTermInterestDetector> longTermInterestDetectors = new ArrayList<LongTermInterestDetector>();

    private final NutritionCalculationStrategy nutritionConverter = new AbsoluteNutritionCalculationStrategy();;

    private final int longTermCalculationPeriodInBins;

    private int calculatedLongTermPeriodsAgo;
    private final EnergyCalculationConfiguration energyCalculationConfiguration;

    public ShortTermUserModelUpdater(Persistence persistence, ScorerConfiguration configuration) {
        super();
        this.scorerConfiguration = configuration;
        this.persistence = persistence;
        this.userModelTypes = new HashMap<String, UserModelConfiguration>();
        for (Entry<String, UserModelConfiguration> entry : configuration
                .getUserModelConfigurations()
                .entrySet()) {
            if (needsToBeCalculated(entry.getValue())) {
                userModelTypes.put(entry.getKey(), entry.getValue());
            }
        }
        this.shortTermMemoryConfiguration = configuration.getShortTermMemoryConfiguration();
        this.energyCalculationConfiguration = shortTermMemoryConfiguration
                .getEnergyCalculationConfiguration();
        switch (shortTermMemoryConfiguration.getEnergyCalculationConfiguration().getStrategy()) {
        case RELATIVE:
            strategy = new RelativeNutritionCalculationStrategy(
                    new BinAggregatedUserModelEntryDecorator(shortTermMemoryConfiguration
                            .getEnergyCalculationConfiguration().getBinAggregationCount()));
            break;
        case ABSOLUTE:
        default:
            strategy = new AbsoluteNutritionCalculationStrategy();
            break;
        }
        aggregatedCount = shortTermMemoryConfiguration.getEnergyCalculationConfiguration()
                .getBinAggregationCount();
        entryDecorator = new BinAggregatedUserModelEntryDecorator(aggregatedCount);
        for (String userModel : configuration.getUserModelConfigurations().keySet()) {
            // no new terms are created in the user model, this indicates this model is a long term
            // model
            if (scorerConfiguration.isCreateUnknownTermsInUsermodel(userModel)) {
                modelsToTransferTermsFrom.add(userModel);
            }
        }
        LongTermMemoryConfiguration longTermMemoryConfiguration = configuration
                .getShortTermMemoryConfiguration().getLongTermMemoryConfiguration();
        longTermCalculationPeriodInBins = configuration.getShortTermMemoryConfiguration()
                .getLongTermMemoryConfiguration() == null ? -1 : configuration
                .getShortTermMemoryConfiguration().getLongTermMemoryConfiguration()
                .getLongTermCalculationPeriodInBins();
        // if in all user models the unknown terms are created no separated long term user model
        // exists
        if (!(modelsToTransferTermsFrom.size() == scorerConfiguration.getUserModelConfigurations()
                .size())) {
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

    private float calculateTermWeightByEnergy(UserModelEntry entry) {
        entryDecorator.setEntry(entry);
        float[] nutrition = strategy.getNutrition(entryDecorator, persistence);
        nutrition = addMissingZeros(nutrition, shortTermMemoryConfiguration.getPrecision(),
                aggregatedCount);// TODO try if can be removed
        int length = nutrition.length - 1;
        if (length < 0) {
            return -1f;
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
            energy += (Math.pow(weightedAverage(nutrition), 2) - Math.pow(historicalNutrition, 2))
                    / (length - histNutrIndex);
        }
        int g = energyCalculationConfiguration.getG();
        float k = energyCalculationConfiguration.getK();
        float d = energyCalculationConfiguration.getD();

        float weight = (float) (g / (1 + d * Math.pow(Math.E, -k * g * currentNutrition * energy)));
        return weight;
    }

    private boolean isInUserModel(UserModel userModel, Term term) {
        Set<Term> terms = new HashSet<Term>();
        terms.add(term);
        Map<Term, UserModelEntry> userModelEntries = persistence.getUserModelEntriesForTerms(
                userModel, terms);
        return userModelEntries.size() > 0;
    }

    private boolean itsTimeToCalculateLongTermInterests() {
        if (longTermCalculationPeriodInBins != -1
                && longTermCalculationPeriodInBins <= calculatedLongTermPeriodsAgo) {
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

    /**
     * only UserModelEntryIntegrationStrategy.TIMEBINNED user models with the attribute
     * isCalculateLater are calculated
     * 
     * @param userModelConfiguration
     * @return
     */
    private boolean needsToBeCalculated(UserModelConfiguration userModelConfiguration) {
        return userModelConfiguration.getUserModelEntryIntegrationStrategyType()
                .equals(UserModelEntryIntegrationStrategyType.TIMEBINNED)
                ? userModelConfiguration.isCalculateLater()
                : false;
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

    @Override
    public String toString() {
        return "ShortTermUserModelUpdater [shortTermMemoryConfiguration="
                + shortTermMemoryConfiguration + ", userModelTypes=" + userModelTypes
                + ", lastModelCalculationDate=" + lastModelCalculationDate + ", firstBinStartTime="
                + firstBinStartTime + ", entryDecorator=" + entryDecorator + ", strategy="
                + strategy + ", aggregatedCount=" + aggregatedCount
                + ", modelsToTransferTermsFrom=" + modelsToTransferTermsFrom
                + ", longTermInterestDetectors=" + longTermInterestDetectors
                + ", nutritionConverter=" + nutritionConverter
                + ", longTermCalculationPeriodInBins=" + longTermCalculationPeriodInBins
                + ", calculatedLongTermPeriodsAgo=" + calculatedLongTermPeriodsAgo
                + ", energyCalculationConfiguration=" + energyCalculationConfiguration + "]";
    }

    private void transferTermsToLongTermModelIfNecessary(UserModelEntry entry, String userGlobalId) {
        // for (UserModelEntry entry : entries) {
        UserModel LongTermUserModel = persistence.getOrCreateUserModelByUser(userGlobalId,
                UserModel.DEFAULT_USER_MODEL_TYPE);
        Term term = entry.getScoredTerm().getTerm();
        if (!isInUserModel(LongTermUserModel, term)) {
            entry = sort(entry);
            float[] nutrition = nutritionConverter.getNutrition(entry, persistence);
            checkLongTermInterest: for (LongTermInterestDetector detector : longTermInterestDetectors) {
                if (detector.isLongTermInterest(nutrition)) {
                    transferTermToLongTermUsermodel(LongTermUserModel, entry);
                    break checkLongTermInterest;
                }
            }
        }
        // }
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
        for (String userModelType : userModelTypes.keySet()) {
            Map<UserModel, Collection<UserModelEntry>> userModelsAndEntries = persistence
                    .getAllUserModelEntries(userModelType);
            for (UserModel userModel : userModelsAndEntries.keySet()) {

                LOGGER.debug("working on modeltype {}", userModel.getUserModelType());
                Collection<UserModelEntry> entries = userModelsAndEntries.get(userModel);
                for (UserModelEntry entry : entries) {
                    float weight = calculateTermWeightByEnergy(entry);
                    if (weight != -1) {
                        entry.getScoredTerm().setWeight(weight);
                    }
                    if (calculateLongTermInterests
                            && modelsToTransferTermsFrom.contains(userModelType)) {
                        transferTermsToLongTermModelIfNecessary(entry, userModel.getUser()
                                .getGlobalId());
                    }
                }
                persistence.storeOrUpdateUserModelEntries(userModel, entries);

            }
        }
        if (calculateLongTermInterests) {
            calculatedLongTermPeriodsAgo = 1;
        } else {
            calculatedLongTermPeriodsAgo++;
        }
        LOGGER.debug("Finished updating UserModels");
    }

    private float weightedAverage(float[] nutrition) {
        float result = 0;
        int currentBin = nutrition.length - 1;
        result = nutrition[currentBin];
        if (energyCalculationConfiguration.getNutritionHistLength() > 0) {
            int binsToUse = Math.min(currentBin,
                    energyCalculationConfiguration.getNutritionHistLength());
            for (int i = 0; i <= binsToUse; i++) {
                result += (float) Math.pow(2, -i - 1) * nutrition[currentBin - i];

            }
        }
        return result;
    }
}
