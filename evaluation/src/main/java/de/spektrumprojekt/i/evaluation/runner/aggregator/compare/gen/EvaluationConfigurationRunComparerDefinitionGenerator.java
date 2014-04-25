package de.spektrumprojekt.i.evaluation.runner.aggregator.compare.gen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.spektrumprojekt.i.evaluation.measure.MeasureOutput;
import de.spektrumprojekt.i.evaluation.runner.aggregator.compare.EvaluationRunComparerDefinition;
import de.spektrumprojekt.i.evaluation.runner.configuration.EvaluationExecuterConfiguration;

public class EvaluationConfigurationRunComparerDefinitionGenerator extends
        AbstractEvaluationRunComparerDefinitionGenerator {

    private final List<EvaluationExecuterConfiguration> evaluationExecuterConfigurations;

    public EvaluationConfigurationRunComparerDefinitionGenerator(String evaluationResultsPath,
            Collection<EvaluationExecuterConfiguration> evaluationExecuterConfigurations) {
        super(evaluationResultsPath);
        this.evaluationExecuterConfigurations = new ArrayList<EvaluationExecuterConfiguration>();
        if (evaluationExecuterConfigurations != null) {
            this.evaluationExecuterConfigurations.addAll(evaluationExecuterConfigurations);
        }
    }

    public Collection<EvaluationRunComparerDefinition> createDefinitions() {
        List<EvaluationRunComparerDefinition> definitions = new ArrayList<EvaluationRunComparerDefinition>();

        for (TopTimeBins top : TopTimeBins.values()) {
            EvaluationRunComparerDefinition definition = new EvaluationRunComparerDefinition(
                    MeasureOutput.TIME_BIN_MEAN_AVERAGE_PRECISION,
                    "latest-compare-" + top.name(),
                    "Comparing the configurations of the latest run for " + top.name());
            this.findAndSetFiles(definition, getConfigNames(top));

            definitions.add(definition);
        }

        return definitions;
    }

    private Map<String, String[]> getConfigNames(TopTimeBins top) {
        String topName = top.name() + ".ranks.eval-0.9.pr";
        Map<String, String[]> configNames = new LinkedHashMap<String, String[]>();

        for (EvaluationExecuterConfiguration conf : this.evaluationExecuterConfigurations) {
            configNames.put(conf.getName(), new String[] { conf.getName(), topName });
        }
        return configNames;
    }

}
