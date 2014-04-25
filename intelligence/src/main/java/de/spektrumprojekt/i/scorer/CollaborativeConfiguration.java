package de.spektrumprojekt.i.scorer;

import java.io.File;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.i.collab.CollaborativeScoreComputerType;
import de.spektrumprojekt.i.scorer.feature.Feature;

public class CollaborativeConfiguration implements ConfigurationDescriptable {

    private CollaborativeScoreComputerType collaborativeScoreComputerType;

    private boolean useGenericRecommender;

    private boolean outPreferencesToFile;

    private File preferencesDebugFile = new File("preferences.txt");

    private boolean slopeOneUseWeighted = true;
    private boolean slopeOneUseStdDevWeighted = true;
    private long slopeOneMaxEntries = Long.MAX_VALUE;

    // it is needed for backreference for the scoring feature weight. not perfect.
    private final ScorerConfiguration scorerConfiguration;

    public CollaborativeConfiguration(ScorerConfiguration scorerConfiguration) {
        this.scorerConfiguration = scorerConfiguration;
    }

    public CollaborativeScoreComputerType getCollaborativeScoreComputerType() {
        return collaborativeScoreComputerType;
    }

    @Override
    public String getConfigurationDescription() {
        return this.toString();
    }

    public File getPreferencesDebugFile() {
        return preferencesDebugFile;
    }

    public float getScoringFeatureWeight() {
        Float weight = internalGetScoringFeatureWeight();
        if (weight == null) {
            throw new IllegalStateException(
                    "no collab feature weight defined. set it on scorerConfiguration.getFeatureWeights()");
        }
        return weight.floatValue();
    }

    public long getSlopeOneMaxEntries() {
        return slopeOneMaxEntries;
    }

    private Float internalGetScoringFeatureWeight() {
        Float weight = scorerConfiguration.getFeatureWeights().get(
                Feature.COLLABORATION_MATCH_FEATURE);
        return weight;
    }

    public boolean isOutPreferencesToFile() {
        return outPreferencesToFile;
    }

    public boolean isSlopeOneUseStdDevWeighted() {
        return slopeOneUseStdDevWeighted;
    }

    public boolean isSlopeOneUseWeighted() {
        return slopeOneUseWeighted;
    }

    public boolean isUseGenericRecommender() {
        return useGenericRecommender;
    }

    public void setCollaborativeScoreComputerType(
            CollaborativeScoreComputerType collaborativeScoreComputerType) {
        this.collaborativeScoreComputerType = collaborativeScoreComputerType;
    }

    public void setOutPreferencesToFile(boolean outPreferencesToFile) {
        this.outPreferencesToFile = outPreferencesToFile;
    }

    public void setPreferencesDebugFile(File preferencesDebugFile) {
        this.preferencesDebugFile = preferencesDebugFile;
    }

    public void setSlopeOneMaxEntries(long slopeOneMaxEntries) {
        this.slopeOneMaxEntries = slopeOneMaxEntries;
    }

    public void setSlopeOneUseStdDevWeighted(boolean slopeOneUseStdDevWeighted) {
        this.slopeOneUseStdDevWeighted = slopeOneUseStdDevWeighted;
    }

    public void setSlopeOneUseWeighted(boolean slopeOneUseWeighted) {
        this.slopeOneUseWeighted = slopeOneUseWeighted;
    }

    public void setUseGenericRecommender(boolean useGenericRecommender) {
        this.useGenericRecommender = useGenericRecommender;
    }

    @Override
    public String toString() {
        return "CollaborativeConfiguration [collaborativeScoreComputerType="
                + collaborativeScoreComputerType + ", useGenericRecommender="
                + useGenericRecommender + ", outPreferencesToFile=" + outPreferencesToFile
                + ", preferencesDebugFile=" + preferencesDebugFile + ", slopeOneUseWeighted="
                + slopeOneUseWeighted + ", slopeOneUseStdDevWeighted=" + slopeOneUseStdDevWeighted
                + ", slopeOneMaxEntries=" + slopeOneMaxEntries
                + ", internalGetScoringFeatureWeight()="
                + internalGetScoringFeatureWeight()
                + "]";
    }

}