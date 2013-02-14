package de.spektrumprojekt.i.term.similarity;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.i.term.weight.TermWeightComputer;

public class MaximumTermVectorSimilarityComputer extends TermWeightTermVectorSimilarityComputer {

    public MaximumTermVectorSimilarityComputer(TermWeightComputer termWeightComputer) {
        super(termWeightComputer);
    }

    @Override
    public float getSimilarity(String messageGroupId, Map<Term, UserModelEntry> relevantEntries,
            Collection<Term> terms) {
        float max = 0;
        for (Entry<Term, UserModelEntry> entry : relevantEntries.entrySet()) {
            float termWeight = getTermWeightComputer().determineTermWeight(messageGroupId,
                    entry.getKey());

            float entryScore = 0;
            if (entry.getValue() != null) {
                entryScore = entry.getValue().getScoredTerm().getWeight();
            }

            max = Math.max(max, termWeight * entryScore);

        }
        return max;
    }
}
