package de.spektrumprojekt.i.evaluation.runner.processor;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.UserMessageScore;
import de.spektrumprojekt.i.evaluation.helper.SpektrumRating;
import de.spektrumprojekt.i.evaluation.runner.MessageAnalyzer;
import de.spektrumprojekt.i.evaluation.runner.configuration.EvaluationExecuterConfiguration;
import de.spektrumprojekt.i.evaluation.runner.configuration.EvaluatorConfiguration;
import de.spektrumprojekt.i.evaluation.runner.configuration.EvaluatorConfiguration.TestRatingsEvalTyp;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.i.ranker.feature.Feature;

public class TestRatingsEvaluationProcessor extends SimpleEvaluationProcessor {

    private EvaluatorConfiguration evaluatorConfiguration;

    private final Queue<Message> testRatingsReady = new ConcurrentLinkedQueue<Message>();

    private MessageAnalyzer messageAnalyzer;

    private EvaluationExecuterConfiguration evaluationExecutionConfiguration;

    private final Map<String, MessageFeatureContext> messageToMessageFeatureContext = new HashMap<String, MessageFeatureContext>();

    private Map<String, List<SpektrumRating>> testRatings;

    private TestRatingsEvalTyp testRatingsEvalTyp;

    public TestRatingsEvaluationProcessor(EvaluationExecutionProvider evaluationProvider) {
        super(evaluationProvider, 10000);
    }

    @Override
    public void afterMessage(Message spektrumMessage, MessageFeatureContext context)
            throws EvaluationException {
        if (!evaluatorConfiguration.isOnlyEvaluateRoot()
                || context.check(Feature.MESSAGE_ROOT_FEATURE, 1f)) {

            if (TestRatingsEvalTyp.IMMEDIATE
                    .equals(this.evaluatorConfiguration.getTestRatingsEvalTyp())) {

                test(spektrumMessage, context);
            } else {
                testRatingsReady.add(spektrumMessage);
            }
        }
    }

    @Override
    public void afterRankingMessage(Message spektrumMessage, MessageFeatureContext context)
            throws EvaluationException {
        messageToMessageFeatureContext.put(spektrumMessage.getGlobalId(), context);
    }

    @Override
    public void afterRun() throws EvaluationException {
        if (messageAnalyzer != null) {
            try {
                messageAnalyzer.close();
            } catch (IOException e) {
                throw new EvaluationException(e);
            }
        }
    }

    @Override
    public void afterSingleRun() throws EvaluationException {

        List<SpektrumRating> testRatingsLeft = SpektrumRating.sortSpektrumRatings(this.testRatings);
        testRatingsLeft = new LinkedList<SpektrumRating>(testRatingsLeft);

        while (testRatingsLeft.size() > 0) {
            evaluate(testRatingsLeft.get(0));
            testRatingsLeft.remove(0);
        }

    }

    private void emptyQueue() throws EvaluationException {
        while (!this.testRatingsReady.isEmpty()) {
            test(this.testRatingsReady.poll(), null);
        }
    }

    private void evaluate(SpektrumRating rating) throws EvaluationException {
        UserMessageScore goal = new UserMessageScore(rating.getMessage().getGlobalId(),
                rating.getUserGlobalId());
        goal.setScore(rating.getInterest().getScore());

        UserMessageScore computed = this.getEvaluationExecutionProvider().getPersistence()
                .getMessageScore(goal.getUserGlobalId(),
                        goal.getMessageGlobalId());

        if (computed != null
                && !this.evaluatorConfiguration.isValidMessageRankInteractionLevel(computed
                        .getInteractionLevel())) {
            return;
        }

        MessageFeatureContext context = this.messageToMessageFeatureContext.get(rating.getMessage()
                .getGlobalId());

        this.getEvaluationExecutionProvider().getEvaluator().evaluate(goal, computed, context);

        if (this.messageAnalyzer != null) {
            UserSpecificMessageFeatureContext userContext = context.getUserContext(rating
                    .getUserGlobalId());
            this.messageAnalyzer.analyze(goal, userContext);
        }
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    public Map<String, List<SpektrumRating>> getTestRatings() {
        return Collections.unmodifiableMap(testRatings);
    }

    public boolean haveReadyTestRatings() {
        return !this.testRatings.isEmpty();
    }

    @Override
    public void onNewDay() throws EvaluationException {
        if (TestRatingsEvalTyp.EVERY_DAY.equals(this.testRatingsEvalTyp)) {
            emptyQueue();
        }
    }

    @Override
    public void onNewMonth() throws EvaluationException {
        if (TestRatingsEvalTyp.EVERY_MONTH.equals(this.testRatingsEvalTyp)) {
            emptyQueue();
        }
    }

    @Override
    public void onNewWeek() throws EvaluationException {
        if (TestRatingsEvalTyp.EVERY_WEEK.equals(this.testRatingsEvalTyp)) {
            emptyQueue();
        }
    }

    public Collection<Message> readyForTest() {
        return Collections.unmodifiableCollection(testRatingsReady);
    }

    public void setTestRatings(Map<String, List<SpektrumRating>> testRatings) {
        this.testRatings = testRatings;
    }

    @Override
    public void setup() throws EvaluationException {
        this.evaluatorConfiguration = this.getEvaluationExecutionProvider()
                .getEvaluatorConfiguration();
        this.evaluationExecutionConfiguration = this.getEvaluationExecutionProvider()
                .getEvaluationExecuterConfiguration();

        if (this.getEvaluationExecutionProvider().getEvaluationExecuterConfiguration()
                .isUseMessageAnalyzer()) {
            this.messageAnalyzer = new MessageAnalyzer(
                    this.getEvaluationExecutionProvider().getScorer()
                            .getTermVectorSimilarityComputer().getTermWeightComputer(),
                    this.getEvaluationExecutionProvider().getScorer().getTermFrequencyComputer(),
                    evaluationExecutionConfiguration.getName(),
                    this.getEvaluationExecutionProvider().getEvaluatorResultFilename(),
                    this.evaluationExecutionConfiguration.getLetTheGoalBePositive(),
                    this.evaluationExecutionConfiguration.getLetTheComputedBePositive(),
                    this.evaluationExecutionConfiguration.getMessageAnalyzerOnlyAnalyzeAfter());

            try {
                this.messageAnalyzer.initWriteToFile();
            } catch (IOException e) {
                throw new EvaluationException(e);
            }
        }

        this.testRatingsEvalTyp = getEvaluationExecutionProvider()
                .getEvaluationExecuterConfiguration().getEvaluatorConfiguration()
                .getTestRatingsEvalTyp();
    }

    private void test(Message spektrumMessage, MessageFeatureContext context)
            throws EvaluationException {
        List<SpektrumRating> testRatingsForMessage = this.testRatings.get(spektrumMessage
                .getGlobalId());
        if (testRatingsForMessage != null) {
            for (SpektrumRating testRating : testRatingsForMessage) {

                this.getEvaluationExecutionProvider().getEvaluationProcessorChain()
                        .beforeTest(testRating, context);

                evaluate(testRating);

                this.getEvaluationExecutionProvider().getEvaluationProcessorChain()
                        .afterTest(testRating, context);
            }

            this.testRatings.remove(spektrumMessage.getGlobalId());

        }
    }

}
