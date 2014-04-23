package de.spektrumprojekt.i.evaluation.helper;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.output.SpektrumFileOutput;
import de.spektrumprojekt.datamodel.message.Message;

public class SpektrumRatingOutput extends SpektrumFileOutput<SpektrumRating> {

    private final Map<String, Message> messageMap;

    private final static Logger LOGGER = LoggerFactory.getLogger(SpektrumRatingOutput.class);

    public SpektrumRatingOutput() {
        this(null);
    }

    public SpektrumRatingOutput(Map<String, Message> messageMap) {
        super(SpektrumRating.class);
        this.messageMap = messageMap;
    }

    @Override
    protected SpektrumRating createNewElement(String line) {
        try {
            return new SpektrumRating(line, messageMap);
        } catch (IllegalArgumentException iae) {
            LOGGER.warn("Ignoring rating: " + iae.getMessage());
            return null;
        }
    }

    @Override
    protected String getHeader() {
        return SpektrumRating.getColumnHeader();
    }

    @Override
    protected String toString(SpektrumRating element) {
        return element.toParseableString();
    }

}
