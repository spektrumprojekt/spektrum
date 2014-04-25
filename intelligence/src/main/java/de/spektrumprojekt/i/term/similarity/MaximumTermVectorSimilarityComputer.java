package de.spektrumprojekt.i.term.similarity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.i.term.weight.TermWeightComputer;
import de.spektrumprojekt.i.timebased.MergeValuesStrategy;

public class MaximumTermVectorSimilarityComputer extends TermWeightTermVectorSimilarityComputer {

    public MaximumTermVectorSimilarityComputer(TermWeightComputer termWeightComputer) {
        super(termWeightComputer, false);
    }

    @Override
    public float getSimilarity(Map<Term, UserModelEntry> relevantEntries1,
            Map<Term, UserModelEntry> relevantEntries2) {
        float max = 0;

        for (Term term : relevantEntries1.keySet()) {
            UserModelEntry entry1 = relevantEntries1.get(term);
            UserModelEntry entry2 = relevantEntries2.get(term);

            if (entry2 == null) {
                continue;
            }
            float entryScore1 = entry1.getScoredTerm().getWeight();
            float entryScore2 = entry2.getScoredTerm().getWeight();

            max = Math.max(max, entryScore1 * entryScore2);
        }
        return max;

    }

    @Override
    public Float getSimilarity(String messageGroupId,
            Map<String, Map<Term, UserModelEntry>> allEntries, MergeValuesStrategy strategy,
            Collection<Term> terms) {
        float max = 0;
        for (Term term : terms) {
            float termWeight = getTermWeightComputer().determineTermWeight(messageGroupId, term);
            Map<String, Float> entryScores = new HashMap<String, Float>();
            for (String userModelType : allEntries.keySet()) {
                UserModelEntry modelEntry = allEntries.get(userModelType).get(term);
                if (modelEntry != null) {
                    entryScores.put(userModelType, modelEntry.getScoredTerm().getWeight());
                }
            }
            float entryScore = strategy.merge(entryScores);

            max = Math.max(max, termWeight * entryScore);

        }
        return max;
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
