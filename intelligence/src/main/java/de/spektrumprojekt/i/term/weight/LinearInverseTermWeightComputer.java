package de.spektrumprojekt.i.term.weight;

import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.i.term.frequency.TermFrequencyComputer;

public class LinearInverseTermWeightComputer implements TermWeightComputer {
    private final TermFrequencyComputer termFrequencyComputer;
    private boolean failOnZeroTermCounts = true;

    public LinearInverseTermWeightComputer(TermFrequencyComputer termFrequencyComputer) {
        this.termFrequencyComputer = termFrequencyComputer;
    }

    @Override
    public float determineTermWeight(String messageGroupId, Term term) {
        float numMessageWithTerm = termFrequencyComputer.getMessageCount(messageGroupId);
        if (failOnZeroTermCounts) {
            if (term.getCount() == 0) {
                throw new RuntimeException("No! term.count cannot be 0! " + term);
            } else if (numMessageWithTerm == 0) {
                throw new RuntimeException("No! numMessageWithTerm cannot be 0! " + term);
            }
        }
        float weight = 1 - term.getCount() / numMessageWithTerm;

        return weight;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

}
