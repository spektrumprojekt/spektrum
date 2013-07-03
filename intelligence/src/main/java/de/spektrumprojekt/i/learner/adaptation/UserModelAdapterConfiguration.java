package de.spektrumprojekt.i.learner.adaptation;

import org.apache.commons.lang3.StringUtils;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.i.user.hits.HITSUserMentionComputer.ScoreToUse;

public class UserModelAdapterConfiguration implements ConfigurationDescriptable {

    private double userSimilarityThreshold;

    private boolean userSelectorUseHITS;

    private boolean userSelectorUseMentionsPercentage;

    private ScoreToUse hitsScoreToUse;

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + StringUtils.join(new String[] {
                "userSelectorUseHITS=" + userSelectorUseHITS,
                "userSelectorUseMentionsPercentage=" + userSelectorUseMentionsPercentage,
                "userSimilarityThreshold=" + userSimilarityThreshold,
                "hitsScoreToUse=" + hitsScoreToUse
        }, " ");
    }

    public ScoreToUse getHitsScoreToUse() {
        return hitsScoreToUse;
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

    public boolean isValid() {
        if (userSelectorUseHITS && this.hitsScoreToUse == null) {
            return false;
        }
        // one of both must be set
        return userSelectorUseHITS != userSelectorUseMentionsPercentage
                && userSimilarityThreshold >= 0
                && userSimilarityThreshold <= 1;
    }

    public void setHitsScoreToUse(ScoreToUse hitsScoreToUse) {
        this.hitsScoreToUse = hitsScoreToUse;
    }

    public void setUserSelectorUseHITS(boolean userSelectorUseHITS) {
        this.userSelectorUseHITS = userSelectorUseHITS;
    }

    public void setUserSelectorUseMentionsPercentage(boolean userSelectorUseMentionsPercentage) {
        this.userSelectorUseMentionsPercentage = userSelectorUseMentionsPercentage;
    }

    public void setUserSimilarityThreshold(double userSimilarityThreshold) {
        this.userSimilarityThreshold = userSimilarityThreshold;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserModelAdapterConfiguration [userSimilarityThreshold=");
        builder.append(userSimilarityThreshold);
        builder.append(", userSelectorUseHITS=");
        builder.append(userSelectorUseHITS);
        builder.append(", userSelectorUseMentionsPercentage=");
        builder.append(userSelectorUseMentionsPercentage);
        builder.append(", hitsScoreToUse=");
        builder.append(hitsScoreToUse);
        builder.append("]");
        return builder.toString();
    }

}
