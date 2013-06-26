package de.spektrumprojekt.informationextraction.relations;

import java.util.Collection;

public interface PatternConsolidationConfiguration {

    Collection<NamePattern> getPatterns();

    Long getPeriodOfTime();

    void setPatterns(Collection<NamePattern> patterns);

    void setPeriodOfTime(Long millis);
}
