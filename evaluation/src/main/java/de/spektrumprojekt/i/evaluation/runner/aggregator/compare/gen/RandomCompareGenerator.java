package de.spektrumprojekt.i.evaluation.runner.aggregator.compare.gen;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import de.spektrumprojekt.i.evaluation.measure.MeasureOutput;
import de.spektrumprojekt.i.evaluation.runner.aggregator.compare.EvaluationRunComparerDefinition;

public class RandomCompareGenerator extends AbstractEvaluationRunComparerDefinitionGenerator {

    public RandomCompareGenerator(String evaluationResultsPath) {
        super(evaluationResultsPath);
    }

    private EvaluationRunComparerDefinition createDefinition1(String top) {

        EvaluationRunComparerDefinition definition = new EvaluationRunComparerDefinition(
                MeasureOutput.TIME_BIN_MEAN_AVERAGE_PRECISION,
                "random_compare-" + top,
                "Random Ranker with different threshold for relevant ranking"
                        + getTopReadableString(top));
        definition.setSeriesLabel("Average Precision");
        definition.setXlabel("Threshold to rank positive");

        findAndSetFiles(definition, getConfigNames(top));

        return definition;
    }

    public Collection<EvaluationRunComparerDefinition> createDefinitions() {
        Collection<EvaluationRunComparerDefinition> definitions = new HashSet<EvaluationRunComparerDefinition>();

        definitions.add(createDefinition1("top50Week"));
        definitions.add(createDefinition1("top100Week"));
        definitions.add(createDefinition1("top25Day"));
        definitions.add(createDefinition1("top10Day"));

        return definitions;
    }

    private Map<String, String[]> getConfigNames(String top) {
        final String topMatch = top + ".ranks.eval-0.9.pr";
        // the config names we want
        Map<String, String[]> configNames = new LinkedHashMap<String, String[]>();

        for (int randomRankThreshold = 0; randomRankThreshold < 10; randomRankThreshold++) {
            String shortName = randomRankThreshold == 0 ? "Random Value" : randomRankThreshold
                    / 10d + "";

            String name = "test_random_ranker_onlyNoneILs_rndTh" + randomRankThreshold;
            configNames.put(shortName, new String[] { name, topMatch });
        }
        return configNames;
    }

}
