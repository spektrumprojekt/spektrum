package de.spektrumprojekt.datamodel.message;

public enum InteractionLevel {
    /** nothing known */
    NONE(0),
    /**
     * the user participated in the discussion or was mentioned but this message is not in the
     * direct reply chain of the participation
     */
    INDIRECT(1),
    /**
     * the user interacted directly with the message, which includes a mention or author, or a reply
     * chain
     */
    DIRECT(2);

    public static InteractionLevel fromNumberValue(int numberValue) {
        for (InteractionLevel level : InteractionLevel.values()) {
            if (level.getNumberValue() == numberValue) {
                return level;
            }
        }
        return null;
    }

    private final int numberValue;

    private InteractionLevel(int value) {
        this.numberValue = value;
    }

    public int getNumberValue() {
        return numberValue;
    }
}