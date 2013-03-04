package de.spektrumprojekt.i.term.similarity;

import java.util.Collection;
import java.util.Map;

import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.i.term.weight.TermWeightComputer;

public class AverageTermVectorSimilarityComputer extends TermWeightTermVectorSimilarityComputer {

    public AverageTermVectorSimilarityComputer(TermWeightComputer termWeightComputer,
            boolean treatMissingUserModelEntriesAsZero) {
        super(termWeightComputer, treatMissingUserModelEntriesAsZero);
    }

    @Override
    public float getSimilarity(String messageGroupId, Map<Term, UserModelEntry> relevantEntries,
            Collection<Term> terms) {
        float sumTop = 0;
        float sumBottom = 0;
        Collection<Term> termsForIteration;
        if (isTreatMissingUserModelEntriesAsZero()) {
            termsForIteration = terms;
        } else {
            termsForIteration = relevantEntries.keySet();
        }
        for (Term term : termsForIteration) {
            UserModelEntry entry = relevantEntries.get(term);
            float entryScore = 0;
            if (entry != null) {
                entryScore = entry.getScoredTerm().getWeight();
            }
            float termWeight = getTermWeightComputer().determineTermWeight(messageGroupId, term);

            sumTop += termWeight * entryScore;
            sumBottom += termWeight;
        }

        return sumTop / sumBottom;
    }

}
