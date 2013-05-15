package de.spektrumprojekt.informationextraction.relations;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.apache.commons.lang3.time.DateUtils;

public class PatternConsolidationConfigurationImpl implements PatternConsolidationConfiguration {

    private Collection<Pattern> patterns;

    private Long periodOfTime = 4 * DateUtils.MILLIS_PER_HOUR;

    public PatternConsolidationConfigurationImpl(Collection<String> regExes) {
        super();
        setPatters(regExes);
    }

    @Override
    public Collection<Pattern> getPatterns() {
        return patterns;
    }

    @Override
    public Long getPeriodOfTime() {
        return periodOfTime;
    }

    @Override
    public void setPatters(Collection<String> regExes) {
        patterns = new HashSet<Pattern>();
        for (String regEx : regExes) {
            patterns.add(Pattern.compile(regEx));
        }
    }

    @Override
    public void setPeriodOfTime(Long millis) {
        this.periodOfTime = millis;

    }

}
