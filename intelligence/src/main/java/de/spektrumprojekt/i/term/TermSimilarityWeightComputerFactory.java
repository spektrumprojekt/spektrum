package de.spektrumprojekt.i.term;

import de.spektrumprojekt.i.ranker.RankerConfiguration;
import de.spektrumprojekt.i.term.frequency.TermFrequencyComputer;
import de.spektrumprojekt.i.term.similarity.AverageTermVectorSimilarityComputer;
import de.spektrumprojekt.i.term.similarity.CosinusTermVectorSimilarityComputer;
import de.spektrumprojekt.i.term.similarity.MaximumTermVectorSimilarityComputer;
import de.spektrumprojekt.i.term.similarity.TermVectorSimilarityComputer;
import de.spektrumprojekt.i.term.weight.LinearInverseTermWeightComputer;
import de.spektrumprojekt.i.term.weight.LogInverseTermWeightComputer;
import de.spektrumprojekt.i.term.weight.TermWeightComputer;
import de.spektrumprojekt.i.term.weight.TrivalTermWeightComputer;

public class TermSimilarityWeightComputerFactory {

    private static final TermSimilarityWeightComputerFactory INSTANCE = new TermSimilarityWeightComputerFactory();

    public static TermSimilarityWeightComputerFactory getInstance() {
        return INSTANCE;
    }

    private TermSimilarityWeightComputerFactory() {
    }

    public TermVectorSimilarityComputer createTermVectorSimilarityComputer(
            RankerConfiguration rankerConfiguration, TermFrequencyComputer termFrequencyComputer) {
        TermVectorSimilarityStrategy similarityStrategy = rankerConfiguration
                .getTermVectorSimilarityStrategy();
        TermWeightStrategy termWeightStrategy = rankerConfiguration.getTermWeightStrategy();
        boolean treatMissingUserModelEntriesAsZero = rankerConfiguration
                .isTreatMissingUserModelEntriesAsZero();

        TermWeightComputer termWeightComputer = this.createTermWeightComputer(termWeightStrategy,
                termFrequencyComputer);
        TermVectorSimilarityComputer termVectorSimilarityComputer = null;
        switch (similarityStrategy) {
        case AVG:
            termVectorSimilarityComputer = new AverageTermVectorSimilarityComputer(
                    termWeightComputer, treatMissingUserModelEntriesAsZero);
            break;
        case MAX:
            termVectorSimilarityComputer = new MaximumTermVectorSimilarityComputer(
                    termWeightComputer);
            break;
        case COSINUS:
            termVectorSimilarityComputer = new CosinusTermVectorSimilarityComputer(
                    termWeightComputer, treatMissingUserModelEntriesAsZero);
            break;
        }
        return termVectorSimilarityComputer;
    }

    public TermVectorSimilarityComputer createTermVectorSimilarityComputer(
            TermVectorSimilarityStrategy similarityStrategy, TermWeightStrategy termWeightStrategy,
            TermFrequencyComputer termFrequencyComputer, boolean treatMissingUserModelEntriesAsZero) {
        if (similarityStrategy == null) {
            throw new IllegalArgumentException("similarityStrategy cannot be null");
        }
        if (termWeightStrategy == null) {
            throw new IllegalArgumentException("termWeightStrategy cannot be null");
        }
        RankerConfiguration rankerConfiguration = new RankerConfiguration(termWeightStrategy,
                similarityStrategy);
        rankerConfiguration
                .setTreatMissingUserModelEntriesAsZero(treatMissingUserModelEntriesAsZero);

        return this.createTermVectorSimilarityComputer(rankerConfiguration, termFrequencyComputer);
    }

    public TermWeightComputer createTermWeightComputer(TermWeightStrategy termWeightStrategy,
            TermFrequencyComputer termFrequencyComputer) {
        if (termWeightStrategy == null) {
            throw new IllegalArgumentException("termWeightStrategy cannot be null");
        }
        TermWeightComputer termWeightComputer = null;
        switch (termWeightStrategy) {
        case TRIVIAL:
            termWeightComputer = new TrivalTermWeightComputer();
            break;
        case INVERSE_TERM_FREQUENCY:
            termWeightComputer = new LogInverseTermWeightComputer(termFrequencyComputer);
            break;
        case LINEAR_INVERSE_TERM_FREQUENCY:
            termWeightComputer = new LinearInverseTermWeightComputer(termFrequencyComputer);
            break;
        default:
            throw new IllegalArgumentException(termWeightStrategy + " is an unhandled value!");
        }
        return termWeightComputer;
    }

}
