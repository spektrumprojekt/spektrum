package de.spektrumprojekt.informationextraction.relations;

import java.util.Collection;
import java.util.regex.Pattern;

public interface PatternConsolidationConfiguration {

    public abstract Collection<Pattern> getPatterns();

    public abstract Long getPeriodOfTime();

    public abstract void setPatters(Collection<String> regExes);

    public abstract void setPeriodOfTime(Long millis);
}
