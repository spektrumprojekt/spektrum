package de.spektrumprojekt.i.evaluation.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

public class Configuration {

    public static final String COMMUNOTE_MYSTREAM_EVALUATION_STORAGE_ACCESS = "communote.mystream.evaluation.storageAccess";

    public static final Configuration INSTANCE = new Configuration();

    private static final String PROPERTY_EVALUATION_DIRECTORY_NAME = "evaluation.result.dir.name";
    private static final String PROPERTY_EVALUATION_RATING_IMPORTER_CSV_FILENAME = "evaluation.rating.importer.csv.filename";

    private static final String PROPERTY_EVALUATION_STARTER_PRIORITY = "evaluation.starter.configuration.priority";
    private static final String PROPERTY_EVALUATION_STARTER_START_UNIQUE_ID = "evaluation.starter.configuration.startUniqueId";
    private static final String PROPERTY_EVALUATION_STARTER_STOP_UNIQUE_ID = "evaluation.starter.configuration.stopUniqueId";
    private static final String PROPERTY_EVALUATION_STARTER_REVERSE = "evaluation.starter.configuration.reverse";
    private static final String PROPERTY_EVALUATION_STARTER_ONLY_LOG = "evaluation.starter.configuration.onlyLog";
    private static final String PROPERTY_EVALUATION_STARTER_RUN_EVALUATOR = "evaluation.starter.configuration.runEvaluator";
    private static final String PROPERTY_EVALUATION_STARTER_RUN_TOP_RANK_MESSAGE_COMPUTER = "evaluation.starter.configuration.runTopRankMessageComputer";
    private static final String PROPERTY_EVALUATION_STARTER_RUN_MEASURE_COMPUTER = "evaluation.starter.configuration.runMeasureComputer";
    private static final String PROPERTY_EVALUATION_STARTER_GENERATE_COMPARING_PLOTS_FOR_CURRENT = "evaluation.starter.configuration.generateComparingPlotsForCurrent";
    private static final String PROPERTY_EVALUATION_STARTER_RUN_PLOTS = "evaluation.starter.configuration.runPlots";

    private final Properties properties = new Properties();

    public Configuration() {
        InputStream in = null;
        try {
            in = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("evaluation-configuration.properties");
            // in = new FileInputStream(new File("evaluation-configuration.properties"));
            this.properties.load(in);
        } catch (IOException io) {
            throw new RuntimeException(io);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public String getEvaluationConfigurationAdaptationSimilarityMessageGroupPrecomputedFile() {
        return getEvaluationConfigurationAdaptationSimilarityPrecomputedDirectory()
                + File.separator
                + this.properties
                        .getProperty("evaluation.configuration.adaptation.similarity.message.group.precomputed");
    }

    public String getEvaluationConfigurationAdaptationSimilarityMessageGroupPrecomputedWithTimeFile() {
        return getEvaluationConfigurationAdaptationSimilarityPrecomputedDirectory()
                + File.separator
                + this.properties
                        .getProperty("evaluation.configuration.adaptation.similarity.message.group.precomputed.with.time");
    }

    public String getEvaluationConfigurationAdaptationSimilarityPrecomputedDirectory() {
        return this.properties
                .getProperty("evaluation.configuration.adaptation.similarity.precomputed.dir");
    }

    public String getEvaluationConfigurationAdaptationSimilarityPrecomputedFile() {
        return getEvaluationConfigurationAdaptationSimilarityPrecomputedDirectory()
                + File.separator
                + this.properties
                        .getProperty("evaluation.configuration.adaptation.similarity.precomputed.with.time");
    }

    public String getEvaluationConfigurationAdaptationSimilarityPrecomputedWithTimeFile() {
        return getEvaluationConfigurationAdaptationSimilarityPrecomputedDirectory()
                + File.separator
                + this.properties
                        .getProperty("evaluation.configuration.adaptation.similarity.precomputed.with.time");
    }

    /**
     * Example: E:\tlu\spektrum\work\evaluations\C-2012-2013.Q1-ratings-2013-04
     * 
     * @return The full evaluation directory file. The result of the evaluation runs will be stored
     *         here.
     */
    public String getEvaluationDirectoryFull() {
        return this.getEvaluationResultsDir() + File.separator
                + this.getEvaluationDirectoryName() + File.separator;
    }

    /**
     * Example: C-2012-2013.Q1-ratings-2013-04
     * 
     * @return the name (only the name) of the directory name
     */
    public String getEvaluationDirectoryName() {
        return this.properties.getProperty(PROPERTY_EVALUATION_DIRECTORY_NAME);
    }

    /**
     * Example: E:\tlu\spektrum\work\evaluations
     * 
     * @return the base evaluation result dir, it will contain subdirectories for each dataset. The
     *         aggregator files will be stored here.
     */
    public String getEvaluationResultsDir() {
        return this.properties.getProperty("evaluation.result.dir");
    }

    public boolean getEvaluationStarterGenerateComparingPlotsForCurrent() {
        return Boolean.parseBoolean(this.properties
                .getProperty(PROPERTY_EVALUATION_STARTER_GENERATE_COMPARING_PLOTS_FOR_CURRENT));
    }

    public boolean getEvaluationStarterOnlyLog() {
        return Boolean.parseBoolean(this.properties
                .getProperty(PROPERTY_EVALUATION_STARTER_ONLY_LOG));
    }

    public int getEvaluationStarterPriority() {
        return getInteger(PROPERTY_EVALUATION_STARTER_PRIORITY, 0);
    }

    public boolean getEvaluationStarterReverse() {
        return Boolean.parseBoolean(this.properties
                .getProperty(PROPERTY_EVALUATION_STARTER_REVERSE));
    }

    public boolean getEvaluationStarterRunEvaluator() {
        return Boolean.parseBoolean(this.properties
                .getProperty(PROPERTY_EVALUATION_STARTER_RUN_EVALUATOR));
    }

    public boolean getEvaluationStarterRunMeasureComputer() {
        return Boolean.parseBoolean(this.properties
                .getProperty(PROPERTY_EVALUATION_STARTER_RUN_MEASURE_COMPUTER));
    }

    public boolean getEvaluationStarterRunPlots() {
        return Boolean.parseBoolean(this.properties
                .getProperty(PROPERTY_EVALUATION_STARTER_RUN_PLOTS));
    }

    public boolean getEvaluationStarterRunTopRankMessageComputer() {
        return Boolean.parseBoolean(this.properties
                .getProperty(PROPERTY_EVALUATION_STARTER_RUN_TOP_RANK_MESSAGE_COMPUTER));
    }

    public int getEvaluationStarterStartUniqueId() {
        return getInteger(PROPERTY_EVALUATION_STARTER_START_UNIQUE_ID, 0);
    }

    public int getEvaluationStarterStopUniqueId() {
        int id = getInteger(PROPERTY_EVALUATION_STARTER_STOP_UNIQUE_ID, -1);
        if (id < 0) {
            return Integer.MAX_VALUE;
        }
        return id;
    }

    public String getFileStorageAccessStorageName() {
        return this.properties
                .getProperty("communote.mystream.evaluation.fileStorageAccess.storageName");
    }

    private int getInteger(String key, int def) {
        try {
            return Integer.parseInt(this.properties.getProperty(key));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public Properties getProperties() {
        return this.properties;
    }

    public String getRatingImporterCsvFile() {
        return this.properties.getProperty(PROPERTY_EVALUATION_RATING_IMPORTER_CSV_FILENAME);
    }

    public String getStorageAccessClassName() {
        return this.properties.getProperty(COMMUNOTE_MYSTREAM_EVALUATION_STORAGE_ACCESS);
    }

    public String getTwitterUmapJdbcUrl() {
        return this.properties.getProperty("twitter.umap.jdbc.url");
    }

}
