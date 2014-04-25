package de.spektrumprojekt.i.evaluation.measure;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpecificMeasure {

    public static Map<String, SpecificMeasure> getMap(List<SpecificMeasure> measureList) {
        Map<String, SpecificMeasure> measuresMap = new HashMap<String, SpecificMeasure>();
        for (SpecificMeasure measure : measureList) {
            measuresMap.put(measure.getName(), measure);
        }
        return measuresMap;
    }

    public static String getString(Collection<SpecificMeasure> measures) {
        String str = "";
        String prefix = "";
        for (SpecificMeasure specific : measures) {
            str += prefix + specific.getName() + ": " + specific.getValue();
            prefix = " ";
        }
        return str;
    }

    private final String name;
    private final String description;
    private String others;
    private final double value;

    public SpecificMeasure(String name, double value, String description) {
        this.name = name;
        this.value = value;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getOthers() {
        return others;
    }

    public double getValue() {
        return value;
    }

    public void setOthers(String others) {
        this.others = others;
    }

    @Override
    public String toString() {
        return name + " : " + value + " [ " + description + " ] ( " + others + " )";
    }
}