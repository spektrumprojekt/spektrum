package de.spektrumprojekt.i.ranker;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.i.term.TermVectorSimilarityStrategy;
import de.spektrumprojekt.i.term.TermWeightStrategy;
import de.spektrumprojekt.informationextraction.extractors.TagSource;

public class RankerConfiguration implements ConfigurationDescriptable, Cloneable {

    private Set<RankerConfigurationFlag> flags = new HashSet<RankerConfigurationFlag>();

    // the minimum user similarity that must be fullfilled to be eglible to take the score of
    private float minUserSimilarity = 0.5f;

    // the minimum score the cmf feature of a similar user must have such that it will be used.
    private float minContentMessageScore = 0.5f;

    // the message rank threshold determines for which users the adaption will take place.
    private float messageRankThreshold = 0.5f;
    private float nonParticipationFactor = 1f;

    private boolean doTags;

    private boolean doTokens = true;

    private boolean doKeyphrase;
    private boolean addTagsToText;
    private int minimumCleanTextLengthForInvokingLearner;
    private int minimumTermLength;

    private TermWeightStrategy termWeightStrategy;
    private TermVectorSimilarityStrategy termVectorSimilarityStrategy;

    private boolean immutable;

    private float interestTermTreshold = 0.75f;
    private boolean useWordNGrams;

    private boolean useCharNGrams;

    private int nGramsLength = 2;

    private boolean treatMissingUserModelEntriesAsZero;

    private boolean charNGramsRemoveStopwords;
    private String termUniquenessLogfile;
    private boolean matchTextAgainstTagSource;
    private TagSource tagSource;

    private int rankerConfiguration;

    public RankerConfiguration(TermWeightStrategy strategy, TermVectorSimilarityStrategy aggregation) {
        this(strategy, aggregation, (RankerConfigurationFlag[]) null);
    }

    public RankerConfiguration(TermWeightStrategy termWeightStrategy,
            TermVectorSimilarityStrategy termWeightAggregation, RankerConfigurationFlag... flags) {
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
    }

    public void addFlags(RankerConfigurationFlag... flags) {
        assertCanSet();
        this.flags.addAll(Arrays.asList(flags));
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

    public int getMinimumTermLength() {
        return minimumTermLength;
    }

    public float getMinUserSimilarity() {
        return minUserSimilarity;
    }

    public int getNGramsLength() {
        return nGramsLength;
    }

    public float getNonParticipationFactor() {
        return nonParticipationFactor;
    }

    public TagSource getTagSource() {
        return tagSource;
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

    public boolean hasFlag(RankerConfigurationFlag flag) {
        return this.flags.contains(flag);
    }

    public void immutable() {

        this.immutable = true;
    }

    public boolean isAddTagsToText() {
        return addTagsToText;
    }

    public boolean isCharNGramsRemoveStopwords() {
        return charNGramsRemoveStopwords;
    }

    public boolean isDoKeyphrase() {

        return doKeyphrase;
    }

    public boolean isDoTags() {
        return doTags;
    }

    public boolean isDoTokens() {
        return doTokens;
    }

    public boolean isImmutable() {
        return immutable;
    }

    public boolean isMatchTextAgainstTagSource() {
        return matchTextAgainstTagSource;
    }

    public boolean isTreatMissingUserModelEntriesAsZero() {
        return treatMissingUserModelEntriesAsZero;
    }

    public boolean isUseCharNGrams() {
        return this.useCharNGrams;
    }

    public boolean isUseWordNGrams() {
        return useWordNGrams;
    }

    public void setAddTagsToText(boolean addTagsToText) {
        this.addTagsToText = addTagsToText;
    }

    public void setCharNGramsRemoveStopwords(boolean charNGramsRemoveStopwords) {
        assertCanSet();
        this.charNGramsRemoveStopwords = charNGramsRemoveStopwords;
    }

    public void setDoKeyphrase(boolean doKeyphrase) {
        assertCanSet();
        this.doKeyphrase = doKeyphrase;
    }

    public void setDoTags(boolean doTags) {
        assertCanSet();
        this.doTags = doTags;
    }

    public void setDoTokens(boolean doTokens) {
        assertCanSet();
        this.doTokens = doTokens;
    }

    public void setFlags(RankerConfigurationFlag... flags) {
        assertCanSet();
        this.flags.clear();
        if (flags != null) {
            addFlags(flags);
        }
    }

    public void setInterestTermTreshold(float interestTermTreshold) {
        assert01(interestTermTreshold, "interestTermTreshold");
        this.interestTermTreshold = interestTermTreshold;
    }

    public void setMatchTextAgainstTagSource(boolean matchTextAgainstTagSource) {
        this.matchTextAgainstTagSource = matchTextAgainstTagSource;
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

    public void setMinimumTermLength(int minimumTermLength) {
        assertCanSet();
        this.minimumTermLength = minimumTermLength;
    }

    public void setMinUserSimilarity(float minUserSimilarity) {
        assert01(minUserSimilarity, "minUserSimilarity");
        this.minUserSimilarity = minUserSimilarity;
    }

    public void setNGramsLength(int nGramsLength) {
        assertCanSet();
        this.nGramsLength = nGramsLength;
    }

    public void setNonParticipationFactor(float nonParticipationFactor) {
        this.nonParticipationFactor = nonParticipationFactor;
    }

    public void setTagSource(TagSource tagSource) {
        this.tagSource = tagSource;
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

    public void setUseCharNGrams(boolean useCharNGrams) {
        assertCanSet();
        this.useCharNGrams = useCharNGrams;
    }

    public void setUseWordNGrams(boolean useWordNGrams) {
        assertCanSet();
        this.useWordNGrams = useWordNGrams;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RankerConfiguration [flags=");
        builder.append(flags);
        builder.append(", minUserSimilarity=");
        builder.append(minUserSimilarity);
        builder.append(", minContentMessageScore=");
        builder.append(minContentMessageScore);
        builder.append(", messageRankThreshold=");
        builder.append(messageRankThreshold);
        builder.append(", doTags=");
        builder.append(doTags);
        builder.append(", doTokens=");
        builder.append(doTokens);
        builder.append(", doKeyphrase=");
        builder.append(doKeyphrase);
        builder.append(", addTagsToText=");
        builder.append(addTagsToText);
        builder.append(", termWeightStrategy=");
        builder.append(termWeightStrategy);
        builder.append(", termWeightAggregation=");
        builder.append(termVectorSimilarityStrategy);
        builder.append(", immutable=");
        builder.append(immutable);
        builder.append(", interestTermTreshold=");
        builder.append(interestTermTreshold);
        builder.append("]");
        return builder.toString();
    }

}