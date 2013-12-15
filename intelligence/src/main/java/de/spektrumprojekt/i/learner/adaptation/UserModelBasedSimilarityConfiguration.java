package de.spektrumprojekt.i.learner.adaptation;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.i.similarity.set.SetSimilarity;

public class UserModelBasedSimilarityConfiguration implements ConfigurationDescriptable {

    private SetSimilarity setSimilarity;

    private String precomputedUserSimilaritesFilename;

    private String basicSimFilename = "H:/Files/Thesis-Work/evaluations/precomputed/test-usim-write";

    private boolean writeUserSimilaritiesToPrecomputedFile;
    private boolean readUserSimilaritiesFromPrecomputedFile;
    private boolean precomputedIsWithDate;

    public String getBasicSimFilename() {
        return basicSimFilename;
    }

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

    public boolean isPrecomputedIsWithDate() {
        return precomputedIsWithDate;
    }

    public boolean isReadUserSimilaritiesFromPrecomputedFile() {
        return readUserSimilaritiesFromPrecomputedFile;
    }

    public boolean isWriteUserSimilaritiesToPrecomputedFile() {
        return writeUserSimilaritiesToPrecomputedFile;
    }

    public void setBasicSimFilename(String basicSimFilename) {
        this.basicSimFilename = basicSimFilename;
    }

    public void setPrecomputedIsWithDate(boolean precomputedIsWithDate) {
        this.precomputedIsWithDate = precomputedIsWithDate;
    }

    public void setPrecomputedUserSimilaritesFilename(String precomputedUserSimilaritesFilename) {
        this.precomputedUserSimilaritesFilename = precomputedUserSimilaritesFilename;
    }

    public void setReadUserSimilaritiesFromPrecomputedFile(
            boolean readUserSimilaritiesFromPrecomputedFile) {
        this.readUserSimilaritiesFromPrecomputedFile = readUserSimilaritiesFromPrecomputedFile;
    }

    public void setSetSimilarity(SetSimilarity setSimilarity) {
        this.setSimilarity = setSimilarity;
    }

    public void setWriteUserSimilaritiesToPrecomputedFile(
            boolean writeUserSimilaritiesToPrecomputedFile) {
        this.writeUserSimilaritiesToPrecomputedFile = writeUserSimilaritiesToPrecomputedFile;
    }

    @Override
    public String toString() {
        return "UserModelBasedSimilarityConfiguration [setSimilarity=" + setSimilarity
                + ", precomputedUserSimilaritesFilename=" + precomputedUserSimilaritesFilename
                + ", basicSimFilename=" + basicSimFilename
                + ", writeUserSimilaritiesToPrecomputedFile="
                + writeUserSimilaritiesToPrecomputedFile
                + ", readUserSimilaritiesFromPrecomputedFile="
                + readUserSimilaritiesFromPrecomputedFile + ", precomputedIsWithDate="
                + precomputedIsWithDate + "]";
    }

}