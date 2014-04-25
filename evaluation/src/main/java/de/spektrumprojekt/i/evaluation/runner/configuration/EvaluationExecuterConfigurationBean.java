package de.spektrumprojekt.i.evaluation.runner.configuration;

import java.util.Date;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.i.evaluation.MessageDataSetProvider;
import de.spektrumprojekt.i.evaluation.MessageDataSetProviderLoader;
import de.spektrumprojekt.i.scorer.ScorerConfiguration;
import de.spektrumprojekt.i.scorer.ScorerConfigurationFlag;
import de.spektrumprojekt.i.scorer.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.i.scorer.special.RandomScorerCommand;
import de.spektrumprojekt.i.scorer.special.RelevantScorerCommand;
import de.spektrumprojekt.i.term.TermVectorSimilarityStrategy;
import de.spektrumprojekt.i.term.TermWeightStrategy;

public class EvaluationExecuterConfigurationBean implements EvaluationExecuterConfiguration {

    private final ScorerConfiguration scorerConfiguration;

    private final EvaluatorConfiguration evaluatorConfiguration;

    private String comment;

    private boolean useCollabRanker;

    private boolean useIterativeCollabScorer;

    private boolean logTermFrequency;

    private final String name;

    private boolean outputTerms;

    private boolean useMessageAnalyzer;

    private int priority;

    private MessageDataSetProviderLoader<? extends MessageDataSetProvider> dataSetProviderLoader;

    private MessageDataSetProvider dataSetProvider;

    private Date messageAnalyzerOnlyAnalyzeAfter;

    private Date startDate;

    private Date endDate;

    private final int uniqueId;

    private static int COUNT = 1;

    private Class<? extends Command<UserSpecificMessageFeatureContext>> specialRankCommandClass;

    // private UserModelEntryIntegrationStrategy
    // userModelEntryIntegrationStrategy;

    private Double randomRankerThresholdOfRankingRelevant;

    private boolean outputMessageGroupSimilarity;

    @Deprecated
    public EvaluationExecuterConfigurationBean(
            EvaluationExecuterConfigurationBean clone, String newName) {
        if (newName == null) {
            throw new IllegalArgumentException("name cannot be null.");
        }
        this.uniqueId = COUNT++;
        this.name = uniqueId + "_" + newName;

        this.dataSetProviderLoader = clone.getDataSetProviderLoader();
        if (clone.getDataSetProviderLoader() == null) {
            this.dataSetProvider = clone.getDataSetProvider();
        }
        // this.userModelEntryIntegrationStrategy =
        // clone.getUserModelEntryIntegrationStrategy();

        this.evaluatorConfiguration = new EvaluatorConfiguration();
        this.evaluatorConfiguration.setTrainingRatingSetRatio(clone
                .getEvaluatorConfiguration().getTrainingRatingSetRatio());
        this.evaluatorConfiguration.setLetTheGoalBePositive(clone
                .getEvaluatorConfiguration().getLetTheGoalBePositive());
        this.evaluatorConfiguration.setLetTheComputedBePositive(clone
                .getLetTheComputedBePositive());

        ScorerConfiguration configuration = clone.getScorerConfiguration();
        if (configuration == null) {
            configuration = new ScorerConfiguration(TermWeightStrategy.TRIVIAL,
                    TermVectorSimilarityStrategy.AVG);
        }
        this.scorerConfiguration = configuration.cloneMe();

        this.priority = clone.getPriority();
        this.outputTerms = clone.isOutputTerms();
        this.evaluatorConfiguration.setEvaluateAtTheEnd(clone
                .getEvaluatorConfiguration().isEvaluateAtTheEnd());
        this.evaluatorConfiguration.setOnlyEvaluateRoot(clone
                .getEvaluatorConfiguration().isOnlyEvaluateRoot());
        this.useMessageAnalyzer = clone.isUseMessageAnalyzer();
        this.startDate = clone.getStartDate();
        this.messageAnalyzerOnlyAnalyzeAfter = clone
                .getMessageAnalyzerOnlyAnalyzeAfter();
    }

    public EvaluationExecuterConfigurationBean(String name) {
        this(name, new EvaluatorConfiguration(), false);
    }

    public EvaluationExecuterConfigurationBean(String name,
            EvaluatorConfiguration evalConfiguration, boolean dontAddUnqiueIdToName) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null.");
        } else if (name.contains(" ")) {
            throw new IllegalArgumentException(
                    "name must not contain white spaces.");
        }
        if (evalConfiguration == null) {
            throw new IllegalArgumentException("evalConfiguration cannot be null.");
        }

        this.uniqueId = COUNT++;
        if (dontAddUnqiueIdToName) {
            this.name = name;
        } else {
            this.name = uniqueId + "_" + name;
        }

        this.evaluatorConfiguration = evalConfiguration;
        scorerConfiguration = new ScorerConfiguration(
                TermWeightStrategy.TRIVIAL, TermVectorSimilarityStrategy.AVG);

    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public MessageDataSetProvider getDataSetProvider() {
        if (dataSetProvider == null && this.dataSetProviderLoader != null) {
            synchronized (this) {
                if (dataSetProvider == null
                        && this.dataSetProviderLoader != null) {
                    dataSetProvider = this.dataSetProviderLoader
                            .getMessageDataSetProvider(this.startDate,
                                    this.endDate);
                    dataSetProvider
                            .addOnlyUseUserIdsForRatings(this.evaluatorConfiguration
                                    .getUserIdsToUseForRating());
                    dataSetProvider
                            .setTopNRatersToUse(this.evaluatorConfiguration
                                    .getTopNRatersToUse());
                }
            }
        }
        return dataSetProvider;
    }

    public MessageDataSetProviderLoader<? extends MessageDataSetProvider> getDataSetProviderLoader() {
        return dataSetProviderLoader;
    }

    public Date getEndDate() {
        return endDate;
    }

    @Override
    public EvaluatorConfiguration getEvaluatorConfiguration() {
        return evaluatorConfiguration;
    }

    @Override
    public float getLetTheComputedBePositive() {
        return evaluatorConfiguration.getLetTheComputedBePositive();
    }

    @Override
    public float getLetTheGoalBePositive() {
        return evaluatorConfiguration.getLetTheGoalBePositive();
    }

    @Override
    public Date getMessageAnalyzerOnlyAnalyzeAfter() {
        return messageAnalyzerOnlyAnalyzeAfter;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public Double getRandomRankerThresholdOfRankingRelevant() {
        return randomRankerThresholdOfRankingRelevant;
    }

    @Override
    public ScorerConfiguration getScorerConfiguration() {
        return scorerConfiguration;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Command<UserSpecificMessageFeatureContext>> Class<T> getSpecialRankCommandClass() {
        return (Class<T>) this.specialRankCommandClass;
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    @Override
    public int getUniqueId() {
        return uniqueId;
    }

    @Override
    public boolean isLogTermFrequency() {
        return logTermFrequency;
    }

    @Override
    public boolean isOutputMessageGroupSimilarity() {
        return outputMessageGroupSimilarity;
    }

    @Override
    public boolean isOutputTerms() {
        return outputTerms;
    }

    @Override
    public boolean isUseCollabRanker() {
        return useCollabRanker;
    }

    @Override
    public boolean isUseIterativeCollabRanker() {
        return useIterativeCollabScorer;
    }

    @Override
    public boolean isUseMessageAnalyzer() {
        return useMessageAnalyzer;
    }

    @Override
    public void recycle() {
        this.scorerConfiguration.getInformationExtractionConfiguration()
                .setTermFrequencyComputer(null);
        this.dataSetProvider = null;
        System.gc();

    }

    // public UserModelEntryIntegrationStrategy
    // getUserModelEntryIntegrationStrategy() {
    // return userModelEntryIntegrationStrategy;
    // }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setDataSetProvider(MessageDataSetProvider dataSetProvider) {
        this.dataSetProvider = dataSetProvider;
    }

    public void setDataSetProviderLoader(
            MessageDataSetProviderLoader<? extends MessageDataSetProvider> dataSetProviderClazz) {
        this.dataSetProviderLoader = dataSetProviderClazz;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setEvaluateAgainstTraining(boolean evaluateAgainstTraining) {
        this.evaluatorConfiguration
                .setEvaluateAgainstTraining(evaluateAgainstTraining);
    }

    public void setEvaluateAtTheEnd(boolean evaluateAtTheEnd) {
        this.evaluatorConfiguration.setEvaluateAtTheEnd(evaluateAtTheEnd);
    }

    public void setLetTheComputedBePositive(float letTheComputedBePositive) {
        this.evaluatorConfiguration
                .setLetTheComputedBePositive(letTheComputedBePositive);
    }

    public void setLetTheGoalBePositive(float letTheGoalBePositive) {
        this.evaluatorConfiguration
                .setLetTheGoalBePositive(letTheGoalBePositive);
    }

    public void setLogTermUniqueness(boolean logTermFrequency) {
        this.logTermFrequency = logTermFrequency;
    }

    public void setMessageAnalyzerOnlyAnalyzeAfter(
            Date messageAnalyzerOnlyAnalyzeAfter) {
        this.messageAnalyzerOnlyAnalyzeAfter = messageAnalyzerOnlyAnalyzeAfter;
    }

    public void setOnlyEvaluateRoot(boolean onlyEvaluateRoot) {
        this.evaluatorConfiguration.setOnlyEvaluateRoot(onlyEvaluateRoot);
    }

    public void setOutputMessageGroupSimilarity(boolean outputMessageGroupSimilarity) {
        this.outputMessageGroupSimilarity = outputMessageGroupSimilarity;
    }

    public void setOutputTerms(boolean outputTerms) {
        this.outputTerms = outputTerms;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setRandomRankerThresholdOfRankingRelevant(
            Double randomRankerThresholdOfRankingRelevant) {
        this.randomRankerThresholdOfRankingRelevant = randomRankerThresholdOfRankingRelevant;
    }

    public void setRerankAtTheEnd(boolean rerankAtTheEnd) {
        this.evaluatorConfiguration.setRerankAtTheEnd(rerankAtTheEnd);
    }

    public void setSpecialRankCommandClass(
            Class<? extends Command<UserSpecificMessageFeatureContext>> specialRankCommandClass) {
        this.specialRankCommandClass = specialRankCommandClass;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setUseCollabRanker(boolean useCollabRanker) {
        this.useCollabRanker = useCollabRanker;

    }

    public void setUseIterativeCollabRanker(boolean useIterativeCollabRanker) {
        this.useIterativeCollabScorer = useIterativeCollabRanker;
    }

    public void setUseMessageAnalyzer(boolean useMessageAnalyzer) {
        this.useMessageAnalyzer = useMessageAnalyzer;
    }

    // public void setUserModelEntryIntegrationStrategy(
    // UserModelEntryIntegrationStrategy userModelEntryIntegrationStrategy) {
    // this.userModelEntryIntegrationStrategy =
    // userModelEntryIntegrationStrategy;
    // }

    @Override
    public String toString() {
        return "EvaluationExecuterConfigurationBean [name=" + name + ", scorerConfiguration="
                + scorerConfiguration + ", evaluatorConfiguration=" + evaluatorConfiguration
                + ", comment=" + comment + ", useCollabRanker=" + useCollabRanker
                + ", useIterativeCollabRanker=" + useIterativeCollabScorer + ", logTermFrequency="
                + logTermFrequency + ", outputTerms=" + outputTerms + ", useMessageAnalyzer="
                + useMessageAnalyzer + ", priority=" + priority + ", dataSetProviderLoader="
                + dataSetProviderLoader + ", dataSetProvider=" + dataSetProvider
                + ", messageAnalyzerOnlyAnalyzeAfter=" + messageAnalyzerOnlyAnalyzeAfter
                + ", startDate=" + startDate + ", endDate=" + endDate + ", uniqueId=" + uniqueId
                + ", specialRankCommandClass=" + specialRankCommandClass
                + ", randomRankerThresholdOfRankingRelevant="
                + randomRankerThresholdOfRankingRelevant + ", outputMessageGroupSimilarity="
                + outputMessageGroupSimilarity + "]";
    }

    public void useRandomRanker() {
        this.specialRankCommandClass = RandomScorerCommand.class;
    }

    public void useRelevantRanker() {
        this.specialRankCommandClass = RelevantScorerCommand.class;
    }

    @Override
    public void validate() {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null.");
        }
        if (startDate == null) {
            throw new IllegalArgumentException(
                    "startDate cannot be null. There must be a beginning of time!");
        }
        // if (userModelEntryIntegrationStrategy == null) {
        // throw new
        // IllegalArgumentException("userModelEntryIntegrationStrategy cannot be null.");
        // }
        if (dataSetProvider == null && dataSetProviderLoader == null) {
            throw new IllegalArgumentException(
                    "dataSetProvider and dataSetProviderLoader cannot be both null.");
        }
        if (evaluatorConfiguration.getLetTheGoalBePositive() < 0
                || evaluatorConfiguration.getLetTheGoalBePositive() > 1) {
            throw new IllegalArgumentException(
                    "callTheGoalPositive must be >=0 and =<1 but is "
                            + evaluatorConfiguration.getLetTheGoalBePositive());
        }
        if (evaluatorConfiguration.getTrainingRatingSetRatio() < 0
                || evaluatorConfiguration.getTrainingRatingSetRatio() > 1) {
            throw new IllegalArgumentException(
                    "trainingRatingSetRatio must be >=0 and =<1 but is "
                            + evaluatorConfiguration
                                    .getTrainingRatingSetRatio());
        }
        if (evaluatorConfiguration.isRerankAtTheEnd()
                && !evaluatorConfiguration.isEvaluateAtTheEnd()) {
            throw new IllegalArgumentException(
                    "if reranktAtTheEnd==true thant evaluateAthTheEnd should be set as well");
        }

        this.evaluatorConfiguration.validate();

        this.scorerConfiguration.immutable();

        if (this.scorerConfiguration
                .hasFlag(ScorerConfigurationFlag.USE_DIRECTED_USER_MODEL_ADAPTATION)
                && !this.scorerConfiguration.getUserModelAdapterConfiguration()
                        .isValid()) {
            throw new IllegalArgumentException(
                    "userModelAdaptionConf is not valid: "
                            + this.scorerConfiguration
                                    .getUserModelAdapterConfiguration());
        }

        if (this.scorerConfiguration.getUserModelConfigurations() == null
                || this.scorerConfiguration.getUserModelConfigurations().size() == 0) {
            throw new IllegalArgumentException(
                    "this.rankerConfiguration.getUserModelTypes() is not set.");
        }

    }

}