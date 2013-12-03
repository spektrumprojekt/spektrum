package de.spektrumprojekt.i.learner;

import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.i.ranker.UserModelConfiguration;

public class IncrementalUserModelIntegrationStrategy extends
        TermCountUserModelEntryIntegrationStrategy {

    private float learningFactorAlpha = 0.25f;
    private float thresholdOfNeutral = 0.5f;
    private boolean useCurrentUserModelEntryValueAsThreshold = true;

    public IncrementalUserModelIntegrationStrategy(UserModelConfiguration userModelConfiguration) {
        learningFactorAlpha = userModelConfiguration.getIncrementalLearningFactorAlpha();
        thresholdOfNeutral = userModelConfiguration.getIncrementalLearningFactorAlpha();
        useCurrentUserModelEntryValueAsThreshold = userModelConfiguration
                .isIncrementalLUseCurrentUserModelEntryValueAsThreshold();
    }

    private float decrement(float currentValue, float incrementWeight) {
        float newValue = currentValue - learningFactorAlpha * currentValue * incrementWeight;
        return newValue;
    }

    @Override
    public String getConfigurationDescription() {
        return super.getConfigurationDescription()
                + " learningFactorAlpha: " + learningFactorAlpha
                + " thresholdOfNeutral: " + thresholdOfNeutral
                + " useCurrentUserModelEntryValueAsThreshold:  "
                + useCurrentUserModelEntryValueAsThreshold;
    }

    private float increment(float currentValue, float incrementWeight) {
        float newValue = currentValue + learningFactorAlpha * (1 - currentValue) * incrementWeight;
        return newValue;
    }

    @Override
    protected boolean updateEntry(UserModelEntry entry, float interestScore, ScoredTerm scoredTerm) {
        if (scoredTerm.getWeight() >= getMinScore()) {
            // inverse means to disintegrate a value, that is learn it "back"
            boolean inverse = interestScore < 0;
            // the uninversed interest weight
            float interestWeight;

            if (!inverse) {
                entry.setScoreCount(entry.getScoreCount() + 1);
                entry.setAdapted(false);
                interestWeight = interestScore;
            } else {
                entry.setScoreCount(entry.getScoreCount() - 1);
                interestWeight = -interestScore;
            }
            entry.setScoreSum(entry.getScoreSum() + interestScore);

            float currentValue = entry.getScoredTerm().getWeight();
            float newValue;

            // determine if the term weight should be increased or decreased
            boolean increment = this.useCurrentUserModelEntryValueAsThreshold
                    && interestWeight >= currentValue;
            increment = increment || !this.useCurrentUserModelEntryValueAsThreshold
                    && interestWeight >= this.thresholdOfNeutral;

            // if inverse do the opposite of the normal increment / decrement to learn it back
            // if the interestWeight is 1.0 with inverse it means that in the past this weight has
            // been learned and used to increment, and now to learn back it should be used for
            // decrementation
            if (inverse) {
                increment = !increment;
            }

            if (increment) {
                newValue = this.increment(currentValue, interestWeight);
            } else {
                newValue = this.decrement(currentValue, interestWeight);
            }

            entry.getScoredTerm().setWeight(newValue);

        }
        return !(entry.getScoreCount() > 0);

    }

}
