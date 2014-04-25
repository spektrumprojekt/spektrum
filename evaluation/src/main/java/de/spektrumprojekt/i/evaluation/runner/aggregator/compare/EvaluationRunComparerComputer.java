package de.spektrumprojekt.i.evaluation.runner.aggregator.compare;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.computer.Computer;
import de.spektrumprojekt.i.evaluation.configuration.Configuration;
import de.spektrumprojekt.i.evaluation.measure.MeasureOutput;
import de.spektrumprojekt.i.evaluation.measure.TimeBinnedMeasure;
import de.spektrumprojekt.i.evaluation.measure.TimeBinnedMeasureFactory;
import de.spektrumprojekt.i.evaluation.runner.aggregator.compare.gen.EvaluationRunComparerDefinitionGenerator;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class EvaluationRunComparerComputer implements Computer {

    private final String evaluationResultsPath;
    private final String compareResultsPath;

    private final static Logger LOGGER = LoggerFactory
            .getLogger(EvaluationRunComparerComputer.class);

    private final static String TEMPLATE = "gnuplot/compare.template.plt";

    private String templateContent;

    private int warnings;

    private int errors;

    private final List<EvaluationRunComparerDefinition> evaluationRunComparerDefinitions = new ArrayList<EvaluationRunComparerDefinition>();

    public EvaluationRunComparerComputer(
            EvaluationRunComparerDefinitionGenerator evaluationRunComparerDefinitionGenerator) {

        this.evaluationResultsPath = Configuration.INSTANCE.getEvaluationDirectoryFull();
        if (this.evaluationResultsPath == null) {
            throw new IllegalArgumentException(
                    " evaluationResultsPath (read from configuration) cannot be null.");
        }
        this.compareResultsPath = FilenameUtils
                .concat(
                        Configuration.INSTANCE.getEvaluationResultsDir(), "compare");
        File compare = new File(this.compareResultsPath);
        if (!compare.exists()) {
            compare.mkdir();
        }

        if (evaluationRunComparerDefinitionGenerator == null) {
            throw new IllegalArgumentException(
                    "evaluationRunComparerDefinitionGenerator cannot be null.");
        } else {
            this.evaluationRunComparerDefinitions.addAll(evaluationRunComparerDefinitionGenerator
                    .createDefinitions());

            if (evaluationRunComparerDefinitionGenerator.getWarnings() > 0) {
                LOGGER.warn("Got {} warnings for generator {}",
                        evaluationRunComparerDefinitionGenerator.getWarnings(),
                        evaluationRunComparerDefinitionGenerator.getClass().getName());
            }
        }
    }

    private EvaluationComparisonOutput createEvaluationOutput(
            EvaluationRunComparerDefinition definition)
            throws IOException {
        EvaluationComparisonOutput output = new EvaluationComparisonOutput();
        for (ImmutablePair<String, File> labelToFile : definition.getLabelToFiles()) {

            try {

                runFile(definition, output, labelToFile);
                output.getDescriptions().add(
                        "input " + labelToFile.getLeft() + " : " + labelToFile.getRight());
            } catch (Exception e) {
                LOGGER.error("Error running file=" + labelToFile.toString() + " for definition="
                        + definition, e);
                output.getDescriptions().add(
                        "input SKIPPED " + labelToFile.getLeft() + " : " + labelToFile.getRight());
            }
        }

        output.setContinuousIndex();
        return output;
    }

    private String createPltContent(EvaluationRunComparerDefinition definition,
            EvaluationComparisonOutput output, String outFilename) throws IOException {
        String template = getPltTemplateContent();

        StringBuilder pltContent = new StringBuilder();
        pltContent.append("");
        pltContent.append("set xrange [0:" + (output.getElements().size() + 1) + "]\n");
        pltContent.append("fname = '" + outFilename + "'\n");
        pltContent.append("pngname = '" + outFilename + ".png'\n");
        pltContent.append("seriesLabel = '"
                + StringUtils.defaultString(definition.getSeriesLabel())
                + "'\n");
        pltContent.append("set title '" + StringUtils.defaultString(definition.getTitle())
                + "'\n");
        pltContent.append("set xlabel '"
                + StringUtils.defaultString(definition.getXlabel())
                + "'\n");
        pltContent.append("set ylabel '"
                + StringUtils.defaultString(definition.getYlabel())
                + "'\n");

        pltContent.append(template);
        return pltContent.toString();
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    public String getEvaluationResultsPath() {
        return evaluationResultsPath;
    }

    private String getPltTemplateContent() throws IOException {
        if (templateContent == null) {
            synchronized (this) {
                if (templateContent == null) {
                    InputStream in = null;
                    try {
                        in = this.getClass().getClassLoader()
                                .getResourceAsStream(TEMPLATE);
                        if (in == null) {
                            throw new IOException("Resource not found: " + TEMPLATE);
                        }
                        templateContent = IOUtils.toString(in);
                    } finally {
                        IOUtils.closeQuietly(in);
                    }
                }
            }
        }
        return templateContent;
    }

    protected void incrementWarning(int increment) {
        this.warnings += increment;
    }

    @Override
    public void run() throws IOException {

        LOGGER.info("Running ...");

        for (EvaluationRunComparerDefinition definition : evaluationRunComparerDefinitions) {

            try {
                runDefinition(definition);
            } catch (RuntimeException e) {
                LOGGER.error("Error running definition " + definition, e);
                errors++;
            }

        }

        LOGGER.info("Finished doing {} comparisons.", evaluationRunComparerDefinitions.size());
        if (warnings > 0 || errors > 0) {
            LOGGER.warn("There are {} warnings and {} errors during this run!", warnings, errors);
        } else {
            LOGGER.info("No warnings and no errors during this run. :)");
        }
    }

    private void runDefinition(EvaluationRunComparerDefinition definition) throws IOException {

        EvaluationComparisonOutput output = createEvaluationOutput(definition);

        String outFilename = FilenameUtils.concat(this.compareResultsPath,
                definition.getShortName() + ".cmp");
        output.write(outFilename);

        String pltFile = outFilename + ".plt";

        String pltContent = createPltContent(definition, output, outFilename);

        FileUtils.write(new File(pltFile), pltContent);

        LOGGER.info("Wrote data file to {}.", outFilename);
        LOGGER.info("Wrote plot file to {}.", pltFile);
    }

    private void runFile(EvaluationRunComparerDefinition definition,
            EvaluationComparisonOutput output, ImmutablePair<String, File> labelToFile)
            throws IOException {
        MeasureOutput<TimeBinnedMeasure> measureOutput = new MeasureOutput<TimeBinnedMeasure>(
                new TimeBinnedMeasureFactory());

        measureOutput.read(labelToFile.getRight().toString());

        String measureExtracted = measureOutput.extractMeasure(definition.getComparingMeasure());

        String[] splitted = measureExtracted.split(" ");

        AveragePrecisionRunResult averagePrecisionRunResult = new AveragePrecisionRunResult(
                labelToFile.getLeft());
        int index = 0;
        while (index < splitted.length) {
            String name = splitted[index++];
            name = name.replace(":", "");
            if (averagePrecisionRunResult.containsName(name)) {
                double val = Double.parseDouble(splitted[index++]);
                averagePrecisionRunResult.setValue(name, val);
            }
        }

        if (!averagePrecisionRunResult.getEvaluationRunResult().isValid()) {
            throw new RuntimeException(averagePrecisionRunResult.getEvaluationRunResult()
                    + " is not valid. ");
        }

        output.getElements().add(averagePrecisionRunResult.getEvaluationRunResult());
    }
}
