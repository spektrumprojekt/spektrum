package de.spektrumprojekt.i.evaluation.runner.processor;

import de.spektrumprojekt.commons.task.TaskRunner;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.i.evaluation.runner.Evaluator;
import de.spektrumprojekt.i.evaluation.runner.configuration.EvaluationExecuterConfiguration;
import de.spektrumprojekt.i.evaluation.runner.configuration.EvaluatorConfiguration;
import de.spektrumprojekt.i.learner.Learner;
import de.spektrumprojekt.i.ranker.Scorer;
import de.spektrumprojekt.persistence.Persistence;

public interface EvaluationExecutionProvider {

    void addConfig(String config);

    Communicator getCommunicator();

    EvaluationExecuterConfiguration getEvaluationExecuterConfiguration();

    EvaluationProcessorChain getEvaluationProcessorChain();

    Evaluator getEvaluator();

    EvaluatorConfiguration getEvaluatorConfiguration();

    String getEvaluatorResultFilename();

    Learner getLearner();

    Persistence getPersistence();

    Scorer getScorer();

    TaskRunner getTaskRunner();
}