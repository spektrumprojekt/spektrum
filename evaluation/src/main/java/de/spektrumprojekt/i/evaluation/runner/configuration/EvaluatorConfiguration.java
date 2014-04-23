package de.spektrumprojekt.i.evaluation.runner.configuration;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.datamodel.message.InteractionLevel;

public class EvaluatorConfiguration implements ConfigurationDescriptable {

    public static enum TestRatingsEvalTyp {
        /**
         * Eval the test ratings directly after the message has been ranked.
         */
        IMMEDIATE,
        /**
         * Eval the test ratings one time a day for the messages of the last day.
         */
        EVERY_DAY,
        /**
         * Eval the test ratings one time a week for the messages of the last week.
         */
        EVERY_WEEK,
        /**
         * Eval the test ratings one time a month for the messages of the last month.
         */
        EVERY_MONTH,
        /**
         * Eval the test ratings at the end after all messages have been ranked.
         */
        AT_THE_END;
    }

    private TestRatingsEvalTyp testRatingsEvalTyp = TestRatingsEvalTyp.IMMEDIATE;

    private float trainingRatingSetRatio;

    private float letTheGoalBePositive;

    private float letTheComputedBePositive;
    private boolean rerankAtTheEnd;

    private boolean onlyEvaluateRoot;
    private boolean evaluateAgainstTraining;
    private boolean onlyUseUsersWithRatings;
    private boolean cutoffRanksWithSameRelevanceScore = true;

    private int crossValidationPartitionSize = 10;
    /**
     * Defines the interaction levels a message rank must have to be used for interaction level.
     * Typically the following is useful:
     * 
     * null or ALL => all message ranks with test ratings are used<br>
     * NONE => only ranks with no user interaction are used for evaluation<br>
     * NONE,INDIRECT => only ranks with no or indirect interaction are used
     * 
     * <br>
     * NONE,DIRECT => not useful
     */
    private InteractionLevel[] messageRankInteractionLevelsToInclude;

    private boolean crossValidationSwitchTrainingTestPartitions;

    private Collection<String> userIdsToUseForRating = null;

    private Integer topNRatersToUse;

    private boolean topRankMeasureOnlyUseDataPointsAvailableForEvaluation;

    private boolean useCrossValidationWithUserPartitions;

    private boolean useCrossValidation;

    private boolean analyzeTerms;

    private boolean useLikesForLearning;

    public EvaluatorConfiguration() {
    }

    public void addOnlyUseUserIdsForRatings(String userId) {
        if (userIdsToUseForRating == null) {
            this.userIdsToUseForRating = new HashSet<String>();
        }
        this.userIdsToUseForRating.add(userId);
    }

    public String getConfigurationDescription() {
        return getClass().getSimpleName()
                + "trainingRatingSetRatio: " + trainingRatingSetRatio
                + " letTheGoalBePositive: " + letTheGoalBePositive
                + " letTheComputedBePositive: " + letTheComputedBePositive
                + " testRatingsEvalTyp: " + testRatingsEvalTyp
                + " rerankAtTheEnd: " + rerankAtTheEnd
                + " onlyEvaluateRoot: " + onlyEvaluateRoot
                + " evaluateAgainstTraining: " + evaluateAgainstTraining
                + " onlyUseUsersWithRatings: " + onlyUseUsersWithRatings
                + " crossValidationPartitionSize: " + crossValidationPartitionSize
                + " messageRankInteractionLevelsToInclude: "
                + Arrays.toString(messageRankInteractionLevelsToInclude)
                + " crossValidationSwitchTrainingTestPartitions: "
                + crossValidationSwitchTrainingTestPartitions
                + " userIdsToUseForRating: " + userIdsToUseForRating
                + " topRankMeasureOnlyUseDataPointsAvailableForEvaluation: "
                + topRankMeasureOnlyUseDataPointsAvailableForEvaluation
                + " topNRatersToUse: " + topNRatersToUse
                + " useLikesForLearning: " + useLikesForLearning;
    }

    public int getCrossValidationPartitionSize() {
        return crossValidationPartitionSize;
    }

    public float getLetTheComputedBePositive() {
        return letTheComputedBePositive;
    }

    public float getLetTheGoalBePositive() {
        return letTheGoalBePositive;
    }

    public InteractionLevel[] getMessageRankInteractionLevelsToInclude() {
        return messageRankInteractionLevelsToInclude;
    }

    public TestRatingsEvalTyp getTestRatingsEvalTyp() {
        return testRatingsEvalTyp;
    }

    public Integer getTopNRatersToUse() {
        return topNRatersToUse;
    }

    public float getTrainingRatingSetRatio() {
        return trainingRatingSetRatio;
    }

    public Collection<String> getUserIdsToUseForRating() {
        return userIdsToUseForRating;
    }

    public boolean isAnalyzeTerms() {
        return analyzeTerms;
    }

    public boolean isCrossValidationSwitchTrainingTestPartitions() {
        return crossValidationSwitchTrainingTestPartitions;
    }

    /**
     * if true ordering becomes a random factor. If e.g. the top 20 elements have all the same score
     * of 0.5 and we want the top 10 when with this variable true it will pick the some of the 10
     * based on the ordering the JVM will do it. With this variable to false all 20 will be picked,
     * even if we have a top 10 config.
     * 
     * however, in TopRankMessageComputer the sorting will be reproducable if
     * cutoffRanksWithSameRelevanceScore is set to true based on the message id
     * 
     * @return
     */
    public boolean isCutoffRanksWithSameRelevanceScore() {
        return cutoffRanksWithSameRelevanceScore;
    }

    public boolean isEvaluateAgainstTraining() {
        return evaluateAgainstTraining;
    }

    public boolean isEvaluateAtTheEnd() {
        return testRatingsEvalTyp.equals(TestRatingsEvalTyp.AT_THE_END);
    }

    public boolean isOnlyEvaluateRoot() {
        return onlyEvaluateRoot;
    }

    public boolean isOnlyUseUsersWithRatings() {
        return onlyUseUsersWithRatings;
    }

    public boolean isRerankAtTheEnd() {
        return rerankAtTheEnd;
    }

    /**
     * If true only the ranks in the evaluation dataset will be used for top ranking instead of
     * using all computed ranks (that is all messages) and then filter them within the top
     * 
     * @return
     */
    public boolean isTopRankMeasureOnlyUseDataPointsAvailableForEvaluation() {
        return topRankMeasureOnlyUseDataPointsAvailableForEvaluation;
    }

    public boolean isUseCrossValidation() {
        return useCrossValidation;
    }

    public boolean isUseCrossValidationWithUserPartitions() {
        return useCrossValidationWithUserPartitions;
    }

    public boolean isUseLikesForLearning() {
        return useLikesForLearning;
    }

    public boolean isValidMessageRankInteractionLevel(InteractionLevel interactionLevel) {
        if (this.messageRankInteractionLevelsToInclude == null) {
            return true;
        }
        for (InteractionLevel iLevel : this.messageRankInteractionLevelsToInclude) {
            if (iLevel.equals(interactionLevel)) {
                return true;
            }
        }
        if (InteractionLevel.UNKNOWN.equals(interactionLevel)) {
            throw new IllegalStateException("UNKNOWN interaction level not allowed here.");
        }
        return false;
    }

    public boolean isValidUserIdToUseForRating(String userId) {
        if (this.userIdsToUseForRating == null || this.userIdsToUseForRating.isEmpty()) {
            return true;
        }
        return this.userIdsToUseForRating.contains(userId);
    }

    public void setAnalyzeTerms(boolean analyzeTerms) {
        this.analyzeTerms = analyzeTerms;
    }

    public void setCrossValidationPartitionSize(int crossValidationPartitionSize) {
        this.crossValidationPartitionSize = crossValidationPartitionSize;
    }

    public void setCrossValidationSwitchTrainingTestPartitions(
            boolean crossValidationSwitchTrainingTestPartitions) {
        this.crossValidationSwitchTrainingTestPartitions = crossValidationSwitchTrainingTestPartitions;
    }

    public void setCutoffRanksWithSameRelevanceScore(boolean cutoffRanksWithSameRelevanceScore) {
        this.cutoffRanksWithSameRelevanceScore = cutoffRanksWithSameRelevanceScore;
    }

    public void setEvaluateAgainstTraining(boolean evaluateAgainstTraining) {
        this.evaluateAgainstTraining = evaluateAgainstTraining;
    }

    public void setEvaluateAtTheEnd(boolean evaluateAtTheEnd) {
        if (evaluateAtTheEnd) {
            this.testRatingsEvalTyp = TestRatingsEvalTyp.AT_THE_END;
        } else {
            this.testRatingsEvalTyp = TestRatingsEvalTyp.IMMEDIATE;
        }
    }

    public void setLetTheComputedBePositive(float letTheComputedBePositive) {
        this.letTheComputedBePositive = letTheComputedBePositive;
    }

    public void setLetTheGoalBePositive(float letTheGoalBePositive) {
        this.letTheGoalBePositive = letTheGoalBePositive;
    }

    public void setMessageRankInteractionLevelsToInclude(
            InteractionLevel... messageRankInteractionLevelsToInclude) {
        this.messageRankInteractionLevelsToInclude = messageRankInteractionLevelsToInclude;
    }

    public void setOnlyEvaluateRoot(boolean onlyEvaluateRoot) {
        this.onlyEvaluateRoot = onlyEvaluateRoot;
    }

    public void setOnlyUseUsersWithRatings(boolean onlyUseUsersWithRatings) {
        this.onlyUseUsersWithRatings = onlyUseUsersWithRatings;
    }

    public void setRerankAtTheEnd(boolean rerankAtTheEnd) {
        this.rerankAtTheEnd = rerankAtTheEnd;
    }

    public void setTestRatingsEvalTyp(TestRatingsEvalTyp testRatingsEvalTyp) {
        if (testRatingsEvalTyp == null) {
            throw new IllegalArgumentException("testRatingsEvalTyp cannot be null.");
        }
        this.testRatingsEvalTyp = testRatingsEvalTyp;
    }

    public void setTopNRatersToUse(Integer topNRatersToUse) {
        this.topNRatersToUse = topNRatersToUse;
    }

    public void setTopRankMeasureOnlyUseDataPointsAvailableForEvaluation(
            boolean topRankMeasureOnlyUseDataPointsAvailableForEvaluation) {
        this.topRankMeasureOnlyUseDataPointsAvailableForEvaluation = topRankMeasureOnlyUseDataPointsAvailableForEvaluation;
    }

    public void setTrainingRatingSetRatio(float trainingRatingSetRatio) {
        this.trainingRatingSetRatio = trainingRatingSetRatio;
    }

    public void setUseCrossValidation(boolean useCrossValidation) {
        this.useCrossValidation = useCrossValidation;
    }

    public void setUseCrossValidationWithUserPartitions(boolean useCrossValidationWithUserPartitions) {
        this.useCrossValidationWithUserPartitions = useCrossValidationWithUserPartitions;
    }

    public void setUseLikesForLearning(boolean useLikesForLearning) {
        this.useLikesForLearning = useLikesForLearning;
    }

    public void setUserIdsToUseForRating(Collection<String> userIdsToUseForRating) {
        this.userIdsToUseForRating = userIdsToUseForRating;
    }

    @Override
    public String toString() {
        return "EvaluatorConfiguration [testRatingsEvalTyp=" + testRatingsEvalTyp
                + ", trainingRatingSetRatio=" + trainingRatingSetRatio + ", letTheGoalBePositive="
                + letTheGoalBePositive + ", letTheComputedBePositive=" + letTheComputedBePositive
                + ", rerankAtTheEnd=" + rerankAtTheEnd + ", onlyEvaluateRoot=" + onlyEvaluateRoot
                + ", evaluateAgainstTraining=" + evaluateAgainstTraining
                + ", onlyUseUsersWithRatings=" + onlyUseUsersWithRatings
                + ", cutoffRanksWithSameRelevanceScore=" + cutoffRanksWithSameRelevanceScore
                + ", crossValidationPartitionSize=" + crossValidationPartitionSize
                + ", messageRankInteractionLevelsToInclude="
                + Arrays.toString(messageRankInteractionLevelsToInclude)
                + ", crossValidationSwitchTrainingTestPartitions="
                + crossValidationSwitchTrainingTestPartitions + ", userIdsToUseForRating="
                + userIdsToUseForRating + ", topNRatersToUse=" + topNRatersToUse
                + ", topRankMeasureOnlyUseDataPointsAvailableForEvaluation="
                + topRankMeasureOnlyUseDataPointsAvailableForEvaluation
                + ", useCrossValidationWithUserPartitions=" + useCrossValidationWithUserPartitions
                + ", useCrossValidation=" + useCrossValidation + ", analyzeTerms=" + analyzeTerms
                + ", useLikesForLearning=" + useLikesForLearning + "]";
    }

    public void validate() {
        if (this.useCrossValidation) {
            if (this.crossValidationPartitionSize <= 0) {
                throw new IllegalStateException("crossValidationPartitionSize must >= 0 but is "
                        + crossValidationPartitionSize);
            }
        }
    }

}