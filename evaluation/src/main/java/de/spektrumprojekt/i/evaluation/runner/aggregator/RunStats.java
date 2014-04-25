package de.spektrumprojekt.i.evaluation.runner.aggregator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.spektrumprojekt.i.evaluation.measure.SpecificMeasure;

public class RunStats {

    private final static String STRING_PREFIX = "\"";
    private final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
    private final static DateFormat TIME_FORMAT = new SimpleDateFormat("HH:ss:mm");

    public static String getHeader(List<String> featureOrder) {
        StringBuilder sb = new StringBuilder();
        sb.append("name ");
        sb.append("date ");
        sb.append("time ");
        for (String feature : featureOrder) {
            sb.append(" " + feature);
        }
        return sb.toString();
    }

    private Map<String, SpecificMeasure> specificMeasures = new HashMap<String, SpecificMeasure>();
    private final String name;

    private Date lastModifiedDate;

    public RunStats(String name) {
        this.name = name;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public String getName() {
        return name;
    }

    public Map<String, SpecificMeasure> getSpecificMeasures() {
        return specificMeasures;
    }

    public String getSplittedName() {
        return this.name.replace("_", " ").replace("-", " ").replace(".", " ");
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public void setSpecificMeasures(Map<String, SpecificMeasure> specificMeasures) {
        this.specificMeasures = specificMeasures;
    }

    public String toString(List<String> featureOrder) {
        StringBuilder sb = new StringBuilder();
        sb.append(name + " ");
        if (this.lastModifiedDate == null) {
            sb.append(STRING_PREFIX + "-" + STRING_PREFIX + " ");
            sb.append(STRING_PREFIX + "-" + STRING_PREFIX);
        } else {
            sb.append(STRING_PREFIX + DATE_FORMAT.format(this.lastModifiedDate) + STRING_PREFIX
                    + " ");
            sb.append(STRING_PREFIX + TIME_FORMAT.format(this.lastModifiedDate) + STRING_PREFIX);
        }
        for (String feature : featureOrder) {
            SpecificMeasure measure = this.specificMeasures.get(feature);
            if (measure == null) {
                sb.append(" -1");
            } else {
                sb.append(" " + measure.getValue());
            }
        }
        return sb.toString();
    }
}