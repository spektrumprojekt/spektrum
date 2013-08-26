package de.spektrumprojekt.i.ranker;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.i.informationextraction.InformationExtractionCommand;
import de.spektrumprojekt.i.informationextraction.InformationExtractionConfiguration;
import de.spektrumprojekt.i.learner.adaptation.UserModelAdapterConfiguration;
import de.spektrumprojekt.i.term.TermVectorSimilarityStrategy;
import de.spektrumprojekt.i.term.TermWeightStrategy;
import de.spektrumprojekt.i.timebased.config.ShortTermMemoryConfiguration;

public class RankerConfiguration implements ConfigurationDescriptable, Cloneable {

    private Set<RankerConfigurationFlag> flags = new HashSet<RankerConfigurationFlag>();

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

    public RankerConfiguration(TermWeightStrategy strategy, TermVectorSimilarityStrategy aggregation) {
        this(strategy, aggregation, null, null, (RankerConfigurationFlag[]) null);
    }

    private RankerConfiguration(TermWeightStrategy termWeightStrategy,
            TermVectorSimilarityStrategy termWeightAggregation,
            InformationExtractionCommand<MessageFeatureContext> informationExtractionCommand,
            InformationExtractionConfiguration informationExtractionConfiguration,
            RankerConfigurationFlag... flags) {
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

    public RankerConfiguration(TermWeightStrategy termWeightStrategy,
            TermVectorSimilarityStrategy termWeightAggregation,
            InformationExtractionCommand<MessageFeatureContext> informationExtractionCommand,
            RankerConfigurationFlag... flags) {
        this(termWeightStrategy, termWeightAggregation, informationExtractionCommand,
                informationExtractionCommand.getInformationExtractionConfiguration(), flags);
    }

    public RankerConfiguration(TermWeightStrategy termWeightStrategy,
            TermVectorSimilarityStrategy termWeightAggregation,
            InformationExtractionConfiguration informationExtractionConfiguration,
            RankerConfigurationFlag... flags) {
        this(termWeightStrategy, termWeightAggregation,
                (InformationExtractionCommand<MessageFeatureContext>) null,
                informationExtractionConfiguration, flags);
    }

    public RankerConfiguration(TermWeightStrategy termWeightStrategy,
            TermVectorSimilarityStrategy termWeightAggregation, RankerConfigurationFlag... flags) {
        this(termWeightStrategy, termWeightAggregation, null, null, flags);
    }

    public void addFlags(RankerConfigurationFlag... flags) {
        assertCanSet();
        this.flags.addAll(Arrays.asList(flags));

        this.informationExtractionConfiguration.setBeMessageGroupSpecific(this
                .hasFlag(RankerConfigurationFlag.USE_MESSAGE_GROUP_SPECIFIC_USER_MODEL));
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

    public RankerConfiguration cloneMe() {
        RankerConfiguration clone;
        try {
            clone = (RankerConfiguration) this.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        clone.immutable = false;
        clone.flags = new HashSet<RankerConfigurationFlag>(this.getFlags());
        return clone;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " " + this.toString();
    }

    public Collection<RankerConfigurationFlag> getFlags() {
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

    public float getMessageRankThreshold() {
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

    public boolean hasFlag(RankerConfigurationFlag flag) {
        return this.flags.contains(flag);
    }

    public void immutable() {

        this.immutable = true;
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

    public UserModelConfiguration put(String userModelType,
            UserModelConfiguration modelConfiguration) {
        return userModelTypes.put(userModelType, modelConfiguration);
    }

    public void setFlags(RankerConfigurationFlag... flags) {
        assertCanSet();
        this.flags.clear();
        if (flags != null) {
            addFlags(flags);
        }
        this.informationExtractionConfiguration.setBeMessageGroupSpecific(this
                .hasFlag(RankerConfigurationFlag.USE_MESSAGE_GROUP_SPECIFIC_USER_MODEL));

    }

    public void setInterestTermTreshold(float interestTermTreshold) {
        assert01(interestTermTreshold, "interestTermTreshold");
        this.interestTermTreshold = interestTermTreshold;
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

    public void setUserModelType(Map<String, UserModelConfiguration> userModelTypes) {
        if (userModelTypes == null) {
            throw new IllegalArgumentException("userModelType cannot be null.");
        }
        assertCanSet();
        this.userModelTypes = userModelTypes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Entry<String, UserModelConfiguration> entry : userModelTypes.entrySet()) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(entry.getKey());
            sb.append(":");
            sb.append(entry.getValue().getConfigurationDescription());
        }
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
                + sb.toString() + ", informationExtractionConfiguration="
                + informationExtractionConfiguration + ", informationExtractionCommand="
                + informationExtractionCommand + ", userModelAdapterConfiguration="
                + userModelAdapterConfiguration + ", mixMemoriesForRating=" + mixMemoriesForRating
                + ", shortTermMemoryConfiguration="
                + shortTermMemoryConfiguration.getConfigurationDescription() + "]";
    }

}