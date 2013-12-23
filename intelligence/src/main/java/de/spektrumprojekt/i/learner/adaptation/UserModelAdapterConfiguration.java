package de.spektrumprojekt.i.learner.adaptation;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.i.similarity.messagegroup.MessageGroupSimilarityConfiguration;
import de.spektrumprojekt.i.similarity.user.UserModelBasedSimilarityConfiguration;
import de.spektrumprojekt.i.similarity.user.UserSimilaritySimType;
import de.spektrumprojekt.i.similarity.user.hits.HITSUserMentionComputer.ScoreToUse;

public class UserModelAdapterConfiguration implements ConfigurationDescriptable {

    private double userSimilarityThreshold;
    private double messageGroupSimilarityThreshold;

    // use only the top mgs for adaptation, 0 for all
    private int topNMessageGroupsToUseForAdaptation;
    // use only the top users for adaptation, 0 for all
    private int topNUsersToUseForAdaptation;

    private boolean userSelectorUseHITS;

    private boolean userSelectorUseMentionsPercentage;

    private ScoreToUse hitsScoreToUse;

    private boolean adaptFromMessageGroups;

    private boolean useWeightedAverageForAggregatingSimilarUsers = true;

    private float scoreThreshold = 0.75f;

    private float confidenceThreshold = 0.5f;

    private UserSimilaritySimType userSimilaritySimType;

    private UserModelBasedSimilarityConfiguration userModelBasedSimilarityConfiguration;
    private MessageGroupSimilarityConfiguration messageGroupSimilarityConfiguration;

    private boolean adaptFromUsers;
    private int intervallOfUserSimComputationInDays = 1;

    private int votingAggregatorNecassaryVotes = 3;
    private boolean votingAggregatorStrict = false;
    private double votingAggregatorStrictThreshold = 0.5d;

    private boolean onlyUseAdaptedTermsForRescoring;

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

    public int getIntervallOfUserSimComputationInDays() {
        return intervallOfUserSimComputationInDays;
    }

    public MessageGroupSimilarityConfiguration getMessageGroupSimilarityConfiguration() {
        return messageGroupSimilarityConfiguration;
    }

    public double getMessageGroupSimilarityThreshold() {
        return messageGroupSimilarityThreshold;
    }

    public float getScoreThreshold() {
        return scoreThreshold;
    }

    public int getTopNMessageGroupsToUseForAdaptation() {
        return topNMessageGroupsToUseForAdaptation;
    }

    public int getTopNUsersToUseForAdaptation() {
        return topNUsersToUseForAdaptation;
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

    public int getVotingAggregatorNecassaryVotes() {
        return votingAggregatorNecassaryVotes;
    }

    public double getVotingAggregatorStrictThreshold() {
        return votingAggregatorStrictThreshold;
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

    public boolean isAdaptFromUsers() {
        return adaptFromUsers;
    }

    public boolean isOnlyUseAdaptedTermsForRescoring() {
        return onlyUseAdaptedTermsForRescoring;
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
        if (!adaptFromMessageGroups && userSelectorUseHITS && this.hitsScoreToUse == null) {
            return false;
        }
        // one of both must be set
        return userSelectorUseHITS != userSelectorUseMentionsPercentage
                && userSimilarityThreshold >= 0
                && userSimilarityThreshold <= 1 || adaptFromMessageGroups;
    }

    public boolean isVotingAggregatorStrict() {
        return votingAggregatorStrict;
    }

    public void setAdaptFromMessageGroups(boolean adaptFromMessageGroups) {
        this.adaptFromMessageGroups = adaptFromMessageGroups;
    }

    public void setAdaptFromUsers(boolean adaptFromUsers) {
        this.adaptFromUsers = adaptFromUsers;
    }

    public void setConfidenceThreshold(float confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }

    public void setHitsScoreToUse(ScoreToUse hitsScoreToUse) {
        this.hitsScoreToUse = hitsScoreToUse;
    }

    public void setIntervallOfUserSimComputationInDays(int intervallOfUserSimComputationInDays) {
        this.intervallOfUserSimComputationInDays = intervallOfUserSimComputationInDays;
    }

    public void setMessageGroupSimilarityConfiguration(
            MessageGroupSimilarityConfiguration messageGroupSimilarityConfiguration) {
        this.messageGroupSimilarityConfiguration = messageGroupSimilarityConfiguration;
    }

    public void setMessageGroupSimilarityThreshold(double messageGroupSimilarityThreshold) {
        this.messageGroupSimilarityThreshold = messageGroupSimilarityThreshold;
    }

    public void setOnlyUseAdaptedTermsForRescoring(boolean onlyUseAdaptedTermsForRescoring) {
        this.onlyUseAdaptedTermsForRescoring = onlyUseAdaptedTermsForRescoring;
    }

    public void setScoreThreshold(float scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
    }

    public void setTopNMessageGroupsToUseForAdaptation(int topNMessageGroupsToUseForAdaptation) {
        this.topNMessageGroupsToUseForAdaptation = topNMessageGroupsToUseForAdaptation;
    }

    public void setTopNUsersToUseForAdaptation(int topNUsersToUseForAdaptation) {
        this.topNUsersToUseForAdaptation = topNUsersToUseForAdaptation;
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

    public void setVotingAggregatorNecassaryVotes(int votingAggregatorNecassaryVotes) {
        this.votingAggregatorNecassaryVotes = votingAggregatorNecassaryVotes;
    }

    public void setVotingAggregatorStrict(boolean votingAggregatorStrict) {
        this.votingAggregatorStrict = votingAggregatorStrict;
    }

    public void setVotingAggregatorStrictThreshold(double votingAggregatorStrictThreshold) {
        this.votingAggregatorStrictThreshold = votingAggregatorStrictThreshold;
    }

    @Override
    public String toString() {
        return "UserModelAdapterConfiguration [userSimilarityThreshold=" + userSimilarityThreshold
                + ", messageGroupSimilarityThreshold=" + messageGroupSimilarityThreshold
                + ", topNMessageGroupsToUseForAdaptation=" + topNMessageGroupsToUseForAdaptation
                + ", topNUsersToUseForAdaptation=" + topNUsersToUseForAdaptation
                + ", userSelectorUseHITS=" + userSelectorUseHITS
                + ", userSelectorUseMentionsPercentage=" + userSelectorUseMentionsPercentage
                + ", hitsScoreToUse=" + hitsScoreToUse + ", adaptFromMessageGroups="
                + adaptFromMessageGroups + ", useWeightedAverageForAggregatingSimilarUsers="
                + useWeightedAverageForAggregatingSimilarUsers + ", scoreThreshold="
                + scoreThreshold + ", confidenceThreshold=" + confidenceThreshold
                + ", userSimilaritySimType=" + userSimilaritySimType
                + ", userModelBasedSimilarityConfiguration="
                + userModelBasedSimilarityConfiguration + ", messageGroupSimilarityConfiguration="
                + messageGroupSimilarityConfiguration + ", adaptFromUsers=" + adaptFromUsers
                + ", intervallOfUserSimComputationInDays=" + intervallOfUserSimComputationInDays
                + ", votingAggregatorNecassaryVotes=" + votingAggregatorNecassaryVotes
                + ", votingAggregatorStrict=" + votingAggregatorStrict
                + ", votingAggregatorStrictThreshold=" + votingAggregatorStrictThreshold
                + ", onlyUseAdaptedTermsForRescoring=" + onlyUseAdaptedTermsForRescoring + "]";
    }

}
