package de.spektrumprojekt.i.evaluation.runner.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.observation.Observation;
import de.spektrumprojekt.i.evaluation.helper.SpektrumRating;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;

public class EvaluationProcessorChain implements EvaluationProcessor {

    private final List<EvaluationProcessor> chains = new ArrayList<EvaluationProcessor>();

    public void addProcessor(EvaluationProcessor processor) {
        chains.add(processor);
        Collections.sort(chains, new Comparator<EvaluationProcessor>() {

            public int compare(EvaluationProcessor o1, EvaluationProcessor o2) {
                int v1 = o1 == null ? 0 : o1.getOrder();
                int v2 = o2 == null ? 0 : o2.getOrder();
                return v1 - v2;
            }
        });
    }

    public void afterLearning(Observation observation) throws EvaluationException {
        for (EvaluationProcessor processor : chains) {
            processor.afterLearning(observation);
        }
    }

    public void afterMessage(Message spektrumMessage, MessageFeatureContext context)
            throws EvaluationException {
        for (EvaluationProcessor processor : chains) {
            processor.afterMessage(spektrumMessage, context);
        }
    }

    public void afterRankingMessage(Message spektrumMessage, MessageFeatureContext context)
            throws EvaluationException {
        for (EvaluationProcessor processor : chains) {
            processor.afterRankingMessage(spektrumMessage, context);
        }
    }

    public void afterRun() throws EvaluationException {
        for (EvaluationProcessor processor : chains) {
            processor.afterRun();
        }
    }

    public void afterSingleRun() throws EvaluationException {
        for (EvaluationProcessor processor : chains) {
            processor.afterSingleRun();
        }
    }

    public void afterTest(SpektrumRating testRating, MessageFeatureContext context)
            throws EvaluationException {
        for (EvaluationProcessor processor : chains) {
            processor.afterTest(testRating, context);
        }
    }

    public void beforeLearning(Observation observation) throws EvaluationException {
        for (EvaluationProcessor processor : chains) {
            processor.beforeLearning(observation);
        }
    }

    public void beforeMessage(Message spektrumMessage) throws EvaluationException {
        for (EvaluationProcessor processor : chains) {
            processor.beforeMessage(spektrumMessage);
        }
    }

    public void beforeRun() throws EvaluationException {
        for (EvaluationProcessor processor : chains) {
            processor.beforeRun();
        }
    }

    public void beforeSingleRun() throws EvaluationException {
        for (EvaluationProcessor processor : chains) {
            processor.beforeSingleRun();
        }
    }

    public void beforeTest(SpektrumRating testRating, MessageFeatureContext context)
            throws EvaluationException {
        for (EvaluationProcessor processor : chains) {
            processor.beforeTest(testRating, context);
        }
    }

    public String getConfigurationDescription() {
        String name = this.getClass().getSimpleName() + " ";
        for (EvaluationProcessor proc : this.chains) {
            name += " " + proc.getConfigurationDescription();
        }
        return name;
    }

    public int getOrder() {
        return 0;
    }

    public void onNewDay() throws EvaluationException {
        for (EvaluationProcessor processor : chains) {
            processor.onNewDay();
        }
    }

    public void onNewMonth() throws EvaluationException {
        for (EvaluationProcessor processor : chains) {
            processor.onNewMonth();
        }
    }

    public void onNewWeek() throws EvaluationException {
        for (EvaluationProcessor processor : chains) {
            processor.onNewWeek();
        }
    }

    public void setup() throws EvaluationException {
        for (EvaluationProcessor processor : chains) {
            processor.setup();
        }
    }
}
