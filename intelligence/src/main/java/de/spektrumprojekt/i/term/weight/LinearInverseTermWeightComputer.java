package de.spektrumprojekt.i.term.weight;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.i.term.frequency.TermFrequencyComputer;

public class LinearInverseTermWeightComputer implements TermWeightComputer {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(LinearInverseTermWeightComputer.class);

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
                LOGGER.warn("term.count is 0. term: " + term);
                return 0;
            }
            if (numMessageWithTerm == 0) {
                LOGGER.warn("numMessageWithTerm2 is 0. term: " + term);
                return 0;
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
