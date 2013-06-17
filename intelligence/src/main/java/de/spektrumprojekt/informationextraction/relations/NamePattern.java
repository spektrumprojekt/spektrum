package de.spektrumprojekt.informationextraction.relations;

import java.util.regex.Pattern;

/**
 * <p>
 * A RegEx Pattern with an associated name.
 * </p>
 * 
 * @author Philipp Katz
 */
public class NamePattern {

    private final String name;
    private final String regex;
    private final Pattern pattern;

    public NamePattern(String name, String regex) {
        this.name = name;
        this.regex = regex;
        this.pattern = Pattern.compile(regex);
    }

    public String getName() {
        return name;
    }

    public String getRegex() {
        return regex;
    }

    public Pattern getPattern() {
        return pattern;
    }

}
