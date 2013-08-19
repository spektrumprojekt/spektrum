package de.spektrumprojekt.aggregator.subscription;

import de.spektrumprojekt.datamodel.source.Source;

/**
 * Thrown in case an adapter was not found for a source
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class AdapterNotFoundException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final Source source;

    public AdapterNotFoundException(String message, Source source) {
        super(message);
        this.source = source;
    }

    public Source getSource() {
        return source;
    }

}
