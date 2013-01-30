package de.spektrumprojekt.informationextraction.relations;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;

/**
 * <p>
 * A consolidation strategy which combines {@link Message}s referencing the same entities (e.g.
 * tickets, content, ...).
 * </p>
 * 
 * @author Philipp Katz
 */
public class InteractionConsolidationCommand implements Command<InformationExtractionContext> {

    // XXX store separately, integrate into Persistence
    private final Map<String, Message> entityMessageMap = new LruMap<>(100);

    // XXX make configurable
    private static final Pattern JIRA_PATTERN = Pattern
            .compile("https://jira.communardo.de/browse/[\\d\\w-]+");

    private int processed;
    private int consolidated;

    @Override
    public String getConfigurationDescription() {
        return getClass().getName() + ", # of processed messages = " + processed
                + " # of consolidated messages =  "
                + consolidated;
    }

    @Override
    public void process(InformationExtractionContext context) {
        System.out.println("Process "
                + getTitle(context.getMessage()));

        Message message = context.getMessage();
        MessagePart messagePart = context.getMessagePart();
        String messageContent = messagePart.getContent();

        Set<String> matches = getUniqueMatches(JIRA_PATTERN, messageContent);
        for (String match : matches) {
            Message reference = entityMessageMap.get(match);
            if (reference != null) {
                System.out.println("    > References " + getTitle(reference) + "(" + match + ")");
                consolidated++;
            }
            entityMessageMap.put(match, message);
        }

        processed++;

    }

    private Set<String> getUniqueMatches(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        Set<String> result = new HashSet<>();
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    private static String getTitle(Message message) {
        return message.getPropertiesAsMap().get(Property.PROPERTY_KEY_TITLE).getPropertyValue();
    }

}
