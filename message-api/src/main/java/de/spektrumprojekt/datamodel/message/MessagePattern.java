package de.spektrumprojekt.datamodel.message;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.Validate;

import de.spektrumprojekt.datamodel.identifiable.Identifiable;

/**
 * <p>
 * A pattern which is extracted from a specific {@link Message}.
 * </p>
 * 
 * @author Philipp Katz
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "globalId"))
public class MessagePattern extends Identifiable {

    private static final long serialVersionUID = 1L;

    private String pattern;

    private Message message;

    /**
     * <p>
     * Construct a new MessagePattern with the given properties.
     * </p>
     * 
     * @param message
     *            The message.
     * @param pattern
     *            The pattern extracted from the message.
     */
    public MessagePattern(Message message, String pattern) {
        Validate.notNull(message, "message must not be null");
        Validate.notNull(pattern, "pattern must not be null");

        this.message = message;
        this.pattern = pattern;
    }

    protected MessagePattern() {
        // ORM constructor.
    }

    public String getPattern() {
        return pattern;
    }

    public Message getMessage() {
        return message;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MessagePattern [pattern=");
        builder.append(pattern);
        builder.append(", message=");
        builder.append(message);
        builder.append("]");
        return builder.toString();
    }

}
