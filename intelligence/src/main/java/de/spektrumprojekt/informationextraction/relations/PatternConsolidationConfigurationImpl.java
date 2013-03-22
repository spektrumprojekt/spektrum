package de.spektrumprojekt.informationextraction.relations;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

public class PatternConsolidationConfigurationImpl implements PatternConsolidationConfiguration {

    private Collection<Pattern> patterns;

    private Long periodOfTime = 1000 * 60 * 60 * 4L;

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
