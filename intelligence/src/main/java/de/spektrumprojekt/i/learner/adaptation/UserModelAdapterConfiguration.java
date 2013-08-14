package de.spektrumprojekt.i.learner.adaptation;

import org.apache.commons.lang3.StringUtils;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.i.user.hits.HITSUserMentionComputer.ScoreToUse;
import de.spektrumprojekt.i.user.similarity.UserSimilarityComputer.UserSimilaritySimType;

public class UserModelAdapterConfiguration implements ConfigurationDescriptable {

    private double userSimilarityThreshold;

    private boolean userSelectorUseHITS;

    private boolean userSelectorUseMentionsPercentage;

    private ScoreToUse hitsScoreToUse;

    private boolean useWeightedAverageForAggregatingSimilarUsers = true;

    private float rankThreshold = 0.75f;
    private float confidenceThreshold = 0.5f;

    private UserSimilaritySimType userSimilaritySimType;

    public float getConfidenceThreshold() {
        return confidenceThreshold;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName()
                + StringUtils.join(new String[] {
                        "userSelectorUseHITS=" + userSelectorUseHITS,
                        "userSelectorUseMentionsPercentage=" + userSelectorUseMentionsPercentage,
                        "userSimilarityThreshold=" + userSimilarityThreshold,
                        "hitsScoreToUse=" + hitsScoreToUse,
                        "useWeightedAverageForAggregatingSimilarUsers="
                                + useWeightedAverageForAggregatingSimilarUsers,
                        "rankThreshold=" + rankThreshold,
                        "confidenceThreshold=" + confidenceThreshold,
                        "userSimilaritySimType=" + userSimilaritySimType
                }, " ");
    }

    public ScoreToUse getHitsScoreToUse() {
        return hitsScoreToUse;
    }

    public float getRankThreshold() {
        return rankThreshold;
    }

    public UserSimilaritySimType getUserSimilaritySimType() {
        return userSimilaritySimType;
    }

    public double getUserSimilarityThreshold() {
        return userSimilarityThreshold;
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

    public void setConfidenceThreshold(float confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }

    public void setHitsScoreToUse(ScoreToUse hitsScoreToUse) {
        this.hitsScoreToUse = hitsScoreToUse;
    }

    public void setRankThreshold(float rankThreshold) {
        this.rankThreshold = rankThreshold;
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
                + ", hitsScoreToUse=" + hitsScoreToUse
                + ", useWeightedAverageForAggregatingSimilarUsers="
                + useWeightedAverageForAggregatingSimilarUsers + ", rankThreshold=" + rankThreshold
                + ", confidenceThreshold=" + confidenceThreshold + ", userSimilaritySimType="
                + userSimilaritySimType + "]";
    }

}
