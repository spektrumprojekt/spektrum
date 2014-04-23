package de.spektrumprojekt.i.evaluation.runner.aggregator.compare;

import org.apache.commons.lang3.StringUtils;

import de.spektrumprojekt.commons.output.SpektrumParseableElement;

/**
 * Format of data file is
 * 
 * <pre>
 * # Data columns: index Min 1stQuartile Median 3rdQuartile Max Mean BoxWidth Titles
 * 1 5 7 10 15 24 6 0.3 Quick
 * 2 6 8 11 16 23 6 0.4 Fox
 * 3 5 7 11 17 22 6 0.5 Lazy
 * 4 6 9 10 18 21 6 0.3 Dog
 * </pre>
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class EvaluationRunResult implements SpektrumParseableElement {

    public static String getColumnHeaders() {
        return "index min quartile1st median quartile3rd max mean boxWidth title";
    }

    private int index;

    private double mean;

    private double min;

    private double quartile1st;

    private double median;

    private double quartile3rd;

    private double max;

    private double boxWidth = 0.5;

    private String title;

    public EvaluationRunResult() {
    }

    public EvaluationRunResult(String line) {
        if (line == null) {
            throw new IllegalArgumentException("line cannot be null");
        }
        String[] vals = line.split(" ");
        int index = 0;
        index = Integer.parseInt(vals[index++]);
        min = Double.parseDouble(vals[index++]);
        quartile1st = Double.parseDouble(vals[index++]);
        median = Double.parseDouble(vals[index++]);
        quartile3rd = Double.parseDouble(vals[index++]);
        max = Double.parseDouble(vals[index++]);
        mean = Double.parseDouble(vals[index++]);
        boxWidth = Double.parseDouble(vals[index++]);
        title = vals[index++];
    }

    public double getBoxWidth() {
        return boxWidth;
    }

    public int getIndex() {
        return index;
    }

    public double getMax() {
        return max;
    }

    public double getMean() {
        return mean;
    }

    public double getMedian() {
        return median;
    }

    public double getMin() {
        return min;
    }

    public double getQuartile1st() {
        return quartile1st;
    }

    public double getQuartile3rd() {
        return quartile3rd;
    }

    public String getTitle() {
        return title;
    }

    public boolean isValid() {
        return min <= quartile1st && quartile1st <= median && median <= quartile3rd
                && quartile3rd <= max
                && title != null && min <= mean && mean <= max;
    }

    public void setBoxWidth(double boxWidth) {
        this.boxWidth = boxWidth;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public void setQuartile1st(double quartile1st) {
        this.quartile1st = quartile1st;
    }

    public void setQuartile3rd(double quartile3rd) {
        this.quartile3rd = quartile3rd;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String toParseableString() {
        return StringUtils.join(new String[] {
                String.valueOf(index),
                String.valueOf(min),
                String.valueOf(quartile1st),
                String.valueOf(median),
                String.valueOf(quartile3rd),
                String.valueOf(max),
                String.valueOf(mean),
                String.valueOf(boxWidth),
                "\"" + title + "\""
        }, " ");
    }
}