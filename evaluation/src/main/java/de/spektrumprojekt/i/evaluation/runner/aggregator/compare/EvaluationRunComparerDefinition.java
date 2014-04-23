package de.spektrumprojekt.i.evaluation.runner.aggregator.compare;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;

public class EvaluationRunComparerDefinition {

    private final List<ImmutablePair<String, File>> labelToFiles = new ArrayList<ImmutablePair<String, File>>();
    private String title;
    private String seriesLabel;
    private String xlabel;

    private String ylabel = "Value";

    private final String shortName;
    private final String comparingMeasure;

    public EvaluationRunComparerDefinition(String comparingMeasure, String shortName,
            String title) {
        if (comparingMeasure == null) {
            throw new IllegalArgumentException("comparingMeasure cannot be null");
        }
        if (shortName == null) {
            throw new IllegalArgumentException("shortName cannot be null");
        }
        this.shortName = shortName;
        this.title = title;
        this.comparingMeasure = comparingMeasure;
    }

    public String getComparingMeasure() {
        return comparingMeasure;
    }

    public List<ImmutablePair<String, File>> getLabelToFiles() {
        return labelToFiles;
    }

    public String getSeriesLabel() {
        return seriesLabel;
    }

    /**
     * 
     * @return used as filename for the plt file
     */
    public String getShortName() {
        return shortName;
    }

    public String getTitle() {
        return title;
    }

    public String getXlabel() {
        return xlabel;
    }

    public String getYlabel() {
        return ylabel;
    }

    public void setSeriesLabel(String seriesLabel) {
        this.seriesLabel = seriesLabel;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setXlabel(String xlabel) {
        this.xlabel = xlabel;
    }

    public void setYlabel(String ylabel) {
        this.ylabel = ylabel;
    }
}