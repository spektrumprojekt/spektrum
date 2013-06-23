package de.spektrumprojekt.informationextraction.relations;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.lang3.time.DateUtils;

public class PatternConsolidationConfigurationImpl implements PatternConsolidationConfiguration {

    private Collection<NamePattern> patterns;

    private Long periodOfTime = 4 * DateUtils.MILLIS_PER_HOUR;

    public PatternConsolidationConfigurationImpl(NamePattern pattern) {
        this(Collections.singleton(pattern));
    }

    public PatternConsolidationConfigurationImpl(Collection<NamePattern> patterns) {
        setPatterns(patterns);
    }

    @Override
    public Collection<NamePattern> getPatterns() {
        return patterns;
    }

    @Override
    public Long getPeriodOfTime() {
        return periodOfTime;
    }

    @Override
    public void setPatterns(Collection<NamePattern> patterns) {
        this.patterns = new HashSet<NamePattern>();
        this.patterns.addAll(patterns);
    }

    @Override
    public void setPeriodOfTime(Long millis) {
        this.periodOfTime = millis;

    }

}
