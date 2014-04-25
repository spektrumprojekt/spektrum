package de.spektrumprojekt.i.evaluation.runner.aggregator.compare.gen;

import java.util.Collection;

import de.spektrumprojekt.i.evaluation.runner.aggregator.compare.EvaluationRunComparerDefinition;

public interface EvaluationRunComparerDefinitionGenerator {

    public Collection<EvaluationRunComparerDefinition> createDefinitions();

    public int getWarnings();
}
