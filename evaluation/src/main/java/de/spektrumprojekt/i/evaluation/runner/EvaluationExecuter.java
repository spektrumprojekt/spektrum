package de.spektrumprojekt.i.evaluation.runner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.callbacks.MessageGroupMemberRunner;
import de.spektrumprojekt.callbacks.SimpleMessageGroupMemberRunner;
import de.spektrumprojekt.commons.SpektrumUtils;
import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.commons.task.TaskRunner;
import de.spektrumprojekt.commons.time.ManualTaskRunningTimeProvider;
import de.spektrumprojekt.commons.time.TimeProviderHolder;
import de.spektrumprojekt.communication.CommunicationMessage;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.configuration.SpektrumConfiguration;
import de.spektrumprojekt.configuration.properties.SimpleProperties;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.message.UserMessageScore;
import de.spektrumprojekt.datamodel.observation.Observation;
import de.spektrumprojekt.datamodel.observation.ObservationPriority;
import de.spektrumprojekt.datamodel.observation.ObservationType;
import de.spektrumprojekt.datamodel.user.User;
import de.spektrumprojekt.datamodel.user.UserModel;
import de.spektrumprojekt.datamodel.user.UserModelEntry;
import de.spektrumprojekt.datamodel.user.UserModelEntryTimeBin;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.collab.CollaborativeIntelligenceFactory;
import de.spektrumprojekt.i.collab.UserToMessageCollaborativeScoreComputer;
import de.spektrumprojekt.i.evaluation.MessageDataSetProvider;
import de.spektrumprojekt.i.evaluation.RatingAccessor;
import de.spektrumprojekt.i.evaluation.SimpleRatingAccessor;
import de.spektrumprojekt.i.evaluation.configuration.Configuration;
import de.spektrumprojekt.i.evaluation.helper.SpektrumRating;
import de.spektrumprojekt.i.evaluation.rank.InternalMessageRank;
import de.spektrumprojekt.i.evaluation.runner.configuration.EvaluationExecuterConfiguration;
import de.spektrumprojekt.i.evaluation.runner.configuration.EvaluatorConfiguration;
import de.spektrumprojekt.i.evaluation.runner.crossvalidation.CrossValidationPerUserRatingSplitter;
import de.spektrumprojekt.i.evaluation.runner.crossvalidation.CrossValidationRatingAccessor;
import de.spektrumprojekt.i.evaluation.runner.crossvalidation.CrossValidationRatingSplitter;
import de.spektrumprojekt.i.evaluation.runner.like.LikeLearnerEvaluationProcessor;
import de.spektrumprojekt.i.evaluation.runner.processor.CollaborativeEvaluationProcessor;
import de.spektrumprojekt.i.evaluation.runner.processor.DUMAEvaluationProcessor;
import de.spektrumprojekt.i.evaluation.runner.processor.EvaluationException;
import de.spektrumprojekt.i.evaluation.runner.processor.EvaluationExecutionProvider;
import de.spektrumprojekt.i.evaluation.runner.processor.EvaluationProcessorChain;
import de.spektrumprojekt.i.evaluation.runner.processor.MessageGroupSimilarityEvaluationProcessor;
import de.spektrumprojekt.i.evaluation.runner.processor.TestRatingsEvaluationProcessor;
import de.spektrumprojekt.i.evaluation.runner.shortterm.ShortTermEvaluationProcessor;
import de.spektrumprojekt.i.evaluation.runner.shorttermanalysis.MessageTermProvider;
import de.spektrumprojekt.i.evaluation.runner.shorttermanalysis.ShortTermAnalysis;
import de.spektrumprojekt.i.evaluation.runner.shorttermanalysis.ShortTermConfiguration;
import de.spektrumprojekt.i.evaluation.runner.shorttermanalysis.TermProvider;
import de.spektrumprojekt.i.evaluation.runner.shorttermanalysis.TimeBinnedUserModelTermProvider;
import de.spektrumprojekt.i.evaluation.runner.timetracker.TermTimeTracker;
import de.spektrumprojekt.i.learner.Learner;
import de.spektrumprojekt.i.learner.LearningMessage;
import de.spektrumprojekt.i.learner.contentbased.UserModelCleaner;
import de.spektrumprojekt.i.scorer.MessageFeatureContext;
import de.spektrumprojekt.i.scorer.Scorer;
import de.spektrumprojekt.i.scorer.ScorerConfiguration;
import de.spektrumprojekt.i.scorer.ScorerConfigurationFlag;
import de.spektrumprojekt.i.scorer.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.i.scorer.special.RandomScorerCommand;
import de.spektrumprojekt.i.scorer.special.RelevantScorerCommand;
import de.spektrumprojekt.i.scorer.special.SpecialScorer;
import de.spektrumprojekt.i.similarity.user.UserSimilarityOutput;
import de.spektrumprojekt.i.term.frequency.TermFrequencyComputer;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.UserMessageScoreVisitor;
import de.spektrumprojekt.persistence.jpa.JPAPersistence;
import de.spektrumprojekt.persistence.simple.SimplePersistence;
import de.spektrumprojekt.persistence.simple.UserModelHolder;

public class EvaluationExecuter implements EvaluationExecutionProvider {

    private final static Logger LOGGER = LoggerFactory.getLogger(EvaluationExecuter.class);

    public static File getFinalMessageRankFile(String evaluatorResultFilename) {
        return new File(evaluatorResultFilename + ".ranks");
    }

    private final EvaluationProcessorChain chain = new EvaluationProcessorChain();

    private Persistence persistence;

    private Scorer scorer;

    private Learner learner;

    private Evaluator evaluator;

    private boolean doSomeDebugging;

    private final TaskRunner taskRunner = new TaskRunner(true);

    private final ManualTaskRunningTimeProvider timeProvider = new ManualTaskRunningTimeProvider(
            taskRunner);

    private UserModelCleaner userModelCleaner;

    private final MessageDataSetProvider dataSetProvider;

    private final Queue<CommunicationMessage> communicatorQueue = new LinkedBlockingQueue<CommunicationMessage>();

    private ManualVirtualMachineCommunicator communicator;

    private final String evaluatorResultFilename;

    private final EvaluationExecuterConfiguration executionConfiguration;

    private final EvaluatorConfiguration evaluatorConfiguration;

    private final ScorerConfiguration scorerConfiguration;
    private final DecimalFormat doubleFormatTwoDigests = new DecimalFormat("#.##");

    private final boolean dataSetProviderWillBeReused = true;

    private Date lastMessageDate = null;

    private int messageIndex = 0;

    private int learnings;

    private RatingAccessor ratingAccessor;

    private FileWriter finalMessageRankWriter = null;

    private File finalMessageRankFilename;
    private Date firstRatingDate;

    private Date lastRatingDate;

    /**
     * Map from message global id to the ratings of that id
     */
    private Map<String, List<SpektrumRating>> trainingRatings;

    /**
     * Map from message global id to the ratings of that id
     */
    private Map<String, List<SpektrumRating>> testRatings;

    private final boolean doNutritionAndEnergyAnalysis = false;

    private TestRatingsEvaluationProcessor testRatingsEvaluationProcessor;

    public EvaluationExecuter(EvaluationExecuterConfiguration configuration,
            String evaluatorResultFilename) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration cannot be null");
        }
        if (configuration.getEvaluatorConfiguration() == null) {
            throw new IllegalArgumentException(
                    "configuration.getEvaluatorConfiguration() cannot be null");
        }
        if (configuration.getScorerConfiguration() == null) {
            throw new IllegalArgumentException(
                    "configuration.getRankerConfiguration() cannot be null");
        }
        this.executionConfiguration = configuration;
        this.evaluatorConfiguration = configuration.getEvaluatorConfiguration();
        this.scorerConfiguration = configuration.getScorerConfiguration();
        this.evaluatorResultFilename = evaluatorResultFilename;
        this.dataSetProvider = this.executionConfiguration.getDataSetProvider();

        setupChain();
    }

    // private Date lastModelCalculationDate;

    @Override
    public void addConfig(String config) {
        this.evaluator.addConfig(config);
    }

    public void addStatistics() {
        evaluator.addConfig("users size: " + dataSetProvider.getUserGlobalIds().size());

        evaluator.addConfig("messages size: " + dataSetProvider.getMessageSize());
        evaluator.addConfig("trainingRatings size: "
                + SpektrumUtils.countMapOfLists(trainingRatings));
        evaluator.addConfig("number of messages with trainingRatings: " + trainingRatings.size());
        evaluator.addConfig("MessageRankInteractionLevelsToInclude: "
                + Arrays.toString(this.evaluatorConfiguration
                        .getMessageRankInteractionLevelsToInclude()));
        evaluator.addConfig("onlyEvaluateRoot "
                + this.executionConfiguration.getEvaluatorConfiguration().isOnlyEvaluateRoot());

        int testRatingCount = SpektrumUtils.countMapOfLists(testRatings);
        if (this.executionConfiguration.getEvaluatorConfiguration().isOnlyEvaluateRoot()) {
            evaluator.addConfig("testRatings size (maximum, only root messages are evaluated!): "
                    + testRatingCount);
            evaluator.addConfig("number of messages with testRatings: " + testRatings.size());
        } else {
            evaluator.addConfig("testRatings size: " + testRatingCount);
            evaluator.addConfig("number of messages with testRatings: " + testRatings.size());
        }
    }

    private void changeCurrentTime(Date newTime) throws EvaluationException {
        long current = timeProvider.getCurrentTime();

        Calendar oldCal = new GregorianCalendar();
        oldCal.setTime(new Date(current));

        int oldWeek = oldCal.get(Calendar.WEEK_OF_YEAR);
        int oldMonth = oldCal.get(Calendar.MONTH);
        int oldDay = oldCal.get(Calendar.DAY_OF_YEAR);

        Calendar newCal = new GregorianCalendar();
        newCal.setTime(newTime);

        int newWeek = newCal.get(Calendar.WEEK_OF_YEAR);
        int newMonth = newCal.get(Calendar.MONTH);
        int newDay = newCal.get(Calendar.DAY_OF_YEAR);

        timeProvider.setCurrentTime(newTime.getTime());

        if (oldMonth != newMonth) {
            chain.onNewMonth();
        }
        if (oldWeek != newWeek) {
            chain.onNewWeek();
        }
        if (oldDay != newDay) {
            chain.onNewDay();
        }

    }

    private void checkMessage(Message mes) {
        Collection<Term> terms = MessageHelper.getAllTerms(mes);

        if (!terms.isEmpty()) {
            throw new RuntimeException("terms should be empty.");
        }
    }

    private void cleanUp(List<SpektrumRating> testRatings, int messageIndex) {
        if (messageIndex % 1000 == 0 && userModelCleaner != null) {
            this.userModelCleaner.run();
            SimplePersistence sp = (SimplePersistence) persistence;
            Collection<String> removeMe = new LinkedList<String>();
            int i = 0;
            for (Message message : sp.getMessages()) {
                boolean canDelete = true;
                ratings: for (int k = 0; k < testRatings.size(); k++) {
                    SpektrumRating rating = testRatings.get(k);
                    if (message.getId().equals(rating.getMessage().getId())) {
                        canDelete = false;
                        break ratings;
                    }
                }
                if (canDelete) {
                    removeMe.add(message.getGlobalId());
                }
                LOGGER.debug("Cleanup iteration " + i++ + " " + 100 * i / sp.getMessages().size()
                        + " %");
            }
            for (String id : removeMe) {
                sp.removeMessage(id);
            }
        }
    }

    public void close() throws IOException {
        if (scorer != null) {
            scorer.close();
        }

        if (evaluator != null) {
            evaluator.close();
        }

        if (this.communicator != null) {
            this.communicator.close();
            this.communicator = null;
        }
        if (finalMessageRankWriter != null) {
            IOUtils.closeQuietly(finalMessageRankWriter);
        }
    }

    private void deliverAllCommunicatorMessages() throws InterruptedException {
        this.communicator.deliever();
        boolean reallyWait = false;
        if (reallyWait) {
            int wait = 100;
            while (!communicatorQueue.isEmpty()) {
                Thread.sleep(50);
                wait--;
                if (wait == 0) {
                    throw new RuntimeException("Queue not empty after 5 seconds messagesLeft="
                            + communicatorQueue.size() + " " + communicatorQueue);
                }
            }
        }
        if (communicator.hasErrors()) {
            throw new RuntimeException(
                    "Communicator encountered errors deliverung messages. Check the log.");
        }
    }

    private void detailedDebug() throws IOException {

        if (executionConfiguration.isOutputTerms()) {
            TermFrequencyComputer termFrequencyComputer = this.scorer.getTermFrequencyComputer();
            if (termFrequencyComputer != null) {
                termFrequencyComputer.dumpTermCounts(this.evaluatorResultFilename + ".terms");
            }

        }

        if (persistence instanceof SimplePersistence) {
            SimplePersistence sp = (SimplePersistence) persistence;
            if (doSomeDebugging) {
                User user = null; // sp.getOrCreateUser(convertToGlobalId(new CommunoteUser(4l,
                                  // "tlu")));
                if (user != null) {
                    for (String userModelType : scorerConfiguration.getUserModelConfigurations()
                            .keySet()) {
                        UserModelHolder userModel = sp.getUserModelHolder(user, userModelType);
                        for (UserModelEntry entry : userModel.getUserModelEntries().values()) {
                            LOGGER.debug(entry.toString());
                        }
                    }
                }
            }
            LOGGER.debug(sp.dumpUserModelSizes());
        }
    }

    private void evalSingleMessage(Message spektrumMessage) throws InterruptedException, Exception {

        chain.beforeMessage(spektrumMessage);

        checkMessage(spektrumMessage);

        if (lastMessageDate != null && spektrumMessage.getPublicationDate().before(lastMessageDate)) {
            throw new IllegalStateException(
                    "spektrumMessage.getPublicationDate().before(lastMessageDate) == true");
        }

        lastMessageDate = spektrumMessage.getPublicationDate();

        changeCurrentTime(spektrumMessage.getPublicationDate());

        MessageRelation relation = dataSetProvider.getMessageRelation(spektrumMessage);

        MessageFeatureContext context = scorer.score(spektrumMessage, relation, null, false);

        this.deliverAllCommunicatorMessages();

        chain.afterScoringMessage(spektrumMessage, context);

        train(spektrumMessage);

        chain.afterMessage(spektrumMessage, context);

        // cleanUp(testRatings, messageIndex);
    }

    @Override
    public Communicator getCommunicator() {
        return this.communicator;
    }

    public double getCurrentNumberOfProcessedMessagesRatio() {
        return dataSetProvider == null ? -1 : this.messageIndex
                / (double) dataSetProvider.getMessageSize();
    }

    @Override
    public EvaluationExecuterConfiguration getEvaluationExecuterConfiguration() {
        return this.executionConfiguration;
    }

    @Override
    public EvaluationProcessorChain getEvaluationProcessorChain() {
        return chain;
    }

    @Override
    public Evaluator getEvaluator() {
        return evaluator;
    }

    @Override
    public EvaluatorConfiguration getEvaluatorConfiguration() {
        return this.evaluatorConfiguration;
    }

    @Override
    public String getEvaluatorResultFilename() {
        return evaluatorResultFilename;
    }

    public File getFinalMessageRankFilename() {
        return this.finalMessageRankFilename;
    }

    @Override
    public Learner getLearner() {
        return learner;
    }

    @Override
    public Persistence getPersistence() {
        return this.persistence;
    }

    @Override
    public Scorer getScorer() {
        return this.scorer;
    }

    @Override
    public TaskRunner getTaskRunner() {
        return this.taskRunner;
    }

    private String getUserSimilarityDumpFilename() {
        return this.evaluatorResultFilename + ".userSim";
    }

    private void learnMessage(SpektrumRating rating) throws Exception {

        Observation observation = new Observation(
                rating.getUserGlobalId(),
                rating.getMessage().getGlobalId(),
                ObservationType.RATING,
                ObservationPriority.USER_FEEDBACK,
                null,
                rating.getMessage().getPublicationDate(),
                rating.getInterest()
                );

        LearningMessage learningMessage = new LearningMessage(observation);

        chain.beforeLearning(observation);

        learner.deliverMessage(learningMessage);

        chain.afterLearning(observation);
    }

    private void reRank(final int overallMessageSize) {
        LOGGER.info("Reranking {} messages ...", overallMessageSize);
        int count = 0;
        Iterator<Message> rescoreIterator = this.dataSetProvider.getMessageIterator();
        while (rescoreIterator.hasNext()) {
            count++;

            scorer.rescore(rescoreIterator.next(), null);
            rescoreIterator.remove();

            LOGGER.info("Rescoring {} % done ...", count * 100 / overallMessageSize);
        }
    }

    public synchronized void run() throws Exception {
        executionConfiguration.getDataSetProvider().init();

        evaluator = new Evaluator(evaluatorResultFilename);
        evaluator.addConfig(this.executionConfiguration.getComment());
        evaluator.addConfig(this.evaluatorConfiguration.getConfigurationDescription());
        evaluator.addConfig(this.chain.getConfigurationDescription());

        finalMessageRankFilename = getFinalMessageRankFile(evaluatorResultFilename);
        finalMessageRankWriter = new FileWriter(finalMessageRankFilename);

        chain.beforeRun();

        if (evaluatorConfiguration.isUseCrossValidation()
                || evaluatorConfiguration.isUseCrossValidationWithUserPartitions()) {

            CrossValidationRatingSplitter splitter;

            if (evaluatorConfiguration.isUseCrossValidationWithUserPartitions()) {
                splitter = new CrossValidationPerUserRatingSplitter(executionConfiguration
                        .getDataSetProvider().getRatings(),
                        evaluatorConfiguration.getCrossValidationPartitionSize(),
                        evaluatorConfiguration.isCrossValidationSwitchTrainingTestPartitions());
            } else {
                splitter = new CrossValidationRatingSplitter(executionConfiguration
                        .getDataSetProvider().getRatings(),
                        evaluatorConfiguration.getCrossValidationPartitionSize(),
                        evaluatorConfiguration.isCrossValidationSwitchTrainingTestPartitions());
            }

            CrossValidationRatingAccessor xRatingAccessor = new CrossValidationRatingAccessor(
                    splitter);
            this.ratingAccessor = xRatingAccessor;
            evaluator.addConfig("ratingAccessor"
                    + this.ratingAccessor.getConfigurationDescription());
            for (int i = 0; i < splitter.getNumberOfPartitions(); i++) {

                evaluator.addConfig("Running with partition " + i + " of "
                        + splitter.getNumberOfPartitions());
                xRatingAccessor.setPartion(i);

                this.runSingle();

                executionConfiguration.getDataSetProvider().reset();

            }
        } else {
            ratingAccessor = new SimpleRatingAccessor(executionConfiguration.getDataSetProvider(),
                    evaluatorConfiguration.getTrainingRatingSetRatio());

            this.runSingle();
        }

        chain.afterRun();

        this.persistence.close();
        this.persistence = null;
    }

    public synchronized void runSingle() throws Exception {

        // lets take this stuff as a reset
        this.evaluator.resetCurrentCount();
        lastMessageDate = null;
        messageIndex = 0;

        // 0th setup
        setupPersistence();
        setupData();
        setupIntelligence();

        setupTime();

        chain.setup();

        final Iterator<Message> messagesIterator = dataSetProvider.getMessageIterator();
        final int overallMessageSize = dataSetProvider.getMessageSize();

        setupTrainingTestRatings();

        addStatistics();

        // generator = null;

        if (!dataSetProviderWillBeReused) {
            dataSetProvider.close();
        }
        LOGGER.info("Phase 1 successfull.");

        // 2nd A/ run new messages into process
        // 2nd B/ if there are simulated ratings for this message run it directly

        int mod = overallMessageSize / 1000;
        mod = mod < 1 ? 1 : mod > 1000 ? 1000 : mod;
        StopWatch stopWatch = new StopWatch();
        StopWatch overallStopWatch = new StopWatch();
        overallStopWatch.start();

        chain.beforeSingleRun();

        while (messagesIterator.hasNext()) {
            learnings = 0;

            Message spektrumMessage = messagesIterator.next();

            stopWatch.reset();
            stopWatch.start();

            evalSingleMessage(spektrumMessage);

            messagesIterator.remove();

            messageIndex++;
            double percentage = 100 * messageIndex / (double) overallMessageSize;
            double mpsec = 0;
            if (overallStopWatch.getTime() > 0) {
                mpsec = 1000 * (messageIndex + 1) / overallStopWatch.getTime();
            }
            if (messageIndex % mod == 0 && LOGGER.isDebugEnabled()) {
                // LOGGER.debug("UserModelEntries: {}", getUserModelEntriesCount());
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(
                        "Message {} {} with {} learnings ({} messages with learnings left)  took {} ms Overall: {} %  {} mpsec",
                        spektrumMessage.getPublicationDate(), messageIndex + 1, learnings,
                        trainingRatings.size(), stopWatch.getTime(), (int) percentage,
                        doubleFormatTwoDigests.format(mpsec));
            }

        }
        LOGGER.info("Phase 2 successfull.");

        if (trainingRatings.size() != 0) {
            throw new RuntimeException("There should not be any training rating left.");
        }

        deliverAllCommunicatorMessages();

        if (evaluatorConfiguration.isRerankAtTheEnd()) {
            reRank(overallMessageSize);

            deliverAllCommunicatorMessages();
        }

        chain.afterSingleRun();

        trainingRatings.clear();
        testRatings.clear();
        trainingRatings = null;
        testRatings = null;

        overallStopWatch.stop();

        UserSimilarityOutput userSimilarityOutput = new UserSimilarityOutput(this.persistence);
        userSimilarityOutput.write(getUserSimilarityDumpFilename());

        List<String> featureCounts = scorer.getFeatureStatisticsCommand().getFeatureCountAsString();
        for (String s : featureCounts) {
            evaluator.addConfig(s);
        }

        evaluator.addConfig("overallTime in ms: " + overallStopWatch.getTime());
        evaluator.addConfig("time per message (tpm) in ms: " + overallStopWatch.getTime()
                / messageIndex);
        evaluator.addConfig("evaluator: " + evaluator.toString());
        for (Entry<String, String> entry : persistence.getUserModelEntriesCountDescription()
                .entrySet()) {
            evaluator.addConfig("userModelSize: type: " + entry.getKey() + " size: "
                    + entry.getValue());
        }
        evaluator.addConfig("ranker.reRankCount: " + scorer.getReScoringCount());
        evaluator.addConfig("ranker.invokeLearnerCount: " + scorer.getInvokerLearnerCount());

        evaluator.addConfig("AdaptMessageRankByCMFOfSimilarUsersCommand.adapationCount: "
                + scorer.getAdaptMessageScoreByCMFOfSimilarUsersCommand().getAdaptationCount());
        evaluator.addConfig("AdaptMessageRankByCMFOfSimilarUsersCommand.adapationIncrease: "
                + scorer.getAdaptMessageScoreByCMFOfSimilarUsersCommand().getAdaptationIncrease());
        evaluator.addConfig("BasedOnAdaptedTermsCount: " + evaluator.getBasedOnAdaptedTermsCount());

        evaluator.addConfig("actualTestRatings size (evaluator.currentCount): "
                + this.evaluator.getCurrentCount());
        evaluator.addConfig("evaluator.count: " + this.evaluator.getCount());
        evaluator.addConfig("persistence.statistics: " + this.persistence.computeStatistics());

        storeMessageRanksInFile();
        detailedDebug();
        // 3rd rank with test messages (without learning)
        // 4th compute error

        // ShortTermAnalysisAlgorithm Anaylsis
        if (doNutritionAndEnergyAnalysis) {
            ShortTermConfiguration shortTermConfiguration = new ShortTermConfiguration();
            long startTime = new Date().getTime();
            long endTime = 0;
            long precisition = scorerConfiguration.getUserModelConfigurations().values().iterator()
                    .next()
                    .getLengthOfSingleBinInMs();
            UserModel userModel = persistence.getOrCreateUserModelByUser("125", scorerConfiguration
                    .getUserModelConfigurations().keySet().iterator().next());
            Map<Term, UserModelEntry> UserModelEntries = persistence.getUserModelEntriesForTerms(
                    userModel, persistence.getAllTerms());
            for (UserModelEntry entry : UserModelEntries.values()) {

                for (UserModelEntryTimeBin bin : entry.getTimeBinEntries()) {
                    startTime = Math.min(bin.getTimeBinStart(), startTime);
                    endTime = Math.max(bin.getTimeBinStart(), endTime);
                }
            }

            shortTermConfiguration.setStartDate(new Date(startTime));
            shortTermConfiguration.setEndDate(new Date(endTime + precisition));
            shortTermConfiguration.setBinSize(precisition);
            shortTermConfiguration.setFolderPath(Configuration.INSTANCE.getEvaluationResultsDir());
            shortTermConfiguration.setHistoryLength(new int[] { });
            shortTermConfiguration.setPersistence(persistence);
            MessageGroup messageGroup = persistence.getMessageGroupByGlobalId("communotedev");
            shortTermConfiguration.setUseOnlyMessageGroups(new String[] { String
                    .valueOf(messageGroup.getId()) });
            shortTermConfiguration.setTermProviders(new TermProvider[] {
                    new MessageTermProvider(
                            shortTermConfiguration.getPersistence(),
                            shortTermConfiguration.getStartDate(),
                            shortTermConfiguration.getEndDate()
                    ),
                    new TimeBinnedUserModelTermProvider(
                            shortTermConfiguration.getPersistence(),
                            scorerConfiguration.getUserModelConfigurations().keySet().iterator()
                                    .next(),
                            shortTermConfiguration.getStartDate(),
                            "125") });
            ShortTermAnalysis shortTermAnalysisExecuter = new ShortTermAnalysis(
                    shortTermConfiguration);
            shortTermAnalysisExecuter.doAnalysis();
        }
    }

    public void setRanker(Scorer ranker) {
        this.scorer = ranker;
    }

    private void setupChain() {

        if (scorerConfiguration.hasFlag(ScorerConfigurationFlag.USE_DIRECTED_USER_MODEL_ADAPTATION)) {
            chain.addProcessor(new DUMAEvaluationProcessor(this));
        }

        testRatingsEvaluationProcessor = new TestRatingsEvaluationProcessor(this);
        this.chain.addProcessor(testRatingsEvaluationProcessor);

        if (this.scorerConfiguration.getCollaborativeConfiguration()
                .getCollaborativeScoreComputerType() != null) {
            this.chain.addProcessor(new CollaborativeEvaluationProcessor(this,
                    testRatingsEvaluationProcessor));
        }

        if (evaluatorConfiguration.isAnalyzeTerms()) {
            this.chain.addProcessor(new TermTimeTracker(this));
        }

        if (this.evaluatorConfiguration.isUseLikesForLearning()) {
            this.chain.addProcessor(new LikeLearnerEvaluationProcessor(this));
        }

        if (this.executionConfiguration.isOutputMessageGroupSimilarity()) {
            this.chain.addProcessor(new MessageGroupSimilarityEvaluationProcessor(this));
        }

        if (this.getEvaluationExecuterConfiguration().getScorerConfiguration()
                .getShortTermMemoryConfiguration() != null) {
            this.chain.addProcessor(new ShortTermEvaluationProcessor(this));
        }
    }

    public void setupData() {
        evaluator.setPersistence(persistence);
        evaluator.addConfig("persistence: " + persistence.getClass().getSimpleName());

        // 1st create users
        for (String userGlobalId : dataSetProvider.getUserGlobalIds()) {
            persistence.getOrCreateUser(userGlobalId);
        }

        for (MessageGroup messageGroup : dataSetProvider.getMessageGroups()) {
            persistence.storeMessageGroup(messageGroup);
        }
    }

    private void setupIntelligence() throws Exception {

        if (executionConfiguration.isLogTermFrequency()) {
            scorerConfiguration.setTermUniquenessLogfile(this.evaluatorResultFilename
                    + "-term-uniqueness.txt");
        }

        communicator = new ManualVirtualMachineCommunicator();

        Collection<String> userIds = dataSetProvider.getUserGlobalIds();
        if (this.evaluatorConfiguration.isOnlyUseUsersWithRatings()) {
            userIds = dataSetProvider.getUsersWithRatingsGlobalIds();
        }
        MessageGroupMemberRunner<MessageFeatureContext> memberRunner = new SimpleMessageGroupMemberRunner<MessageFeatureContext>(
                userIds);
        if (executionConfiguration.isUseIterativeCollabScorer()) {
            ObservationType[] observationTypesToUseForDataModel;
            if (evaluatorConfiguration.getTrainingRatingSetRatio() == 0f) {
                observationTypesToUseForDataModel = UserToMessageCollaborativeScoreComputer.OT_ONLY_MESSAGE;
            } else {
                observationTypesToUseForDataModel = UserToMessageCollaborativeScoreComputer.OT_ONLY_RATINGS;
            }

            CollaborativeIntelligenceFactory collaborativeIntelligenceFactory = new CollaborativeIntelligenceFactory(
                    persistence,
                    communicator,
                    memberRunner,
                    scorerConfiguration,
                    observationTypesToUseForDataModel,
                    false);
            scorer = collaborativeIntelligenceFactory.getScorer();
            learner = collaborativeIntelligenceFactory.getLearner();
        } else {

            if (executionConfiguration.getSpecialScoreCommandClass() != null) {
                Command<UserSpecificMessageFeatureContext> command;

                if (executionConfiguration.getSpecialScoreCommandClass().equals(
                        RandomScorerCommand.class)) {
                    command = new RandomScorerCommand(
                            executionConfiguration.getRandomScorerThresholdOfScoringRelevant());
                } else if (executionConfiguration.getSpecialScoreCommandClass().equals(
                        RelevantScorerCommand.class)) {
                    command = new RelevantScorerCommand();
                } else {
                    throw new IllegalArgumentException("Unknown command clazz"
                            + executionConfiguration.getSpecialScoreCommandClass());
                }

                scorer = new SpecialScorer<Command<UserSpecificMessageFeatureContext>>(persistence,
                        communicator,
                        memberRunner,
                        scorerConfiguration, command);

            } else {
                scorer = new Scorer(persistence, communicator,
                        memberRunner,
                        scorerConfiguration);
            }
            learner = new Learner(persistence, scorerConfiguration,
                    scorer.getInformationExtractionChain());
            scorer.configLearner(learner);
        }

        if (persistence instanceof SimplePersistence) {
            userModelCleaner = new UserModelCleaner((SimplePersistence) persistence);
        }

        this.communicator.registerMessageHandler(learner);
        this.communicator.registerMessageHandler(scorer);

        evaluator.addConfig(executionConfiguration.getName());
        evaluator.addConfig("start: " + new Date());
        evaluator.addConfig("dataSetProvider: " + dataSetProvider.getConfigurationDescription());
        evaluator.addConfig("trainingRatingSetRatio: "
                + evaluatorConfiguration.getTrainingRatingSetRatio());
        evaluator.addConfig("ranker: " + scorer.getConfigurationDescription());
        evaluator.addConfig("learner: " + learner.getConfigurationDescription());

        evaluator.addConfig("userModelCleaner: " + userModelCleaner);
        evaluator.addConfig("configuration: " + executionConfiguration);

        communicator.open();

        LOGGER.info("Setup intelligence successfull.");
    }

    private void setupPersistence() {
        LOGGER.info("Setup persistence ...");
        SimpleProperties configuration = new SimpleProperties((Map<String, Object>) null);

        boolean beInMemory = true;
        boolean beHardcoreInMemory = true;

        if (!beInMemory) {
            configuration.getDefaultProperties().put(
                    SpektrumConfiguration.JAVAX_PERSISTENCE_JDBC_DRIVER, "org.postgresql.Driver");
            configuration.getDefaultProperties().put(
                    SpektrumConfiguration.JAVAX_PERSISTENCE_JDBC_URL,
                    "jdbc:postgresql://localhost:5432/spektrum_evaluation");
            configuration.getDefaultProperties().put(
                    SpektrumConfiguration.JAVAX_PERSISTENCE_JDBC_USER, "communote");
            configuration.getDefaultProperties().put(
                    SpektrumConfiguration.JAVAX_PERSISTENCE_JDBC_PASSWORD, "communote");
        } else {
            configuration.getDefaultProperties().put(
                    SpektrumConfiguration.JAVAX_PERSISTENCE_JDBC_DRIVER, "org.h2.Driver");
            configuration.getDefaultProperties().put(
                    SpektrumConfiguration.JAVAX_PERSISTENCE_JDBC_URL, "jdbc:h2:mem:test");
        }

        // persistence = new JPAPersistence(configuration);
        // be really really in memory

        if (beHardcoreInMemory) {
            persistence = new SimplePersistence();
        } else {
            persistence = new JPAPersistence(configuration);
        }
        persistence.initialize();

        LOGGER.info("Setup persistence successfull.");
    }

    private void setupTime() {
        timeProvider.reset();
        timeProvider.setCurrentTime(this.dataSetProvider.getFirstMessageDate().getTime());
        TimeProviderHolder.DEFAULT = timeProvider;

        LOGGER.info("Setup time successfull. currentTime="
                + new Date(TimeProviderHolder.DEFAULT.getCurrentTime()));
    }

    private void setupTrainingTestRatings() throws IOException {
        List<SpektrumRating> ratings = dataSetProvider.getRatings();

        List<SpektrumRating> trainingRatingsList = new ArrayList<SpektrumRating>();
        List<SpektrumRating> testRatingsList = new ArrayList<SpektrumRating>();

        if (evaluatorConfiguration.isEvaluateAgainstTraining()) {
            trainingRatingsList.addAll(ratings);
            testRatingsList.addAll(ratings);

            LOGGER.debug("Using all ratings as training and test ratings.");
        } else {
            trainingRatingsList.addAll(ratingAccessor.getTrainingRatings());
            testRatingsList.addAll(ratingAccessor.getTestRatings());

            LOGGER.debug("Using ratings: {} training and {} test ratings ",
                    trainingRatingsList.size(),
                    testRatingsList.size());
        }

        trainingRatings = SpektrumRating
                .splitByMessages(trainingRatingsList);
        testRatings = SpektrumRating
                .splitByMessages(testRatingsList);

        List<SpektrumRating> trainingRatingsSorted = SpektrumRating
                .sortSpektrumRatings(this.trainingRatings);
        List<SpektrumRating> testRatingsSorted = SpektrumRating
                .sortSpektrumRatings(this.testRatings);

        long dateMin = Long.MAX_VALUE;
        long dateMax = Long.MIN_VALUE;
        if (trainingRatingsSorted.size() > 0) {
            dateMin = trainingRatingsSorted.get(0).getMessage().getPublicationDate().getTime();
            dateMax = trainingRatingsSorted.get(trainingRatingsSorted.size() - 1).getMessage()
                    .getPublicationDate().getTime();
        }
        if (testRatingsSorted.size() > 0) {
            dateMin = Math.min(dateMin, testRatingsSorted.get(0).getMessage().getPublicationDate()
                    .getTime());
            dateMax = Math.max(dateMax, testRatingsSorted.get(testRatingsSorted.size() - 1)
                    .getMessage()
                    .getPublicationDate().getTime());
        }

        trainingRatingsSorted.clear();
        trainingRatingsSorted = null;
        testRatingsSorted.clear();
        testRatingsSorted = null;

        firstRatingDate = new Date(dateMin);
        lastRatingDate = new Date(dateMax);

        evaluator.addConfig("ratings size: " + ratings.size());

        if (!dataSetProviderWillBeReused) {
            ratings.clear();
            ratings = null;
        }

        testRatingsEvaluationProcessor.setTestRatings(testRatings);

    }

    private void storeMessageRanksInFile() throws Exception {
        this.persistence.visitAllUserMessageScores(new UserMessageScoreVisitor() {

            @Override
            public void visit(UserMessageScore messageRank, Message message) throws Exception {
                if (messageRank.getScore() > 0
                        && evaluatorConfiguration.isValidMessageRankInteractionLevel(messageRank
                                .getInteractionLevel())) {
                    finalMessageRankWriter.write(InternalMessageRank.toParseableString(messageRank,
                            message) + "\n");
                }
            }
        }, firstRatingDate, lastRatingDate);

    }

    private void train(Message spektrumMessage) throws Exception, InterruptedException {

        List<SpektrumRating> trainingRatingsForMessage = this.trainingRatings.get(spektrumMessage
                .getGlobalId());
        if (trainingRatingsForMessage != null) {
            for (SpektrumRating rating : trainingRatingsForMessage) {
                learnMessage(rating);
                learnings++;
            }
            this.trainingRatings.remove(spektrumMessage.getGlobalId());
        }
        this.deliverAllCommunicatorMessages();
    }
}
