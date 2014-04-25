/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.spektrumprojekt.i.evaluation.measure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.computer.Computer;
import de.spektrumprojekt.i.evaluation.runner.StatsPerUserTimeBin;

public class MeasureComputer<T extends Measure> implements Computer {

    private final static String resultsDir = "D:/work/Thesis/dev/evaluations/discussion simulation clean/";

    /**
     * @param args
     */
    public static void main(String[] args) {

        try {
            MeasureComputer<PrecisionRecallMeasure> gen = new MeasureComputer<PrecisionRecallMeasure>(
                    resultsDir + File.separator + "no_discussion_ranker_avg",
                    new PrecisionRecallMeasureFactory(),
                    0.9f
                    );
            gen.run();
            gen.outResults();
            LOGGER.info("Finished in success");

        } catch (Exception e) {
            System.err.println("We failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private final float callTheGoalPositive;

    private EvaluatorOutput evaluatorOutput;

    private static final Logger LOGGER = LoggerFactory.getLogger(MeasureComputer.class);

    private List<Measure> measures = new ArrayList<Measure>();
    private final String evaluatorOutputFilename;

    private String resultFileName;

    private Map<String, SpecificMeasure> maxMeasures = new HashMap<String, SpecificMeasure>();

    private final MeasureFactory<T> measureFactory;

    private StatsPerUserTimeBin userTimeBinStats;

    private Map<String, Integer> timeBinUserIdToRelevantCount;

    public MeasureComputer(String evaluatorOutputFilename, MeasureFactory<T> measureFactory,
            float callTheGoalPositive) {
        if (evaluatorOutputFilename == null) {
            throw new IllegalArgumentException("evaluatorOutpurFilename cannot be null.");
        }
        if (measureFactory == null) {
            throw new IllegalArgumentException("measureFactory cannot be null.");
        }
        this.measureFactory = measureFactory;
        this.evaluatorOutputFilename = evaluatorOutputFilename;
        this.callTheGoalPositive = callTheGoalPositive;
    }

    public float getCallTheGoalPositive() {
        return callTheGoalPositive;
    }

    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    public EvaluatorOutput getEvaluatorOutput() {
        return evaluatorOutput;
    }

    public List<Measure> getMeasures() {
        return measures;
    }

    public String getResultFilename() {
        return resultFileName;
    }

    public String getResultFileName() {
        return resultFileName;
    }

    public boolean isUseTimeBins() {
        return this.measureFactory instanceof TimeBinnedMeasureFactory;
    }

    public void outResults() throws IOException {

        LOGGER.info("Start out results ...");

        MeasureOutput<Measure> output = new MeasureOutput(measureFactory);

        if (evaluatorOutput.getDescriptions() != null) {
            output.getDescriptions().addAll(evaluatorOutput.getDescriptions());
        }
        for (SpecificMeasure sm : this.maxMeasures.values()) {
            output.getDescriptions().add("SpecificMeasureMax: " + sm);
        }

        output.getElements().addAll(this.measures);
        output.write(resultFileName);

        LOGGER.info("Finished out results in success");

    }

    public void prepare(Collection<EvaluatorDataPoint> dataPoints) {
        this.evaluatorOutput = new EvaluatorOutput();
        this.evaluatorOutput.getElements().addAll(dataPoints);
    }

    public void run() throws Exception {

        this.resultFileName = this.evaluatorOutputFilename + "-" + callTheGoalPositive
                + ".pr";

        LOGGER.debug("Reading results from {}", evaluatorOutputFilename);

        if (this.evaluatorOutput == null) {
            evaluatorOutput = new EvaluatorOutput();
            evaluatorOutput.read(evaluatorOutputFilename);
            timeBinUserIdToRelevantCount = this.evaluatorOutput.getTimebinRelevantCounts();
        }

        if (this.evaluatorOutput.getElements().size() == 0) {
            LOGGER.warn("No datapoints to use? Did you call prepare?");
            return;
        }

        userTimeBinStats = StatsPerUserTimeBin.getFromComments(this.evaluatorOutput
                .getDescriptions());

        if (isUseTimeBins() && userTimeBinStats == null) {
            throw new RuntimeException(
                    "We are using time bins, but not StatsPerUserTimeBin are available!");
        }

        int numberOfSteps = 1000;
        double stepWidth = 1 / (double) numberOfSteps;
        LOGGER.info("Running with {} datapoints ...", evaluatorOutput.getElements().size());

        int percentage;
        int lastPercentage = -1;

        for (int i = 0; i <= numberOfSteps + 1; i++) {
            Measure measure = run(i * stepWidth);
            measures.add(measure);

            setMaximumMeasures(measure);

            percentage = 100 * i / (numberOfSteps + 1);
            if (lastPercentage < percentage && percentage % 10 == 0) {
                lastPercentage = percentage;
                LOGGER.debug("Running Progress: " + percentage + "%");
            }
        }
        LOGGER.info("Run done.");

    }

    private Measure run(double letTheComputedBePositive) {

        Measure measure = measureFactory.createMeasure(
                letTheComputedBePositive,
                callTheGoalPositive,
                this.userTimeBinStats,
                this.timeBinUserIdToRelevantCount);
        measure.setOverallItems(this.evaluatorOutput.getOverallItems());

        for (EvaluatorDataPoint dataPoint : this.evaluatorOutput.getElements()) {

            measure.addDataPoint(dataPoint);
        }

        measure.finalize();
        return measure;
    }

    private void setMaximumMeasures(Measure measure) {
        Map<String, SpecificMeasure> measureMap = measure.getFinalMeasures();
        for (SpecificMeasure sm : measureMap.values()) {
            SpecificMeasure max = this.maxMeasures.get(sm.getName());
            if (max == null || sm.getValue() > max.getValue()) {
                this.maxMeasures.put(sm.getName(), sm);
            }
        }
    }

}
