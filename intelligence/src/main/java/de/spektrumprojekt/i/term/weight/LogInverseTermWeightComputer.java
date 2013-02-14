package de.spektrumprojekt.i.term.weight;

import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.i.term.frequency.TermFrequencyComputer;

public class LogInverseTermWeightComputer implements TermWeightComputer {

    private final TermFrequencyComputer termFrequencyComputer;
    private boolean failOnZeroTermCounts = true;

    public LogInverseTermWeightComputer(TermFrequencyComputer termFrequencyComputer) {
        this.termFrequencyComputer = termFrequencyComputer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float determineTermWeight(String messageGroupId, Term term) {

        float numMessageWithTerm2 = termFrequencyComputer.getMessageCount(messageGroupId);

        if (failOnZeroTermCounts) {
            if (term.getCount() == 0) {
                throw new RuntimeException("No! term.count cannot be 0! " + term);
            }
            if (numMessageWithTerm2 == 0) {
                throw new RuntimeException("No! numMessageWithTerm cannot be 0! " + term);
            }
        }
        float log = 1 + numMessageWithTerm2 / term.getCount();
        float weight = (float) Math.log(log);
        return weight;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    public boolean isFailOnZeroTermCounts() {
        return failOnZeroTermCounts;
    }

    public void setFailOnZeroTermCounts(boolean failOnZeroTermCounts) {
        this.failOnZeroTermCounts = failOnZeroTermCounts;
    }

}
