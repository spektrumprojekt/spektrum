package de.spektrumprojekt.i.evaluation.runner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.i.evaluation.configuration.Configuration;
import de.spektrumprojekt.i.evaluation.measure.Measure;
import de.spektrumprojekt.i.evaluation.measure.MeasureComputer;
import de.spektrumprojekt.i.evaluation.measure.MeasureFactory;
import de.spektrumprojekt.i.evaluation.measure.PrecisionRecallMeasureFactory;
import de.spektrumprojekt.i.evaluation.measure.TimeBinnedMeasureFactory;
import de.spektrumprojekt.i.evaluation.measure.top.TopRankMessageComputer;
import de.spektrumprojekt.i.evaluation.runner.aggregator.EvaluationAggregator;
import de.spektrumprojekt.i.evaluation.runner.aggregator.compare.EvaluationRunComparerComputer;
import de.spektrumprojekt.i.evaluation.runner.aggregator.compare.gen.EvaluationConfigurationRunComparerDefinitionGenerator;
import de.spektrumprojekt.i.evaluation.runner.configuration.EvaluationExecuterConfiguration;

public class EvaluationExecuterStarter implements EvaluationExecuterStarterMBean {

    private final static Logger LOGGER = LoggerFactory.getLogger(EvaluationExecuter.class);

    public static <T extends EvaluationExecuterStarter> T createStarterFromDefaultConfiguration(
            Class<T> clazz)
            throws JMException {

        int priority = Configuration.INSTANCE.getEvaluationStarterPriority();
        int startUniqueId = Configuration.INSTANCE.getEvaluationStarterStartUniqueId();
        int stopUniqueId = Configuration.INSTANCE.getEvaluationStarterStopUniqueId();

        boolean reverse = Configuration.INSTANCE.getEvaluationStarterReverse();
        boolean onlyLog = Configuration.INSTANCE.getEvaluationStarterOnlyLog();
        boolean runEvaluator = Configuration.INSTANCE.getEvaluationStarterRunEvaluator();
        boolean runTopRankMessageComputer = Configuration.INSTANCE
                .getEvaluationStarterRunTopRankMessageComputer();
        boolean runMeasureComputer = Configuration.INSTANCE
                .getEvaluationStarterRunMeasureComputer();
        boolean generateComparingPlotsForCurrent = Configuration.INSTANCE
                .getEvaluationStarterGenerateComparingPlotsForCurrent();
        boolean runPlots = Configuration.INSTANCE.getEvaluationStarterRunPlots();

        LOGGER.info("StarterConfiguration:\n " + StringUtils.join(new String[] {
                "priority: " + priority,
                "startUniqueId: " + startUniqueId,
                "stopUniqueId: " + stopUniqueId,
                "reverse: " + reverse,
                "onlyLog: " + onlyLog,
                "runEvaluator: " + runEvaluator,
                "runTopRankMessageComputer: " + runTopRankMessageComputer,
                "runMeasureComputer: " + runMeasureComputer,
                "generateComparingPlotsForCurrent: " + generateComparingPlotsForCurrent,
                "runPlots: " + runPlots
        }, ",\n "));

        T starter;
        try {
            starter = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException("Error creating new instance of " + clazz, e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error creating new instance of " + clazz, e);
        }
        starter.setRunTopRankMessageComputer(runTopRankMessageComputer);
        starter.setRunMeasureComputer(runMeasureComputer);
        starter.setRunEvaluator(runEvaluator);

        starter.registerAsMBean();

        return starter;
    }

    public static <T extends EvaluationExecuterStarter> void main(Class<T> clazz) {

        EvaluationExecuterStarter starter = null;
        try {

            int priority = Configuration.INSTANCE.getEvaluationStarterPriority();
            int startUniqueId = Configuration.INSTANCE.getEvaluationStarterStartUniqueId();
            int stopUniqueId = Configuration.INSTANCE.getEvaluationStarterStopUniqueId();

            boolean reverse = Configuration.INSTANCE.getEvaluationStarterReverse();
            boolean onlyLog = Configuration.INSTANCE.getEvaluationStarterOnlyLog();
            boolean generateComparingPlotsForCurrent = Configuration.INSTANCE
                    .getEvaluationStarterGenerateComparingPlotsForCurrent();
            boolean runPlots = Configuration.INSTANCE.getEvaluationStarterRunPlots();

            starter = createStarterFromDefaultConfiguration(clazz);

            starter.createConfigurations();

            starter.run(
                    priority,
                    startUniqueId,
                    stopUniqueId,
                    onlyLog,
                    reverse);

            if (onlyLog) {
                return;
            }
            if (generateComparingPlotsForCurrent) {
                starter.generateComparingPlotsForCurrent();
            }
            if (runPlots) {
                starter.generatePlotFile();
                starter.runPlots();
            }
            starter.aggregateResults();

        } catch (Exception e) {
            LOGGER.error("Error executing evaluation " + e.getMessage(), e);
            LOGGER.error("Possible causing configuration: "
                    + (starter == null ? "(Not even started.)" : starter.getCurrentConfiguration()));
        } finally {
            LOGGER.debug("Running EvaluationExecuter end.");
            LOGGER.info("Last configuration: "
                    + (starter == null ? "(Not even started.)" : starter.getCurrentConfiguration()));
        }
    }

    public static void main(String[] args) throws Exception {
        main(EvaluationExecuterStarter.class);
    }

    private final List<EvaluationExecuterConfiguration> configurations = new ArrayList<EvaluationExecuterConfiguration>();

    private EvaluationExecuterConfiguration currentConfiguration;

    private boolean runTopRankMessageComputer;

    private boolean runMeasureComputer = true;

    private List<EvaluationExecuterConfiguration> filteredConfigurations;

    private int numberOfConfigurationsProcessed;

    private boolean endGracefully;

    private Date runningStartTime;

    private Date lastProcessedConfigurationTime;

    private boolean runEvaluator;

    private EvaluationExecuter currentEvaluationExecuter;

    // TODO generate name from configuration
    public void addConfiguration(EvaluationExecuterConfiguration configuration) {
        configuration.validate();
        for (EvaluationExecuterConfiguration conf : configurations) {
            if (conf.getName().equalsIgnoreCase(configuration.getName())) {
                throw new IllegalArgumentException("cannot have a config with the same name: "
                        + conf.getName());
            }
        }
        configurations.add(configuration);
    }

    public void aggregateResults() throws Exception {

        List<File> prFilesSorted = getPrecisionRecallFiles();

        EvaluationAggregator evaluationAggregator = new EvaluationAggregator(prFilesSorted,
                Configuration.INSTANCE.getEvaluationResultsDir());

        evaluationAggregator.run();

        LOGGER.info("Wrote " + evaluationAggregator.getRunStatistics().size() + " to "
                + evaluationAggregator.getAggregatorFile());
    }

    public void createConfigurations() throws Exception {

    }

    private void filterConfigurations(int minimumPriority, int minimumUniqueId, int maximumUniqueId) {
        filteredConfigurations = new ArrayList<EvaluationExecuterConfiguration>();

        for (EvaluationExecuterConfiguration configuration : configurations) {
            if (shouldRunConfiguration(configuration, minimumPriority, minimumUniqueId,
                    maximumUniqueId)) {

                filteredConfigurations.add(configuration);
            }
        }

        Collections.sort(filteredConfigurations, new Comparator<EvaluationExecuterConfiguration>() {

            @Override
            public int compare(EvaluationExecuterConfiguration o1,
                    EvaluationExecuterConfiguration o2) {
                int diff = o2.getPriority() - o1.getPriority();
                if (diff == 0) {
                    diff = o1.getUniqueId() - o2.getUniqueId();
                }
                return diff;
            }
        });
    }

    private void generateComparingPlotsForCurrent() throws IOException {
        LOGGER.info("Running EvaluationRunComparerComputer for current configurations ...");
        EvaluationConfigurationRunComparerDefinitionGenerator gen = new EvaluationConfigurationRunComparerDefinitionGenerator(
                Configuration.INSTANCE.getEvaluationDirectoryFull(), this.filteredConfigurations);

        EvaluationRunComparerComputer erc = new EvaluationRunComparerComputer(gen);
        erc.run();

        LOGGER.info("Finished EvaluationRunComparerComputer for current configurations .");
    }

    public void generatePlotFile() throws IOException {
        StringBuilder sb = new StringBuilder();

        List<File> prFilesSorted = getPrecisionRecallFiles();

        for (File prFile : prFilesSorted) {

            String dir = FilenameUtils.getFullPath(prFile.toString());
            String name = FilenameUtils.getName(prFile.toString());

            sb.append("cd '" + dir + "'\n");
            sb.append("fname = '" + name);
            sb.append("'\n");
            sb.append("print fname\n");
            sb.append("load '" + "save-plot-pr.plt'\n");
            int numDirs = StringUtils.countMatches(dir, "/") + StringUtils.countMatches(dir, "\\");
            numDirs = Math.min(numDirs, 1);
            for (int i = 0; i < numDirs; i++) {
                sb.append("cd '..'\n");
            }
        }

        FileUtils.writeStringToFile(new File(Configuration.INSTANCE.getEvaluationResultsDir()
                + File.separator + "gen.plt"), sb.toString());

        sb = new StringBuilder();

        sb.append("<html>\n");
        sb.append("<body>\n");
        for (File prFile : prFilesSorted) {

            String prName = new File(Configuration.INSTANCE.getEvaluationResultsDir()).toURI()
                    .relativize(prFile.toURI()).getPath();
            String imgName = prName + ".png";

            sb.append("<a href=\"");
            sb.append(prName);
            sb.append("\"><img src=\"");
            sb.append(imgName);
            sb.append("\" />\n");
        }
        sb.append("</body>\n");
        sb.append("</html>\n");

        FileUtils.writeStringToFile(new File(Configuration.INSTANCE.getEvaluationResultsDir()
                + File.separator + "results.html"), sb.toString());
    }

    @Override
    public long getAverageTimePerProcessedConfiguration() {
        return lastProcessedConfigurationTime == null ? -1 : (lastProcessedConfigurationTime
                .getTime() - runningStartTime.getTime())
                / this.getNumberOfConfigurationsProcessed();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mystream.evaluation.runner.EvaluationExecuterStarterMBean#
     * getCurrentConfiguration()
     */
    @Override
    public EvaluationExecuterConfiguration getCurrentConfiguration() {
        return currentConfiguration;
    }

    @Override
    public String getCurrentConfigurationName() {
        return currentConfiguration == null ? null : currentConfiguration.getName();
    }

    @Override
    public double getCurrentNumberOfProcessedMessagesRatio() {
        return currentEvaluationExecuter == null ? -1 : currentEvaluationExecuter
                .getCurrentNumberOfProcessedMessagesRatio();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mystream.evaluation.runner.EvaluationExecuterStarterMBean#
     * getFilteredConfigurationsSize()
     */
    @Override
    public int getFilteredConfigurationsSize() {
        return this.filteredConfigurations == null ? -1 : this.filteredConfigurations.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mystream.evaluation.runner.EvaluationExecuterStarterMBean#
     * getNumberOfConfigurationsProcessed()
     */
    @Override
    public int getNumberOfConfigurationsProcessed() {
        return numberOfConfigurationsProcessed;
    }

    private List<File> getPrecisionRecallFiles() {
        Collection<String> dirs = new HashSet<String>();
        Collection<File> prFiles = new HashSet<File>();

        for (EvaluationExecuterConfiguration config : this.configurations) {
            String directory = Configuration.INSTANCE.getEvaluationResultsDir() + File.separator
                    + getResultDirectory(config, true) + File.separator;
            if (!dirs.contains(directory)) {
                prFiles.addAll(FileUtils.listFiles(new File(directory), new String[] { "pr" },
                        false));
                dirs.add(directory);
            }

        }

        List<File> prFilesSorted = new ArrayList<File>(prFiles);
        Collections.sort(prFilesSorted);
        return prFilesSorted;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mystream.evaluation.runner.EvaluationExecuterStarterMBean#
     * getRatioOfProcessedConfiguarations()
     */
    @Override
    public double getRatioOfProcessedConfiguarations() {
        return getFilteredConfigurationsSize() <= 0 ? 0
                : (double) this.numberOfConfigurationsProcessed / getFilteredConfigurationsSize();
    }

    private String getResultDirectory(EvaluationExecuterConfiguration configuration,
            boolean beRelative) {
        String providerName = configuration.getDataSetProvider().getName();
        String directory = "";

        if (!beRelative) {
            directory += Configuration.INSTANCE.getEvaluationResultsDir() + File.separator;
        }
        directory += providerName;
        return directory;
    }

    @Override
    public Date getRunningStartTime() {
        return runningStartTime;
    }

    @Override
    public long getTimeRunSoFar() {
        return runningStartTime == null ? -1 : new Date().getTime() - runningStartTime.getTime();
    }

    @Override
    public boolean isEndGracefully() {
        return endGracefully;
    }

    public boolean isRunEvaluator() {
        return runEvaluator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mystream.evaluation.runner.EvaluationExecuterStarterMBean#
     * isRunMeasureComputer()
     */
    @Override
    public boolean isRunMeasureComputer() {
        return runMeasureComputer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mystream.evaluation.runner.EvaluationExecuterStarterMBean#
     * isRunTopRankMessageComputer()
     */
    @Override
    public boolean isRunTopRankMessageComputer() {
        return runTopRankMessageComputer;
    }

    public void logConfigurationsToRun() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (EvaluationExecuterConfiguration configuration : filteredConfigurations) {
            sb.append(configuration.getName() + "\n");
        }
        sb.append(filteredConfigurations.size() + " of " + configurations.size() + "\n");
        LOGGER.info(sb.toString());
    }

    public void registerAsMBean() throws JMException {

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("com.communote.plugins.mystream.evaluation:type="
                + EvaluationExecuterStarterMBean.class.getSimpleName());
        mbs.registerMBean(this, name);

    }

    @Override
    public void revokeStopOnNextConfguration() {
        LOGGER.info("Revoked gracefully stop.");
        endGracefully = false;
    }

    public void run() throws Exception {
        run(0, 0, Integer.MAX_VALUE, false, false);
    }

    public void run(int minimumPriority, int minimumUniqueId, int maximumUniqueId,
            boolean onlyLog, boolean reverse) throws Exception {

        final int numberOfConfigurations = configurations.size();

        numberOfConfigurationsProcessed = 0;

        filterConfigurations(minimumPriority, minimumUniqueId, maximumUniqueId);

        final int numberOfFilteredConfigurations = filteredConfigurations.size();

        LOGGER.info("Going to  run {} of {} configurations (hence skiping {} configurations)",
                numberOfFilteredConfigurations, numberOfConfigurations, numberOfConfigurations
                        - numberOfFilteredConfigurations);

        if (reverse) {
            Collections.reverse(filteredConfigurations);
        }

        logConfigurationsToRun();

        if (onlyLog) {
            return;
        }

        runningStartTime = new Date();
        runLoop: for (EvaluationExecuterConfiguration configuration : filteredConfigurations) {
            LOGGER.info("Running with configuration {} ..." + configuration);

            runConfiguration(configuration);

            lastProcessedConfigurationTime = new Date();
            numberOfConfigurationsProcessed++;
            int percent = numberOfConfigurationsProcessed * 100 / numberOfFilteredConfigurations;
            LOGGER.info("Finished another configuration in success. " + percent
                    + " % of configurations done.");

            if (endGracefully) {
                LOGGER.info("Gracefully end requested. Will stop now.");
                break runLoop;
            }
        }

        LOGGER.info(
                "Running EvaluationExecuterConfiguration sucess. Processed: {} All: {} Skipped: {}",
                numberOfConfigurationsProcessed, numberOfConfigurations, numberOfConfigurations
                        - numberOfConfigurationsProcessed);
    }

    public void runConfiguration(EvaluationExecuterConfiguration configuration) throws Exception {
        currentConfiguration = configuration;
        try {
            String directory = getResultDirectory(configuration, false);

            new File(directory).mkdirs();

            String nameOfRun = configuration.getName();
            String resultFilename = directory + File.separator + nameOfRun;

            LOGGER.debug("directory=" + directory);
            LOGGER.debug("resultFilename=" + resultFilename);

            if (runEvaluator) {
                configuration.getDataSetProvider().init();

                runEvaluation(configuration, directory, resultFilename);
            }
            // generate top measure file
            TopRankMessageComputer topRankMessageComputer = runTopMeasure(configuration,
                    EvaluationExecuter.getFinalMessageRankFile(resultFilename), resultFilename);

            if (runMeasureComputer && topRankMessageComputer != null) {
                for (String evalOut : topRankMessageComputer.getEvaluatorOutputFilenames()) {
                    MeasureFactory<?> measureFactory = evalOut.equals(resultFilename) ? new PrecisionRecallMeasureFactory()
                            : new TimeBinnedMeasureFactory();
                    runMeasureComputer(configuration, measureFactory, directory, evalOut);
                }
            }

        } catch (Exception e) {
            LOGGER.error("Error running configuration " + configuration + " " + e.getMessage(), e);
            throw e;
        } finally {
            configuration.recycle();
        }
    }

    private EvaluationExecuter runEvaluation(EvaluationExecuterConfiguration configuration,
            String directory, String resultFilename) throws Exception, IOException {
        EvaluationExecuter eval = null;
        try {
            eval = new EvaluationExecuter(configuration, resultFilename);
            currentEvaluationExecuter = eval;
            eval.run();
        } finally {
            if (eval != null) {
                eval.close();
            }
            currentEvaluationExecuter = null;
        }

        return eval;
    }

    private <T extends Measure> MeasureComputer<T> runMeasureComputer(
            EvaluationExecuterConfiguration configuration, MeasureFactory<T> measureFactory,
            String directory, String evaluatorOutputFilename) throws Exception {
        MeasureComputer<T> measureComputer = new MeasureComputer<T>(evaluatorOutputFilename,
                measureFactory, configuration.getLetTheGoalBePositive());
        try {

            measureComputer.run();
            measureComputer.outResults();
            LOGGER.info("MeasureComputer result written to " + measureComputer.getResultFilename());
        } catch (FileNotFoundException io) {
            LOGGER.error("Erroring running measure computer. " + io.getMessage(), io);
        }

        return measureComputer;
    }

    public void runPlots() throws IOException, InterruptedException {
        String dir = Configuration.INSTANCE.getEvaluationResultsDir();
        Process process = Runtime.getRuntime().exec("cmd /c start " + dir + "\\gen.bat", null,
                new File(dir));

        process.waitFor();
    }

    private TopRankMessageComputer runTopMeasure(EvaluationExecuterConfiguration configuration,
            File allMessageRanksFile, String evaluatorOutputFileName) throws Exception {

        TopRankMessageComputer topRankMessage = new TopRankMessageComputer(
                configuration.getEvaluatorConfiguration(), allMessageRanksFile,
                evaluatorOutputFileName);
        if (runTopRankMessageComputer) {
            topRankMessage.run();
        }
        return topRankMessage;

    }

    public void setRunEvaluator(boolean runEvaluator) {
        this.runEvaluator = runEvaluator;
    }

    public void setRunMeasureComputer(boolean runMeasureComputer) {
        this.runMeasureComputer = runMeasureComputer;
    }

    public void setRunTopRankMessageComputer(boolean runTopRankMessageComputer) {
        this.runTopRankMessageComputer = runTopRankMessageComputer;
    }

    private boolean shouldRunConfiguration(EvaluationExecuterConfiguration configuration,
            int minimumPriority, int minimumUniqueId, int maximumUniqueId) {
        if (configuration.getPriority() < minimumPriority) {
            LOGGER.debug("Skipping '{}' with priority {} because of minimumPriority {} ",
                    configuration.getName(), configuration.getPriority(), minimumPriority);
            return false;
        }
        if (configuration.getUniqueId() < minimumUniqueId) {
            LOGGER.info("Skipping '{}' with priority {} because of minimumUniqueId {} ",
                    configuration.getName(), configuration.getUniqueId(), minimumUniqueId);
            return false;
        }
        if (configuration.getUniqueId() > maximumUniqueId) {
            LOGGER.info("Skipping '{}' with priority {} because of maximumUniqueId {} ",
                    configuration.getName(), configuration.getUniqueId(), maximumUniqueId);
            return false;
        }
        return true;
    }

    @Override
    public void stopOnNextConfiguration() {
        LOGGER.info("Will stop gracefully on next finished configuration.");
        endGracefully = true;
    }
}