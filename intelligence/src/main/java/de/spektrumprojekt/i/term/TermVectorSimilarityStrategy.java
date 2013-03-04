package de.spektrumprojekt.i.term;

public enum TermVectorSimilarityStrategy {

    MAX("max"),

    AVG("avg"),

    COSINUS("cos");

    private final String shortName;

    private TermVectorSimilarityStrategy(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }
}