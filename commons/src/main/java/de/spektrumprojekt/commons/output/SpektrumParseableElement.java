package de.spektrumprojekt.commons.output;

/**
 * Implementing classes should generate a string that can be easily parsed through the constructor
 * of the class.
 * 
 */
public interface SpektrumParseableElement {

    public String toParseableString();
}