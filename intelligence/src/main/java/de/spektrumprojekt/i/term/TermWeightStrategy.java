package de.spektrumprojekt.i.term;

public enum TermWeightStrategy {
    /** just a weight of 1 */
    TRIVIAL("tri"),
    LINEAR_INVERSE_TERM_FREQUENCY("linv"),
    INVERSE_TERM_FREQUENCY("inv");

    private final String shortName;

    private TermWeightStrategy(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

}