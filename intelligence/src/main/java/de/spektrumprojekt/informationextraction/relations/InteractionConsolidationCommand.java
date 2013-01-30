package de.spektrumprojekt.informationextraction.relations;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.datamodel.message.MessageRelation.MessageRelationType;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;
import de.spektrumprojekt.persistence.Persistence;

/**
 * <p>
 * A consolidation strategy which combines {@link Message}s referencing the same entities (e.g.
 * tickets, content, ...).
 * </p>
 * 
 * @author Philipp Katz
 */
public class InteractionConsolidationCommand implements Command<InformationExtractionContext> {

    /** The logger for this class. */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(InteractionConsolidationCommand.class);

    // XXX make configurable
    private static final Pattern JIRA_PATTERN = Pattern
            .compile("https://jira.communardo.de/browse/[\\d\\w-]+");

    @Override
    public String getConfigurationDescription() {
        return getClass().getName();
    }

    @Override
    public void process(InformationExtractionContext context) {
        LOGGER.debug("Process {}", getTitle(context.getMessage()));

        Message message = context.getMessage();
        MessagePart messagePart = context.getMessagePart();
        String messageContent = messagePart.getContent();
        Persistence persistence = context.getPersistence();

        Set<String> matches = getUniqueMatches(JIRA_PATTERN, messageContent);
        for (String match : matches) {
            Collection<Message> relatedMessages = persistence.getMessagesForPattern(match);
            persistence.storeMessagePattern(match, message);
            if (relatedMessages.isEmpty()) {
                continue;
            }
            Set<String> relatedIds = new HashSet<>();
            for (Message relatedMessage : relatedMessages) {
                relatedIds.add(relatedMessage.getGlobalId());
            }
            String[] relatedIdsArray = relatedIds.toArray(new String[relatedIds.size()]);
            MessageRelation relation = new MessageRelation(MessageRelationType.RELATION,
                    relatedIdsArray);
            LOGGER.debug("Message relation for {} = {}", message.getGlobalId(), relation);
            persistence.storeMessageRelation(message, relation);
        }
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
