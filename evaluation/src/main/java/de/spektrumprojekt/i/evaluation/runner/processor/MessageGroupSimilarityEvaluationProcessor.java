package de.spektrumprojekt.i.evaluation.runner.processor;

import java.io.IOException;

import de.spektrumprojekt.i.similarity.messagegroup.TermBasedMessageGroupSimilarityComputer;

public class MessageGroupSimilarityEvaluationProcessor extends SimpleEvaluationProcessor {

    public static final int ORDER = 200000;

    public MessageGroupSimilarityEvaluationProcessor(EvaluationExecutionProvider evaluationProvider) {
        super(evaluationProvider, ORDER);
    }

    @Override
    public void afterSingleRun() throws EvaluationException {

        TermBasedMessageGroupSimilarityComputer termBasedMessageGroupSimilarityComputer = new TermBasedMessageGroupSimilarityComputer(
                getEvaluationExecutionProvider().getPersistence(),
                getEvaluationExecutionProvider().getEvaluationExecuterConfiguration()
                        .getScorerConfiguration().getUserModelAdapterConfiguration()
                        .getMessageGroupSimilarityConfiguration());

        try {
            termBasedMessageGroupSimilarityComputer.run();
        } catch (IOException e) {
            throw new EvaluationException(e);
        }

    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }
}
