package de.spektrumprojekt.i.learner.adaptation;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.i.similarity.SetSimilarity;
import de.spektrumprojekt.i.user.hits.HITSUserMentionComputer.ScoreToUse;
import de.spektrumprojekt.i.user.similarity.UserSimilaritySimType;

public class UserModelAdapterConfiguration implements ConfigurationDescriptable {

    public static class UserModelBasedSimilarityConfiguration implements ConfigurationDescriptable {

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

    private double userSimilarityThreshold;

    private boolean userSelectorUseHITS;

    private boolean userSelectorUseMentionsPercentage;

    private ScoreToUse hitsScoreToUse;

    private boolean adaptFromMessageGroups;

    private boolean useWeightedAverageForAggregatingSimilarUsers = true;

    private float scoreThreshold = 0.75f;

    private float confidenceThreshold = 0.5f;

    private UserSimilaritySimType userSimilaritySimType;

    private UserModelBasedSimilarityConfiguration userModelBasedSimilarityConfiguration;

    public float getConfidenceThreshold() {
        return confidenceThreshold;
    }

    @Override
    public String getConfigurationDescription() {
        return this.toString();
    }

    public ScoreToUse getHitsScoreToUse() {
        return hitsScoreToUse;
    }

    public float getScoreThreshold() {
        return scoreThreshold;
    }

    public UserModelBasedSimilarityConfiguration getUserModelBasedSimilarityConfiguration() {
        return userModelBasedSimilarityConfiguration;
    }

    public UserSimilaritySimType getUserSimilaritySimType() {
        return userSimilaritySimType;
    }

    public double getUserSimilarityThreshold() {
        return userSimilarityThreshold;
    }

    /**
     * If true the DUMA will adapt from the same user user model but from different message groups.
     * only works if a message group specific model is maintained.
     * 
     * @return
     */
    public boolean isAdaptFromMessageGroups() {
        return adaptFromMessageGroups;
    }

    public boolean isUserSelectorUseHITS() {
        return userSelectorUseHITS;
    }

    public boolean isUserSelectorUseMentionsPercentage() {
        return userSelectorUseMentionsPercentage;
    }

    public boolean isUseWeightedAverageForAggregatingSimilarUsers() {
        return useWeightedAverageForAggregatingSimilarUsers;
    }

    public boolean isValid() {
        if (userSelectorUseHITS && this.hitsScoreToUse == null) {
            return false;
        }
        // one of both must be set
        return userSelectorUseHITS != userSelectorUseMentionsPercentage
                && userSimilarityThreshold >= 0
                && userSimilarityThreshold <= 1;
    }

    public void setAdaptFromMessageGroups(boolean adaptFromMessageGroups) {
        this.adaptFromMessageGroups = adaptFromMessageGroups;
    }

    public void setConfidenceThreshold(float confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }

    public void setHitsScoreToUse(ScoreToUse hitsScoreToUse) {
        this.hitsScoreToUse = hitsScoreToUse;
    }

    public void setScoreThreshold(float scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
    }

    public void setUserModelBasedSimilarityConfiguration(
            UserModelBasedSimilarityConfiguration userModelBasedSimilarityConfiguration) {
        this.userModelBasedSimilarityConfiguration = userModelBasedSimilarityConfiguration;
    }

    public void setUserSelectorUseHITS(boolean userSelectorUseHITS) {
        this.userSelectorUseHITS = userSelectorUseHITS;
    }

    public void setUserSelectorUseMentionsPercentage(boolean userSelectorUseMentionsPercentage) {
        this.userSelectorUseMentionsPercentage = userSelectorUseMentionsPercentage;
    }

    public void setUserSimilaritySimType(UserSimilaritySimType userSimilaritySimType) {
        this.userSimilaritySimType = userSimilaritySimType;
    }

    public void setUserSimilarityThreshold(double userSimilarityThreshold) {
        this.userSimilarityThreshold = userSimilarityThreshold;
    }

    public void setUseWeightedAverageForAggregatingSimilarUsers(
            boolean useWeightedAverageForAggregatingSimilarUsers) {
        this.useWeightedAverageForAggregatingSimilarUsers = useWeightedAverageForAggregatingSimilarUsers;
    }

    @Override
    public String toString() {
        return "UserModelAdapterConfiguration [userSimilarityThreshold=" + userSimilarityThreshold
                + ", userSelectorUseHITS=" + userSelectorUseHITS
                + ", userSelectorUseMentionsPercentage=" + userSelectorUseMentionsPercentage
                + ", hitsScoreToUse=" + hitsScoreToUse + ", adaptFromMessageGroups="
                + adaptFromMessageGroups + ", useWeightedAverageForAggregatingSimilarUsers="
                + useWeightedAverageForAggregatingSimilarUsers + ", scoreThreshold="
                + scoreThreshold + ", confidenceThreshold=" + confidenceThreshold
                + ", userSimilaritySimType=" + userSimilaritySimType
                + ", userModelBasedSimilarityConfiguration="
                + userModelBasedSimilarityConfiguration + "]";
    }

}
