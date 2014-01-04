package de.spektrumprojekt.i.term.similarity;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import de.spektrumprojekt.commons.time.TimeProviderHolder;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.i.term.weight.TermWeightComputer;
import de.spektrumprojekt.i.timebased.MergeValuesStrategy;

public class CosinusTermVectorSimilarityComputer extends TermWeightTermVectorSimilarityComputer {

    private TimeDecayFunction timeDecayFunction = null; // TimeDecayFunction.createWithDayCutOff();

    public CosinusTermVectorSimilarityComputer(TermWeightComputer termWeightComputer,
            boolean treatMissingUserModelEntriesAsZero) {
        this(termWeightComputer, null, treatMissingUserModelEntriesAsZero);
    }

    public CosinusTermVectorSimilarityComputer(
            TermWeightComputer termWeightComputer,
            TimeDecayFunction timeDecayFunction,
            boolean treatMissingUserModelEntriesAsZero) {
        super(termWeightComputer, treatMissingUserModelEntriesAsZero);
        this.timeDecayFunction = timeDecayFunction;
    }

    @Override
    public String getConfigurationDescription() {
        return super.getConfigurationDescription() + " " + this.toString();
    }

    private float getEntryWeight(UserModelEntry entry) {
        if (timeDecayFunction == null) {
            return entry.getScoredTerm().getWeight();
        }

        double decay = timeDecayFunction.getDecay(entry.getLastChange().getTime(),
                TimeProviderHolder.DEFAULT.getCurrentTime());
        float sum = entry.getScoreSum() == 0 ? 1 : entry.getScoreSum();
        return Math.max(0, Math.min(1, (float) decay * sum * entry.getScoredTerm().getWeight()));
    }

    @Override
    public float getSimilarity(Map<Term, UserModelEntry> relevantEntries1,
            Map<Term, UserModelEntry> relevantEntries2) {
        float sumTop = 0;
        float squareSum1 = 0;
        float squareSum2 = 0;

        for (Term term : relevantEntries1.keySet()) {
            UserModelEntry entry1 = relevantEntries1.get(term);
            UserModelEntry entry2 = relevantEntries2.get(term);

            if (entry2 == null) {
                continue;
            }
            float entryScore1 = getEntryWeight(entry1);
            float entryScore2 = getEntryWeight(entry2);

            sumTop += entryScore1 * entryScore2;
            squareSum1 += entryScore1 * entryScore1;
            squareSum2 += entryScore2 * entryScore2;
        }

        if (squareSum1 * squareSum2 == 0) {
            return 0;
        }
        return (float) (sumTop / Math.sqrt(squareSum1 * squareSum2));
    }

    @Override
    public Float getSimilarity(String messageGroupId,
            Map<String, Map<Term, UserModelEntry>> allEntries, MergeValuesStrategy strategy,
            Collection<Term> terms) {
        float sumTop = 0;
        float squareSum1 = 0;
        float squareSum2 = 0;
        Collection<Term> termsForIteration;
        if (isTreatMissingUserModelEntriesAsZero()) {
            termsForIteration = terms;
        } else {
            termsForIteration = new HashSet<Term>();
            for (Map<Term, UserModelEntry> userModelEntries : allEntries.values()) {
                termsForIteration.addAll(userModelEntries.keySet());
            }
        }
        for (Term term : termsForIteration) {
            Map<String, Float> entryScores = new HashMap<String, Float>();
            for (String userModelType : allEntries.keySet()) {
                UserModelEntry entry = allEntries.get(userModelType).get(term);
                if (entry != null) {
                    entryScores.put(userModelType, getEntryWeight(entry));
                } else if (isTreatMissingUserModelEntriesAsZero()) {
                    entryScores.put(userModelType, 0f);
                }
            }
            float entryScore = strategy.merge(entryScores);
            // // UserModelEntry entry = relevantEntries.get(term);
            // // float entryScore = 0;
            // if (entry != null) {
            // entryScore = entry.getScoredTerm().getWeight();
            // }
            float termWeight = getTermWeightComputer().determineTermWeight(messageGroupId, term);

            sumTop += termWeight * entryScore;
            squareSum1 += entryScore * entryScore;
            squareSum2 += termWeight * termWeight;
        }

        if (squareSum1 * squareSum2 == 0) {
            return 0f;
        }
        return (float) (sumTop / Math.sqrt(squareSum1 * squareSum2));
    }

    @Override
    public float getSimilarity(String messageGroupId, Map<Term, UserModelEntry> relevantEntries,
            Collection<Term> terms) {
        float sumTop = 0;
        float squareSum1 = 0;
        float squareSum2 = 0;
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
                entryScore = getEntryWeight(entry);
            }
            float termWeight = getTermWeightComputer().determineTermWeight(messageGroupId, term);

            sumTop += termWeight * entryScore;
            squareSum1 += entryScore * entryScore;
            squareSum2 += termWeight * termWeight;
        }

        if (squareSum1 * squareSum2 == 0) {
            return 0;
        }
        return (float) (sumTop / Math.sqrt(squareSum1 * squareSum2));
    }

    @Override
    public String toString() {
        return "CosinusTermVectorSimilarityComputer [timeDecayFunction=" + timeDecayFunction + "]";
    }

}
