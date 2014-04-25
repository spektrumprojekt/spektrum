package de.spektrumprojekt.i.evaluation.runner.processor;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.mahout.cf.taste.common.TasteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.i.collab.CollaborativeScoreComputer;
import de.spektrumprojekt.i.collab.InconsistentDataException;
import de.spektrumprojekt.i.collab.UserToMessageCollaborativeScoreComputer;
import de.spektrumprojekt.i.evaluation.helper.SpektrumRating;
import de.spektrumprojekt.i.evaluation.runner.configuration.EvaluatorConfiguration.TestRatingsEvalTyp;
import de.spektrumprojekt.i.scorer.CollaborativeConfiguration;

public class CollaborativeEvaluationProcessor extends SimpleEvaluationProcessor {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(CollaborativeEvaluationProcessor.class);

    public static final int ORDER = 1000;

    private TestRatingsEvalTyp testRatingsEvalTyp;
    private final TestRatingsEvaluationProcessor testRatingsEvaluationProcessor;

    public CollaborativeEvaluationProcessor(EvaluationExecutionProvider evaluationProvider,
            TestRatingsEvaluationProcessor testRatingsEvaluationProcessor) {
        super(evaluationProvider, ORDER);
        if (testRatingsEvaluationProcessor == null) {
            throw new IllegalArgumentException("testRatingsEvaluationProcessor cannot be null.");
        }
        if (evaluationProvider.getEvaluationExecuterConfiguration().getScorerConfiguration()
                .getCollaborativeConfiguration().getCollaborativeScoreComputerType() == null) {
            throw new IllegalArgumentException("collaborativeScoreComputerType cannot be null.");
        }
        this.testRatingsEvaluationProcessor = testRatingsEvaluationProcessor;
    }

    @Override
    public void afterSingleRun() throws EvaluationException {

        if (!getEvaluationExecutionProvider().getEvaluationExecuterConfiguration()
                .isUseCollabScorer()) {
            return;
        }

        Map<String, List<SpektrumRating>> testRatings = testRatingsEvaluationProcessor
                .getTestRatings();

        Collection<Message> needForEstimation = SpektrumRating.getMessages(testRatings);

        if (needForEstimation.isEmpty()) {
            return;
        }

        try {

            CollaborativeScoreComputer collaborativeRankerComputer = runCollaborativeRanker(needForEstimation);

            getEvaluationExecutionProvider().addConfig("collabranker: "
                    + collaborativeRankerComputer.getConfigurationDescription());

        } catch (InconsistentDataException e) {
            throw new EvaluationException(e);
        } catch (TasteException e) {
            throw new EvaluationException(e);
        }

    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName()
                + " testRatingsEvalTyp=" + testRatingsEvalTyp
                + " getobservationTypes=" + getObservationTypes();
    }

    public ObservationType[] getObservationTypes() {
        ObservationType[] observationTypesToUseForDataModel;
        if (getEvaluationExecutionProvider().getEvaluatorConfiguration()
                .getTrainingRatingSetRatio() == 0f) {
            observationTypesToUseForDataModel = UserToMessageCollaborativeScoreComputer.OT_ONLY_MESSAGE;
        } else {
            observationTypesToUseForDataModel = UserToMessageCollaborativeScoreComputer.OT_ONLY_RATINGS;
        }
        return observationTypesToUseForDataModel;
    }

    @Override
    public void onNewDay() throws EvaluationException {
        if (TestRatingsEvalTyp.EVERY_DAY.equals(this.testRatingsEvalTyp)) {
            startCollaborativeRanker();
        }
    }

    @Override
    public void onNewMonth() throws EvaluationException {
        if (TestRatingsEvalTyp.EVERY_MONTH.equals(this.testRatingsEvalTyp)) {
            startCollaborativeRanker();
        }
    }

    @Override
    public void onNewWeek() throws EvaluationException {
        if (TestRatingsEvalTyp.EVERY_WEEK.equals(this.testRatingsEvalTyp)) {
            startCollaborativeRanker();
        }
    }

    private CollaborativeScoreComputer runCollaborativeRanker(Collection<Message> messagesToRun)
            throws TasteException {
        StopWatch collWatch = new StopWatch();
        collWatch.start();

        String runSize = messagesToRun == null ? "'all'" : "" + messagesToRun.size();
        LOGGER.info("Starting collab ranker with {} messagesToRun... ", runSize);
        ObservationType[] observationTypesToUseForDataModel;
        observationTypesToUseForDataModel = getObservationTypes();

        CollaborativeConfiguration collaborativeConfiguration = getEvaluationExecutionProvider()
                .getEvaluationExecuterConfiguration().getScorerConfiguration()
                .getCollaborativeConfiguration();

        CollaborativeScoreComputer collaborativeRankerComputer = collaborativeConfiguration
                .getCollaborativeScoreComputerType()
                .createComputer(
                        getEvaluationExecutionProvider().getPersistence(),
                        collaborativeConfiguration,
                        observationTypesToUseForDataModel,
                        getEvaluationExecutionProvider().getScorer()
                                .getTermVectorSimilarityComputer());

        collaborativeRankerComputer.init();

        LOGGER.info("Collab stats: {}", collaborativeRankerComputer.someStats());
        if (messagesToRun != null) {

            collaborativeRankerComputer.run(messagesToRun);
        } else {
            collaborativeRankerComputer.run();
        }

        collWatch.stop();

        LOGGER.info(
                "Finished collab ranker in {} sec with {} ranks, {} are non zero/normal. NaN scores: {}",
                collWatch.getTime() / 1000,
                collaborativeRankerComputer.getMessageScores() == null ? "null"
                        : collaborativeRankerComputer.getMessageScores().size(),
                collaborativeRankerComputer.getNonZeroRanks(),
                collaborativeRankerComputer.getNanScores());
        return collaborativeRankerComputer;
    }

    @Override
    public void setup() throws EvaluationException {

        this.testRatingsEvalTyp = getEvaluationExecutionProvider()
                .getEvaluationExecuterConfiguration().getEvaluatorConfiguration()
                .getTestRatingsEvalTyp();

    }

    private void startCollaborativeRanker() throws EvaluationException {
        if (!getEvaluationExecutionProvider().getEvaluationExecuterConfiguration()
                .isUseCollabScorer()) {
            return;
        }
        // if there is nothing to evaluate we dont need a recomputation
        if (!this.testRatingsEvaluationProcessor.haveReadyTestRatings()) {
            return;
        }

        try {
            runCollaborativeRanker(testRatingsEvaluationProcessor.readyForTest());
        } catch (Exception e) {
            throw new EvaluationException(e);
        }
    }

}
