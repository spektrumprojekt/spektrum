package de.spektrumprojekt.i.term.weight;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.i.term.frequency.TermFrequencyComputer;

public class LogInverseTermWeightComputer implements TermWeightComputer {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(LogInverseTermWeightComputer.class);

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

        float numMessageInGroup = termFrequencyComputer.getMessageCount(messageGroupId);

        if (failOnZeroTermCounts) {
            if (term.getCount() == 0) {
                LOGGER.warn("term.count is 0. term: " + term);
                return 0;
            }
            if (numMessageInGroup == 0) {
                LOGGER.warn("numMessageWithTerm2 is 0. term: " + term);
                return 0;
            }
        }

        float log = term.getCount() == 0 ? 0 : numMessageInGroup / term.getCount();
        float weight = log == 0 ? 0 : (float) Math.log(log);
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
