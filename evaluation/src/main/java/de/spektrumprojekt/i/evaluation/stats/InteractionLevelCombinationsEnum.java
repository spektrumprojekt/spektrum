package de.spektrumprojekt.i.evaluation.stats;

public enum InteractionLevelCombinationsEnum {
    ALL("allI"),
    DIRECT("dirI"),
    INDIRECT("indirI"),
    NONE("noneI"),
    NOTDIRECT("notDI");

    private final String shortName;

    private InteractionLevelCombinationsEnum(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }
}