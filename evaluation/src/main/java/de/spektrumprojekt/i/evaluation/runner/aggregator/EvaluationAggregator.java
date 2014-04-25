package de.spektrumprojekt.i.evaluation.runner.aggregator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.i.evaluation.measure.SpecificMeasure;

public class EvaluationAggregator {

    public static final String AGGREGATOR_LATEST_CSV = "aggregator-latest.csv";

    private final static org.slf4j.Logger LOGGER = LoggerFactory
            .getLogger(EvaluationAggregator.class);

    public static List<Map<String, String>> readAggregate(String fullFilename) throws IOException {

        final String sep = "\\s+";

        List<String> lines = FileUtils.readLines(new File(fullFilename));

        String[] header = lines.get(0).split(sep);
        int hi = 0;
        for (String head : header) {
            LOGGER.info(hi + " " + head);
            if (hi == 8 && !head.equals("f1score")) {
                throw new RuntimeException("Header error, f1score should be of index 8");
            }
            hi++;
        }

        List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
        for (int i = 1; i < lines.size(); i++) {
            String[] cells = lines.get(i).split(sep);
            Map<String, String> cellMap = new HashMap<String, String>();
            cell: for (int c = 0; c < cells.length; c++) {
                if (c >= header.length) {
                    break cell;
                }
                String colName = header[c];
                String val = cells[c];
                cellMap.put(colName, val);
            }
            rows.add(cellMap);
        }
        return rows;
    }

    private final List<File> prFiles;

    private final File outputEvalDirectory;
    private final Pattern decimalPattern = Pattern.compile("\\s\\d+.\\d*");

    private final Pattern rmsePattern = Pattern.compile("rmse=\\d+.\\d*");

    private List<RunStats> runStatistics = new ArrayList<RunStats>();

    private String aggregatorFilename;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    private final List<String> allSpecificFeatureNames = new ArrayList<String>();

    public EvaluationAggregator(List<File> prFiles, String outputEvalDirectory) {
        if (prFiles == null) {
            throw new IllegalArgumentException(prFiles + " cannot be null!");
        }
        this.prFiles = prFiles;
        this.outputEvalDirectory = new File(outputEvalDirectory);
    }

    public String getAggregatorFile() {
        return aggregatorFilename;
    }

    private double getFirstNumber(String line) {

        Matcher matcher = decimalPattern.matcher(line);

        if (!matcher.find()) {
            throw new RuntimeException("Did not found a number for line=" + line);
        }
        return Double.parseDouble(matcher.group());
    }

    private double getRmse(String line, boolean throwOnMissingValue) {
        Matcher matcher = rmsePattern.matcher(line);

        if (!matcher.find()) {
            if (throwOnMissingValue) {
                throw new RuntimeException("Did not found rmse for line=" + line);
            }
            return -1;
        }
        String match = matcher.group();
        match = match.replace("rmse=", "");
        return Double.parseDouble(match);
    }

    public List<RunStats> getRunStatistics() {
        return runStatistics;
    }

    private void parseFile(File eval) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(eval);

            List<String> lines = IOUtils.readLines(fis);
            RunStats stats = new RunStats(eval.getName());
            stats.setLastModifiedDate(new Date(eval.lastModified()));
            for (String line : lines) {
                String measure = null;
                double val = 0;

                line = removeComment(line);
                if (line.startsWith("evaluator")) {
                    val = getRmse(line, false);
                    measure = "rmse";
                } else if (line.startsWith("SpecificMeasureMax:")) {

                    line = line.replace("SpecificMeasureMax:", "");

                    int col = line.indexOf(":");
                    measure = line.substring(0, col).trim();
                    val = getFirstNumber(line);
                }

                if (measure != null) {
                    SpecificMeasure specificMeasure = new SpecificMeasure(measure, val, line);
                    if (!allSpecificFeatureNames.contains(measure)) {
                        allSpecificFeatureNames.add(measure);
                    }
                    stats.getSpecificMeasures().put(specificMeasure.getName(), specificMeasure);
                }
            }
            this.runStatistics.add(stats);

        } catch (Exception e) {
            LOGGER.error("Error parsing " + eval + " " + e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

    private String removeComment(String line) {
        line = line.trim();
        if (line.charAt(0) == '#') {
            line = line.substring(1).trim();
        }
        return line;
    }

    public void run() throws Exception {
        this.scanFiles();
        this.writeAggregate();
    }

    private void scanFiles() throws IOException {

        for (File file : this.prFiles) {
            this.parseFile(file);
        }

    }

    private void writeAggregate() throws IOException {
        List<String> lines = new ArrayList<String>();
        lines.add(RunStats.getHeader(allSpecificFeatureNames));

        for (RunStats stat : this.runStatistics) {

            String line = stat.toString(allSpecificFeatureNames);
            line += stat.getSplittedName();

            lines.add(line);
        }

        this.aggregatorFilename = this.outputEvalDirectory + File.separator
                + "aggregator" + dateFormat.format(new Date()) + ".csv";

        String currentFilename = this.outputEvalDirectory + File.separator
                + AGGREGATOR_LATEST_CSV;

        FileUtils.writeLines(new File(this.aggregatorFilename), lines);
        FileUtils.writeLines(new File(currentFilename), lines);
    }

}
