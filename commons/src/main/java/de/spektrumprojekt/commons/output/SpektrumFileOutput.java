package de.spektrumprojekt.commons.output;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Output elements to a file. Implementing classes must just define the type of object and parse a
 * new one.
 * 
 * @param <T>
 */
public abstract class SpektrumFileOutput<T extends SpektrumParseableElement> implements
        SpektrumOutput<T> {

    private final static Logger LOGGER = LoggerFactory.getLogger(SpektrumFileOutput.class);

    private final Class<T> clazz;
    private final List<T> elements = new ArrayList<T>();
    private final List<String> descriptions = new ArrayList<String>();
    private String commentPrefix = "#";

    public SpektrumFileOutput(Class<T> clazz) {
        this.clazz = clazz;
    }

    public SpektrumFileOutput(Class<T> clazz, String commentPrefix) {
        this(clazz);
        this.setCommentPrefix(commentPrefix);
    }

    protected abstract T createNewElement(String line);

    public String getCommentPrefix() {
        return commentPrefix;
    }

    /**
     * The descriptions. If read from file without the comment symbol.
     * 
     * @return
     */
    public List<String> getDescriptions() {
        return descriptions;
    }

    /**
     * Looks int the description for something like "bla: abc"
     * 
     * If search is "bla" then abc will be returned, or null if not found
     * 
     * Will return the first attr found
     * 
     * @param search
     * @return
     */
    public String getDescriptionValue(String search) {
        List<String> vals = getDescriptionValues(search);
        if (vals != null && vals.size() > 0) {
            return vals.iterator().next();
        }
        return null;
    }

    public List<String> getDescriptionValues(String search) {
        List<String> vals = new ArrayList<String>();
        for (String desc : this.getDescriptions()) {
            int fndIndex = desc.indexOf(search);
            if (fndIndex >= 0) {
                int dpIndex = desc.indexOf(":", fndIndex);
                if (dpIndex != -1) {
                    String str = desc.substring(dpIndex + 1).trim();
                    vals.add(str);
                }
            }
        }
        return vals;
    }

    public List<T> getElements() {
        return elements;
    }

    public Class<T> getHandlingClass() {
        return this.clazz;
    }

    protected abstract String getHeader();

    public void read(String filename) throws IOException {
        FileInputStream fstream = null;
        // we use buffered reader for performance
        BufferedReader br = null;

        try {
            // Open the file
            fstream = new FileInputStream(filename);

            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in));

            // first is header
            String strLine = br.readLine();

            // Read File Line By Line
            while ((strLine = br.readLine()) != null) {

                if (strLine.trim().startsWith(this.commentPrefix)) {
                    strLine = strLine.trim().substring(this.commentPrefix.length());
                    descriptions.add(strLine.trim());

                } else {
                    T element;
                    try {
                        element = createNewElement(strLine);
                    } catch (RuntimeException re) {
                        LOGGER.error("Error reading line from " + filename + ". Line is: "
                                + strLine, re);
                        throw re;
                    }
                    if (element != null) {
                        this.elements.add(element);
                    }

                }
            }

            // Close the input stream
            in.close();
            LOGGER.debug("Read {} elements of class {} .", this.elements.size(),
                    this.getHandlingClass());
        } finally {
            if (br != null) {
                br.close();
            }
            if (fstream != null) {
                fstream.close();
            }
        }
    }

    public void setCommentPrefix(String commentPrefix) {
        if (commentPrefix == null) {
            throw new IllegalArgumentException("commentPrefix cannot be null!");
        }
        this.commentPrefix = commentPrefix;
    }

    protected abstract String toString(T element);

    public void write(String filename) throws IOException {
        List<String> lines = new ArrayList<String>();

        lines.add(this.commentPrefix + " " + getHeader());
        for (String description : this.descriptions) {
            if (!description.startsWith(this.commentPrefix)) {
                description = this.commentPrefix + " " + description.trim();
            }
            lines.add(description);
        }
        for (T element : elements) {
            lines.add(toString(element));
        }

        FileUtils.writeLines(new File(filename), lines);
    }

}
