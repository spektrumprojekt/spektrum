package de.spektrumprojekt.i.evaluation.runner.aggregator;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.i.evaluation.configuration.Configuration;
import de.spektrumprojekt.i.evaluation.stats.InteractionLevelCombinationsEnum;

public class RunFilterAggregator {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(
            "yyyy/mm/dd hh:mm:ss");

    public static String[] finalList() {

        final InteractionLevelCombinationsEnum[] interactions = ArrayUtils
                .toArray(InteractionLevelCombinationsEnum.ALL
                        , InteractionLevelCombinationsEnum.NOTDIRECT
                        , InteractionLevelCombinationsEnum.NONE
                );

        final String INTERACTION = "#IA#";

        final String[] finalRuns = new String[] {

                "fth-collab-U2M-gen-fwc2-EVERY_DAY-#IA#",
                "fth-collab-U2T-gen-fwc2-EVERY_DAY-#IA#",
                "fth-collab-U2TMG-gen-fwc2-EVERY_DAY-#IA#",

                "fth-collab-U2M-s1-fwc2-EVERY_DAY-#IA#",
                "fth-collab-U2T-s1-fwc2-EVERY_DAY-#IA#",
                "fth-collab-U2TMG-s1-fwc2-EVERY_DAY-#IA#",

                "fth-random-#IA#",
                "fth-baseline-mg-#IA#",
                "fth-baseline-nmg-#IA#",
                "fth-baseline-mg-greedy-#IA#",
                "fth-baseline-nmg-greedy-#IA#",
                "fth-nocb-#IA#",
                "fth-cb-nodecay-nmg-count-#IA#",
                "fth-cb-nodecay-mg-count-#IA#",
                "fth-cb-nodecay-nmg-inc-#IA#",
                "fth-cb-nodecay-mg-inc-#IA#",

                "fth-cbtb-mgablm-bld-#IA#",
                "fth-cbtb-mgablm-blw-#IA#",

                "fth-onlycb-nodecay-mg-#IA#",

                "fth-cb-decayDay-nmg-count-#IA#",
                "fth-cb-decayWeek-nmg-count-#IA#",
                "fth-cb-decayDay-mg-count-#IA#",
                "fth-cb-decayWeek-mg-count-#IA#",

                "fth-cb-decayDay-mg-inc-#IA#",
                "fth-cb-decayWeek-mg-inc-#IA#",

                "fth-st-mg-#IA#",

                "fth-uma-jdpc-var3-topSim3-wa-adRe-#IA#-4-usim",
                "fth-uma-jdpc-var3-topSim4-wa-adRe-#IA#-4-usim",

                // 0, 1, 2, 3, 4, 5, 7, 10, 15, 20
                "fth-uma-jdpc-var3-topSim0-wa-adRe-#IA#-4-iTh0-usim",
                "fth-uma-jdpc-var3-topSim1-wa-adRe-#IA#-4-iTh0-usim",
                "fth-uma-jdpc-var3-topSim2-wa-adRe-#IA#-4-iTh0-usim",
                "fth-uma-jdpc-var3-topSim3-wa-adRe-#IA#-4-iTh0-usim",
                "fth-uma-jdpc-var3-topSim4-wa-adRe-#IA#-4-iTh0-usim",
                "fth-uma-jdpc-var3-topSim5-wa-adRe-#IA#-4-iTh0-usim",
                "fth-uma-jdpc-var3-topSim7-wa-adRe-#IA#-4-iTh0-usim",
                "fth-uma-jdpc-var3-topSim10-wa-adRe-#IA#-4-iTh0-usim",
                "fth-uma-jdpc-var3-topSim15-wa-adRe-#IA#-4-iTh0-usim",
                "fth-uma-jdpc-var3-topSim20-wa-adRe-#IA#-4-iTh0-usim",

                "fth-uma-jdpc-var4-topSim0-wa-adRe-#IA#-4-iTh0-mgsim",
                "fth-uma-jdpc-var4-topSim3-wa-adRe-#IA#-4-iTh0-mgsim",

                "fth-uma-jdpc-var5-topSim0-wa-adRe-#IA#-4-iTh0-umgsim",
                "fth-uma-jdpc-var5-topSim3-wa-adRe-#IA#-4-iTh0-umgsim",
        };

        List<String> finals = new ArrayList<String>();
        for (InteractionLevelCombinationsEnum ia : interactions) {
            for (String run : finalRuns) {

                finals.add(run.replace(INTERACTION, ia.getShortName()));
            }
        }
        return finals.toArray(new String[] { });
    }

    public static void main(String[] args) throws Exception {
        Date minDate = SIMPLE_DATE_FORMAT.parse("2014/02/06 23:00:00");

        RunFilterAggregator aggregator;

        // aggregator = new RunFilterAggregator( "umaCompareRuns.csv", minDate, umaCompareList() );
        aggregator = new RunFilterAggregator(
                "finalRuns.csv",
                minDate,
                finalList()
                );
        // aggregator.setAggregatorFilename("H:" + File.separator +
        // EvaluationAggregator.AGGREGATOR_LATEST_CSV);
        aggregator.init();
        aggregator.run();

    }

    public static String[] umaCompareList() {
        final String[] umaCompareRuns = new String[] {

                "fth-uma-jdpc-var3-topSim1-wa-adRe-allI-4-usim",
                "fth-uma-jdpc-var3-topSim2-wa-adRe-allI-4-usim",
                "fth-uma-jdpc-var3-topSim3-wa-adRe-allI-4-usim",
                "fth-uma-jdpc-var3-topSim4-wa-adRe-allI-4-usim",
                "fth-uma-jdpc-var3-topSim5-wa-adRe-allI-4-usim",
                "fth-uma-jdpc-var3-topSim7-wa-adRe-allI-4-usim",
                "fth-uma-jdpc-var3-topSim10-wa-adRe-allI-4-usim",
                "fth-uma-jdpc-var3-topSim15-wa-adRe-allI-4-usim",
                "fth-uma-jdpc-var3-topSim20-wa-adRe-allI-4-usim",
                "fth-uma-jdpc-var3-topSim0-wa-adRe-allI-4-usim",

                "fth-uma-jdpc-var3-topSim1-wa-adAll-allI-4-usim",
                "fth-uma-jdpc-var3-topSim2-wa-adAll-allI-4-usim",
                "fth-uma-jdpc-var3-topSim3-wa-adAll-allI-4-usim",
                "fth-uma-jdpc-var3-topSim4-wa-adAll-allI-4-usim",
                "fth-uma-jdpc-var3-topSim5-wa-adAll-allI-4-usim",
                "fth-uma-jdpc-var3-topSim7-wa-adAll-allI-4-usim",
                "fth-uma-jdpc-var3-topSim10-wa-adAll-allI-4-usim",
                "fth-uma-jdpc-var3-topSim15-wa-adAll-allI-4-usim",
                "fth-uma-jdpc-var3-topSim20-wa-adAll-allI-4-usim",
                "fth-uma-jdpc-var3-topSim0-wa-adAll-allI-4-usim",

                "fth-uma-jdpc-var3-topSim1-wa-adRe-noneDI-4-usim",
                "fth-uma-jdpc-var3-topSim2-wa-adRe-noneDI-4-usim",
                "fth-uma-jdpc-var3-topSim3-wa-adRe-noneDI-4-usim",
                "fth-uma-jdpc-var3-topSim4-wa-adRe-noneDI-4-usim",
                "fth-uma-jdpc-var3-topSim5-wa-adRe-noneDI-4-usim",
                "fth-uma-jdpc-var3-topSim7-wa-adRe-noneDI-4-usim",
                "fth-uma-jdpc-var3-topSim10-wa-adRe-noneDI-4-usim",
                "fth-uma-jdpc-var3-topSim15-wa-adRe-noneDI-4-usim",
                "fth-uma-jdpc-var3-topSim20-wa-adRe-noneDI-4-usim",
                "fth-uma-jdpc-var3-topSim0-wa-adRe-noneDI-4-usim",

                "fth-uma-jdpc-var3-topSim1-wa-adAll-noneDI-4-usim",
                "fth-uma-jdpc-var3-topSim2-wa-adAll-noneDI-4-usim",
                "fth-uma-jdpc-var3-topSim3-wa-adAll-noneDI-4-usim",
                "fth-uma-jdpc-var3-topSim4-wa-adAll-noneDI-4-usim",
                "fth-uma-jdpc-var3-topSim5-wa-adAll-noneDI-4-usim",
                "fth-uma-jdpc-var3-topSim7-wa-adAll-noneDI-4-usim",
                "fth-uma-jdpc-var3-topSim10-wa-adAll-noneDI-4-usim",
                "fth-uma-jdpc-var3-topSim15-wa-adAll-noneDI-4-usim",
                "fth-uma-jdpc-var3-topSim20-wa-adAll-noneDI-4-usim",
                "fth-uma-jdpc-var3-topSim0-wa-adAll-noneDI-4-usim"
        };
        return umaCompareRuns;
    }

    private final String path = Configuration.INSTANCE.getEvaluationResultsDir();

    private List<Map<String, String>> rows;

    private final String[] runNames;

    private final String[] topNames = new String[] {
            "0.9",
            "top5Day.ranks.eval-0.9",
            "top10Day.ranks.eval-0.9",
            "top10Week.ranks.eval-0.9",
            "top20Week.ranks.eval-0.9"
    };

    private final String[] standardMeasures = new String[] {
            "f1score",
            "f2score",
    };

    private final String[] topMeasures = new String[] {
            "precisionAtOfAllTimeBins",
            "timeBinMeanAveragePrecision"
    };

    private final static Logger LOGGER = LoggerFactory.getLogger(RunFilterAggregator.class);

    private Date minDate;

    private final String outFileName;

    private String aggregatorFilename = path + File.separator
            + EvaluationAggregator.AGGREGATOR_LATEST_CSV;

    public RunFilterAggregator(String outFilename, Date minDate, String... runNames) {
        this.runNames = runNames;
        this.minDate = minDate;
        this.outFileName = outFilename;
    }

    public String getAggregatorFilename() {
        return aggregatorFilename;
    }

    public void init() throws IOException {
        rows = EvaluationAggregator.readAggregate(aggregatorFilename);
    }

    public void run() throws Exception {

        StringBuilder header = new StringBuilder();

        header.append("name ");
        for (String topName : topNames) {
            String[] measures = topName.startsWith("top") ? topMeasures : standardMeasures;
            for (String measure : measures) {
                String n = topName.replace(".ranks.eval-0.9", "-");
                n = n.replace("0.9", "");
                header.append(n + measure + " ");
            }
        }

        DateFormat format = SIMPLE_DATE_FORMAT;

        List<String> aggregatedRows = new ArrayList<String>();
        for (String runName : runNames) {

            Map<String, Map<String, String>> topNamesToRow = new HashMap<String, Map<String, String>>();
            for (Map<String, String> row : rows) {
                String name = row.get("name");
                if (name == null) {
                    throw new RuntimeException("No name column found for row " + row.toString());
                }
                String dateS = row.get("date").replace("\"", "");
                String time = row.get("time").replace("\"", "");
                Date date = format.parse(dateS + " " + time);
                if (date.after(minDate)) {
                    for (String topName : topNames) {

                        String match = "_" + runName + "-" + topName;
                        if (name.contains(match)) {
                            topNamesToRow.put(topName, row);
                        }
                    }
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append(runName);
            sb.append(" ");
            topName: for (String topName : topNames) {
                String[] measures = topName.startsWith("top") ? topMeasures : standardMeasures;
                Map<String, String> row = topNamesToRow.get(topName);
                if (row == null) {
                    LOGGER.warn("No measures found for runName {} and topName {}.",
                            runName, topName);
                    continue topName;
                }
                for (String measure : measures) {
                    String val = row.get(measure);
                    if (val == null) {
                        LOGGER.warn("No {} measure found for runName {} and topName {}.", measure,
                                runName, topName);
                        val = "0";
                    }
                    sb.append(val);
                    sb.append(" ");
                }
            }
            aggregatedRows.add(sb.toString());
        }

        String out = "\n" + header + "\n" + StringUtils.join(aggregatedRows, "\n");

        System.out.println(out);

        List<String> fileOut = new ArrayList<String>();
        fileOut.add(header.toString());
        fileOut.addAll(aggregatedRows);

        FileUtils.writeLines(new File(this.path + File.separator + outFileName), fileOut);
    }

    public void setAggregatorFilename(String aggregatorFilename) {
        this.aggregatorFilename = aggregatorFilename;
    }
}
