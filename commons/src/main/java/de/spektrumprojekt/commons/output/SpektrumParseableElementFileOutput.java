package de.spektrumprojekt.commons.output;

/**
 * Ouptput using {@link SpektrumParseableElement}
 * 
 * @param <T>
 */
public abstract class SpektrumParseableElementFileOutput<T extends SpektrumParseableElement>
        extends SpektrumFileOutput<T> {

    public SpektrumParseableElementFileOutput(Class<T> clazz) {
        super(clazz);
    }

    @Override
    protected String toString(T element) {
        return element.toParseableString();
    }

}