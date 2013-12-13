package de.spektrumprojekt.i.learner.adaptation;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.i.similarity.set.SetSimilarity;

public class UserModelBasedSimilarityConfiguration implements ConfigurationDescriptable {

    private SetSimilarity setSimilarity;
    private String precomputedUserSimilaritesFilename;

    @Override
    public String getConfigurationDescription() {
        return toString();
    }

    public String getPrecomputedUserSimilaritesFilename() {
        return precomputedUserSimilaritesFilename;
    }

    public SetSimilarity getSetSimilarity() {
        return setSimilarity;
    }

    public void setPrecomputedUserSimilaritesFilename(String precomputedUserSimilaritesFilename) {
        this.precomputedUserSimilaritesFilename = precomputedUserSimilaritesFilename;
    }

    public void setSetSimilarity(SetSimilarity setSimilarity) {
        this.setSimilarity = setSimilarity;
    }

    @Override
    public String toString() {
        return "UserModelBasedSimilarityConfiguration [setSimilarity=" + setSimilarity
                + ", precomputedUserSimilaritesFilename=" + precomputedUserSimilaritesFilename
                + "]";
    }

}