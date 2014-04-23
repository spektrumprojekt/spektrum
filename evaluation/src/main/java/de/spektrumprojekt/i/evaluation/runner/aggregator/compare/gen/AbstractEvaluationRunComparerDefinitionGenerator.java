package de.spektrumprojekt.i.evaluation.runner.aggregator.compare.gen;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.i.evaluation.runner.aggregator.compare.EvaluationRunComparerDefinition;

public abstract class AbstractEvaluationRunComparerDefinitionGenerator implements
        EvaluationRunComparerDefinitionGenerator {

    public enum TopTimeBins {
        top10Day,
        top25Day,
        top50Week,
        top100Week;
    }

    private static final FileCache FILE_CACHE = new FileCache();

    private final static Logger LOGGER = LoggerFactory
            .getLogger(AbstractEvaluationRunComparerDefinitionGenerator.class);
    private final String evaluationResultsPath;

    private final File evaluationResultsPathFile;

    private int warnings;

    public AbstractEvaluationRunComparerDefinitionGenerator(String evaluationResultsPath) {
        if (evaluationResultsPath == null) {
            throw new IllegalArgumentException("evaluationResultsPath cannot be null");
        }
        this.evaluationResultsPath = evaluationResultsPath;
        this.evaluationResultsPathFile = new File(this.evaluationResultsPath);
    }

    protected void findAndSetFiles(EvaluationRunComparerDefinition definition,
            Map<String, String[]> configNames) {
        List<File> files = FILE_CACHE.getFiles(evaluationResultsPathFile);

        for (Entry<String, String[]> configName : configNames.entrySet()) {
            File fnd = null;
            files: for (File file : files) {
                for (String name : configName.getValue()) {
                    if (!file.getName().contains(name)) {
                        // if one name part does not match, skip this file and take next
                        continue files;

                    }
                }

                fnd = file;
                break files;
            }
            if (fnd == null) {
                warnings++;
                LOGGER.warn("No file found for configName="
                        + Arrays.toString(configName.getValue()) + " shortName="
                        + configName.getKey());
            } else {
                LOGGER.info("Adding {}:{} for comparison.", configName.getKey(), fnd);
                definition.getLabelToFiles().add(
                        new ImmutablePair<String, File>(configName.getKey(), fnd));
            }
        }
    }

    public String getEvaluationResultsPath() {
        return evaluationResultsPath;
    }

    public File getEvaluationResultsPathFile() {
        return evaluationResultsPathFile;
    }

    public String getTopReadableString(String top) {
        top = top.toLowerCase();
        top = top.replace("top", "Top ");
        top = top.replace("week", " Week");
        top = top.replace("day", "  Day");
        return top;
    }

    public int getWarnings() {
        return warnings;
    }

}