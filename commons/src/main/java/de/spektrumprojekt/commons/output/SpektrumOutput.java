package de.spektrumprojekt.commons.output;

import java.io.IOException;
import java.util.List;

/**
 * Interface to read and write elements
 * 
 * @param <T>
 *            Tpye of object to output
 */
public interface SpektrumOutput<T extends Object> {

    public List<T> getElements();

    public Class<T> getHandlingClass();

    public void read(String filename) throws IOException;

    public void write(String filename) throws IOException;
}
