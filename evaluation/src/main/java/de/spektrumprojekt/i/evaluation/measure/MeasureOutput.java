package de.spektrumprojekt.i.evaluation.measure;

import java.io.IOException;
import java.util.List;

import de.spektrumprojekt.commons.output.SpektrumParseableElementFileOutput;

public class MeasureOutput<T extends Measure> extends SpektrumParseableElementFileOutput<T> {

    private final static String SPECIFIC_MEASURE_MAX = "SpecificMeasureMax";

    public final static String TIME_BIN_MEAN_AVERAGE_PRECISION = "timeBinMeanAveragePrecision";
    public final static String F1 = "f1score";

    private final MeasureFactory<T> measureFactory;

    public MeasureOutput(MeasureFactory<T> measureFactory) {
        super(measureFactory.getMeasureClass());
        this.measureFactory = measureFactory;
    }

    @Override
    protected T createNewElement(String line) {
        return this.measureFactory.createMeasure(line);
    }

    public String extractMeasure(String comparingMeasure) {
        List<String> specificMeasures = this.getDescriptionValues(SPECIFIC_MEASURE_MAX);

        String measureExtracted = null;
        specificMeasure: for (String specificMeasure : specificMeasures) {
            if (specificMeasure.trim().startsWith(comparingMeasure)) {
                int index = specificMeasure.trim().indexOf(":");
                measureExtracted = specificMeasure.substring(index + 1).trim();
                break specificMeasure;
            }
        }
        return measureExtracted;
    }

    public double extractMeasureValue(String comparingMeasure) {
        String measure = extractMeasure(comparingMeasure);
        String[] vals = measure.split(" ");
        return Double.parseDouble(vals[0]);
    }

    @Override
    protected String getHeader() {
        return measureFactory.getColumnMeasureHeaders();
    }

    @Override
    public void read(String filename) throws IOException {
        super.read(filename);

        // TODO parse the specific measure
    }

}