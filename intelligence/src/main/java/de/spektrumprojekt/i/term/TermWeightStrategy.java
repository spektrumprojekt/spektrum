package de.spektrumprojekt.i.term;

public enum TermWeightStrategy {
    /** just a weight of 1 */
    TRIVIAL,
    LINEAR_INVERSE_TERM_FREQUENCY,
    INVERSE_TERM_FREQUENCY;
}