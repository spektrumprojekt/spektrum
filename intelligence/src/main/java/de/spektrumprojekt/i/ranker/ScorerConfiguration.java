package de.spektrumprojekt.i.ranker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.datamodel.observation.Interest;
import de.spektrumprojekt.i.informationextraction.InformationExtractionCommand;
import de.spektrumprojekt.i.informationextraction.InformationExtractionConfiguration;
import de.spektrumprojekt.i.learner.adaptation.UserModelAdapterConfiguration;
import de.spektrumprojekt.i.learner.contentbased.UserModelConfiguration;
import de.spektrumprojekt.i.ranker.feature.Feature;
import de.spektrumprojekt.i.ranker.feature.FixWeightFeatureAggregator;
import de.spektrumprojekt.i.term.TermVectorSimilarityStrategy;
import de.spektrumprojekt.i.term.TermWeightStrategy;
import de.spektrumprojekt.i.timebased.config.ShortTermMemoryConfiguration;

public class ScorerConfiguration implements ConfigurationDescriptable, Cloneable {

    private Set<ScorerConfigurationFlag> flags = new HashSet<ScorerConfigurationFlag>();

    // for adaption the message rank (not the user model): the minimum user similarity that must be
    // fullfilled to be eglible to take the score of
    private float minUserSimilarity = 0.5f;

    // for adaption the message rank (not the user model): the minimum score the cmf feature of a
    // similar user must have such that it will be used.
    private float minContentMessageScore = 0.5f;

    // for adaption the message rank (not the user model): the message rank threshold determines for
    // which users the adaption will take place.
    private float messageRankThreshold = 0.5f;

    private float nonParticipationFactor = 1f;
    private int minimumCleanTextLengthForInvokingLearner;

    private TermWeightStrategy termWeightStrategy;

    private TermVectorSimilarityStrategy termVectorSimilarityStrategy;
    private boolean immutable;

    private float interestTermTreshold = 0.75f;

    private boolean treatMissingUserModelEntriesAsZero;

    private String termUniquenessLogfile;

    private Map<String, UserModelConfiguration> userModelTypes = new HashMap<String, UserModelConfiguration>();// UserModel.DEFAULT_USER_MODEL_TYPE;

    private final InformationExtractionConfiguration informationExtractionConfiguration;

    private final InformationExtractionCommand<MessageFeatureContext> informationExtractionCommand;

    private final UserModelAdapterConfiguration userModelAdapterConfiguration = new UserModelAdapterConfiguration();

    private boolean mixMemoriesForRating = false;

    private ShortTermMemoryConfiguration shortTermMemoryConfiguration;

    private final List<String> modelsToNotCreateUnknownTermsIn = new ArrayList<String>();

    private boolean useFixedDefaultFeatureWeights = true;
    private boolean useFixedDefaultLearningFeatureWeights = true;

    private Map<Feature, Float> featureWeights;

    private Float scoreToLearnThreshold;
    private Map<Feature, Float> learningFeatureWeights;
    private Map<Feature, Float> learningFeatureTresholds;

    public ScorerConfiguration(TermWeightStrategy strategy, TermVectorSimilarityStrategy aggregation) {
        this(strategy, aggregation, null, null, (ScorerConfigurationFlag[]) null);
    }

    private ScorerConfiguration(TermWeightStrategy termWeightStrategy,
            TermVectorSimilarityStrategy termWeightAggregation,
            InformationExtractionCommand<MessageFeatureContext> informationExtractionCommand,
            InformationExtractionConfiguration informationExtractionConfiguration,
            ScorerConfigurationFlag... flags) {
        if (termWeightStrategy == null) {
            throw new IllegalStateException("termWeightStrategy cannot be null.");
        }
        if (termWeightAggregation == null) {
            throw new IllegalStateException("termWeightAggregation cannot be null.");
        }
        this.termWeightStrategy = termWeightStrategy;
        this.termVectorSimilarityStrategy = termWeightAggregation;
        if (flags != null) {
            this.flags.addAll(Arrays.asList(flags));
        }
        if (informationExtractionConfiguration == null) {
            informationExtractionConfiguration = new InformationExtractionConfiguration();
        }
        this.informationExtractionConfiguration = informationExtractionConfiguration;
        this.informationExtractionCommand = informationExtractionCommand;
    }

    public ScorerConfiguration(TermWeightStrategy termWeightStrategy,
            TermVectorSimilarityStrategy termWeightAggregation,
            InformationExtractionCommand<MessageFeatureContext> informationExtractionCommand,
            ScorerConfigurationFlag... flags) {
        this(termWeightStrategy, termWeightAggregation, informationExtractionCommand,
                informationExtractionCommand.getInformationExtractionConfiguration(), flags);
    }

    public ScorerConfiguration(TermWeightStrategy termWeightStrategy,
            TermVectorSimilarityStrategy termWeightAggregation,
            InformationExtractionConfiguration informationExtractionConfiguration,
            ScorerConfigurationFlag... flags) {
        this(termWeightStrategy, termWeightAggregation,
                (InformationExtractionCommand<MessageFeatureContext>) null,
                informationExtractionConfiguration, flags);
    }

    public ScorerConfiguration(TermWeightStrategy termWeightStrategy,
            TermVectorSimilarityStrategy termWeightAggregation, ScorerConfigurationFlag... flags) {
        this(termWeightStrategy, termWeightAggregation, null, null, flags);
    }

    public void addFlags(ScorerConfigurationFlag... flags) {
        assertCanSet();
        this.flags.addAll(Arrays.asList(flags));

        this.informationExtractionConfiguration.setBeMessageGroupSpecific(this
                .hasFlag(ScorerConfigurationFlag.USE_MESSAGE_GROUP_SPECIFIC_USER_MODEL));
    }

    public boolean addModelsToNotCreateUnknownTermsIn(String modelType) {
        return modelsToNotCreateUnknownTermsIn.add(modelType);
    }

    private void assert01(float value, String field) {
        assertCanSet();
        if (value < 0 || value > 1) {
            throw new IllegalArgumentException(field + " must be between 0 and 1 but is: " + value);
        }
    }

    private void assertCanSet() {
        if (immutable) {
            throw new IllegalStateException(
                    "cannot change values anymore after calling immutable()");
        }
    }

    @Deprecated
    public ScorerConfiguration cloneMe() {
        ScorerConfiguration clone;
        try {
            clone = (ScorerConfiguration) this.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        clone.immutable = false;
        clone.flags = new HashSet<ScorerConfigurationFlag>(this.getFlags());
        return clone;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " " + this.toString();
    }

    public Map<Feature, Float> getFeatureWeights() {
        if (useFixedDefaultFeatureWeights && this.featureWeights == null) {
            featureWeights = FixWeightFeatureAggregator
                    .getFixedDefaults4Scoring(this
                            .hasFlag(ScorerConfigurationFlag.ONLY_USE_TERM_MATCHER_FEATURE_BUT_LEARN_FROM_FEATURES));
        } else if (this.featureWeights == null) {
            featureWeights = new HashMap<Feature, Float>();
        }
        return featureWeights;
    }

    public Collection<ScorerConfigurationFlag> getFlags() {
        return Collections.unmodifiableSet(flags);
    }

    public InformationExtractionCommand<MessageFeatureContext> getInformationExtractionCommand() {
        return informationExtractionCommand;
    }

    public InformationExtractionConfiguration getInformationExtractionConfiguration() {
        return informationExtractionConfiguration;
    }

    public float getInterestTermTreshold() {
        return interestTermTreshold;
    }

    public Map<Feature, Float> getLearningFeatureTresholds() {
        if (this.learningFeatureTresholds == null) {
            learningFeatureTresholds = new HashMap<Feature, Float>();
        }
        return learningFeatureTresholds;
    }

    public Map<Feature, Float> getLearningFeatureWeights() {

        if (useFixedDefaultLearningFeatureWeights && this.learningFeatureWeights == null) {
            learningFeatureWeights = FixWeightFeatureAggregator
                    .getFixedDefaults4Learning(hasFlag(ScorerConfigurationFlag.LEARN_NEGATIVE));
        } else if (this.learningFeatureWeights == null) {
            learningFeatureWeights = new HashMap<Feature, Float>();
        }
        return learningFeatureWeights;
    }

    public float getMessageScoreThreshold() {
        return messageRankThreshold;
    }

    public float getMinContentMessageScore() {
        return minContentMessageScore;
    }

    public int getMinimumCleanTextLengthForInvokingLearner() {
        return minimumCleanTextLengthForInvokingLearner;
    }

    public float getMinUserSimilarity() {
        return minUserSimilarity;
    }

    public float getNonParticipationFactor() {
        return nonParticipationFactor;
    }

    public float getScoreToLearnThreshold() {
        if (scoreToLearnThreshold == null) {
            if (hasFlag(ScorerConfigurationFlag.LEARN_NEGATIVE)) {
                scoreToLearnThreshold = Interest.VERY_LOW.getScore();
            } else {
                scoreToLearnThreshold = Interest.NORMAL.getScore();
            }
        }
        return scoreToLearnThreshold.floatValue();
    }

    public ShortTermMemoryConfiguration getShortTermMemoryConfiguration() {
        return shortTermMemoryConfiguration;
    }

    public String getTermUniquenessLogfile() {
        return termUniquenessLogfile;
    }

    public TermVectorSimilarityStrategy getTermVectorSimilarityStrategy() {
        return termVectorSimilarityStrategy;
    }

    public TermWeightStrategy getTermWeightStrategy() {
        return termWeightStrategy;
    }

    public UserModelAdapterConfiguration getUserModelAdapterConfiguration() {
        return userModelAdapterConfiguration;
    }

    public Map<String, UserModelConfiguration> getUserModelTypes() {
        return userModelTypes;
    }

    public boolean hasFlag(ScorerConfigurationFlag flag) {
        return this.flags.contains(flag);
    }

    public void immutable() {

        this.immutable = true;
    }

    public boolean isCreateUnknownTermsInUsermodel(String userModelType) {
        return !modelsToNotCreateUnknownTermsIn.contains(userModelType);
    }

    public boolean isImmutable() {
        return immutable;
    }

    public boolean isMixMemoriesForRating() {
        return mixMemoriesForRating;
    }

    public boolean isTreatMissingUserModelEntriesAsZero() {
        return treatMissingUserModelEntriesAsZero;
    }

    public boolean isUseFixedDefaultFeatureWeights() {
        return useFixedDefaultFeatureWeights;
    }

    public boolean isUseFixedDefaultLearningFeatureWeights() {
        return useFixedDefaultLearningFeatureWeights;
    }

    public UserModelConfiguration put(String userModelType,
            UserModelConfiguration modelConfiguration) {
        return userModelTypes.put(userModelType, modelConfiguration);
    }

    /**
     * sets the feature weight without using the default weights. but be carefull, getFeatureWeights
     * should not be called before unless useFixedDefaultFeatureWeights is set to true counts.
     * 
     * @param feature
     * @param weight
     */
    public void setFeatureWeight(Feature feature, float weight) {
        this.setFeatureWeight(feature, weight, false);
    }

    public void setFeatureWeight(Feature feature, float weight,
            boolean useFixedDefaultFeatureWeights) {
        if (this.featureWeights != null
                && this.useFixedDefaultFeatureWeights != useFixedDefaultFeatureWeights) {
            throw new IllegalStateException(
                    "this.useFixedDefaultFeatureWeights="
                            + this.useFixedDefaultFeatureWeights
                            + " and featureWeights already initalized. cannot change useFixedDefaultFeatureWeights to "
                            + useFixedDefaultFeatureWeights);
        }
        this.useFixedDefaultFeatureWeights = useFixedDefaultFeatureWeights;
        getFeatureWeights();
        this.featureWeights.put(feature, weight);
    }

    public void setFlags(ScorerConfigurationFlag... flags) {
        assertCanSet();
        this.flags.clear();
        if (flags != null) {
            addFlags(flags);
        }
        this.informationExtractionConfiguration.setBeMessageGroupSpecific(this
                .hasFlag(ScorerConfigurationFlag.USE_MESSAGE_GROUP_SPECIFIC_USER_MODEL));

    }

    public void setInterestTermTreshold(float interestTermTreshold) {
        assert01(interestTermTreshold, "interestTermTreshold");
        this.interestTermTreshold = interestTermTreshold;
    }

    /**
     * sets the learning feature weight without using the default weights. but be carefull,
     * getFeatureWeights should not be called before unless useFixedDefaultFeatureWeights is set to
     * true counts.
     * 
     * @param feature
     * @param weight
     */
    public void setLearningFeatureWeight(Feature feature, float weight) {
        this.setFeatureWeight(feature, weight, false);
    }

    public void setLearningFeatureWeight(Feature feature, float weight,
            boolean useFixedDefaultLearningFeatureWeights) {
        if (this.learningFeatureWeights != null
                && this.useFixedDefaultLearningFeatureWeights != useFixedDefaultLearningFeatureWeights) {
            throw new IllegalStateException(
                    "this.useFixedDefaultLearningFeatureWeights="
                            + this.useFixedDefaultLearningFeatureWeights
                            + " and learningFeatureWeights already initalized. cannot change useFixedDefaultLearningFeatureWeights to "
                            + useFixedDefaultLearningFeatureWeights);
        }
        this.useFixedDefaultLearningFeatureWeights = useFixedDefaultLearningFeatureWeights;
        getLearningFeatureWeights();
        this.learningFeatureWeights.put(feature, weight);
    }

    public void setMessageRankThreshold(float messageRankThreshold) {
        assert01(messageRankThreshold, "messageRankThreshold");
        this.messageRankThreshold = messageRankThreshold;
    }

    public void setMinContentMessageScore(float minContentMessageScore) {
        assert01(minContentMessageScore, "minContentMessageScore");
        this.minContentMessageScore = minContentMessageScore;
    }

    public void setMinimumCleanTextLengthForInvokingLearner(
            int minimumCleanTextLengthForInvokingLearner) {
        this.minimumCleanTextLengthForInvokingLearner = minimumCleanTextLengthForInvokingLearner;
    }

    public void setMinUserSimilarity(float minUserSimilarity) {
        assert01(minUserSimilarity, "minUserSimilarity");
        this.minUserSimilarity = minUserSimilarity;
    }

    public void setMixMemoriesForRating(boolean mixMemoriesForRating) {
        this.mixMemoriesForRating = mixMemoriesForRating;
    }

    public void setNonParticipationFactor(float nonParticipationFactor) {
        this.nonParticipationFactor = nonParticipationFactor;
    }

    public void setScoreToLearnThreshold(float scoreToLearnThreshold) {
        this.scoreToLearnThreshold = scoreToLearnThreshold;
    }

    public void setShortTermMemoryConfiguration(
            ShortTermMemoryConfiguration shortTermMemoryConfiguration) {
        this.shortTermMemoryConfiguration = shortTermMemoryConfiguration;
    }

    public void setTermUniquenessLogfile(String termUniquenessLogfile) {
        // assertCanSet();
        this.termUniquenessLogfile = termUniquenessLogfile;
    }

    public void setTermVectorSimilarityStrategy(
            TermVectorSimilarityStrategy termVectorSimilarityStrategy) {
        assertCanSet();
        if (termVectorSimilarityStrategy == null) {
            throw new IllegalStateException("termVectorSimilarityStrategy cannot be null.");
        }
        this.termVectorSimilarityStrategy = termVectorSimilarityStrategy;
    }

    public void setTermWeightStrategy(TermWeightStrategy termWeightStrategy) {
        assertCanSet();
        if (termWeightStrategy == null) {
            throw new IllegalStateException("termWeightStrategy cannot be null.");
        }
        this.termWeightStrategy = termWeightStrategy;
    }

    public void setTreatMissingUserModelEntriesAsZero(boolean treatMissingUserModelEntriesAsZero) {
        assertCanSet();
        this.treatMissingUserModelEntriesAsZero = treatMissingUserModelEntriesAsZero;
    }

    public void setUseFixedDefaultFeatureWeights(boolean useFixedDefaultFeatureWeights) {
        this.useFixedDefaultFeatureWeights = useFixedDefaultFeatureWeights;
    }

    public void setUseFixedDefaultLearningFeatureWeights(
            boolean useFixedDefaultLearningFeatureWeights) {
        this.useFixedDefaultLearningFeatureWeights = useFixedDefaultLearningFeatureWeights;
    }

    public void setUserModelType(Map<String, UserModelConfiguration> userModelTypes) {
        if (userModelTypes == null) {
            throw new IllegalArgumentException("userModelType cannot be null.");
        }
        assertCanSet();
        this.userModelTypes = userModelTypes;
    }

    @Override
    public String toString() {
        return "RankerConfiguration [flags=" + flags + ", minUserSimilarity=" + minUserSimilarity
                + ", minContentMessageScore=" + minContentMessageScore + ", messageRankThreshold="
                + messageRankThreshold + ", nonParticipationFactor=" + nonParticipationFactor
                + ", minimumCleanTextLengthForInvokingLearner="
                + minimumCleanTextLengthForInvokingLearner + ", termWeightStrategy="
                + termWeightStrategy + ", termVectorSimilarityStrategy="
                + termVectorSimilarityStrategy + ", immutable=" + immutable
                + ", interestTermTreshold=" + interestTermTreshold
                + ", treatMissingUserModelEntriesAsZero=" + treatMissingUserModelEntriesAsZero
                + ", termUniquenessLogfile=" + termUniquenessLogfile + ", userModelTypes="
                + userModelTypes + ", informationExtractionConfiguration="
                + informationExtractionConfiguration + ", informationExtractionCommand="
                + informationExtractionCommand + ", userModelAdapterConfiguration="
                + userModelAdapterConfiguration + ", mixMemoriesForRating=" + mixMemoriesForRating
                + ", shortTermMemoryConfiguration=" + shortTermMemoryConfiguration
                + ", modelsToNotCreateUnknownTermsIn=" + modelsToNotCreateUnknownTermsIn
                + ", useFixedDefaultFeatureWeights=" + useFixedDefaultFeatureWeights
                + ", useFixedDefaultLearningFeatureWeights="
                + useFixedDefaultLearningFeatureWeights + ", featureWeights=" + featureWeights
                + ", scoreToLearnThreshold=" + scoreToLearnThreshold + ", learningFeatureWeights="
                + learningFeatureWeights + ", learningFeatureTresholds=" + learningFeatureTresholds
                + "]";
    }

}