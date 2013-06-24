package de.spektrumprojekt.informationextraction.relations;

import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageRelation;
import de.spektrumprojekt.datamodel.message.MessageRelation.MessageRelationType;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;
import de.spektrumprojekt.persistence.Persistence;

/**
 * <p>
 * A consolidation strategy which combines {@link Message}s containing a specific pattern (e.g.
 * tickets, content, ...).
 * </p>
 * 
 * @author Philipp Katz
 */
public class PatternConsolidationCommand implements Command<InformationExtractionContext> {

    /** The logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(PatternConsolidationCommand.class);

    private final PatternConsolidationConfiguration patternProvider;

    /**
     * <p>
     * Initialize a new {@link PatternConsolidationCommand} with the specified collection of
     * regexes. They are used for creating relations from processed to existing messages.
     * </p>
     * 
     * @param patternProvider
     *            The configuration options.
     */
    public PatternConsolidationCommand(PatternConsolidationConfiguration patternProvider) {
        this.patternProvider = patternProvider;
        LOGGER.debug("Initalized {} with {} patterns", getClass().getName(), patternProvider
                .getPatterns().size());
    }

    @Override
    public String getConfigurationDescription() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("PatternConsolidationCommand");
        stringBuilder.append(" patterns=").append(patternProvider.getPatterns());
        return stringBuilder.toString();
    }

    private Set<String> getUniqueMatches(Collection<NamePattern> patterns, String text) {
        Set<String> result = new HashSet<String>();
        for (NamePattern pattern : patterns) {
            Matcher matcher = pattern.getPattern().matcher(text);
            while (matcher.find()) {
                String matchedGroup = matcher.group(1);
                if (matchedGroup == null) {
                    LOGGER.warn(
                            "Could not extract information for {}, make sure to configure capturing group correctly!",
                            pattern.getRegex());
                }
                String identifier = pattern.getName() + "#" + matchedGroup;
                result.add(identifier);
            }
        }
        return result;
    }

    @Override
    public void process(InformationExtractionContext context) {
        try {
            LOGGER.debug("Process {}", MessageHelper.getTitle(context.getMessage()));

            Message message = context.getMessage();
            MessagePart messagePart = context.getMessagePart();
            String messageContent = messagePart.getContent();
            Persistence persistence = context.getPersistence();
            String title = MessageHelper.getTitle(message);
            String link = MessageHelper.getLink(message);

            String fullContent = StringUtils.join(Arrays.asList(title, link, messageContent), "\n");

            Set<String> matches = getUniqueMatches(patternProvider.getPatterns(), fullContent);
            LOGGER.trace("Extracted matches: {}", matches);

            for (String match : matches) {
                persistence.storeMessagePattern(match, message);

                GregorianCalendar calendar = new GregorianCalendar();
                calendar.setTime(message.getPublicationDate());
                calendar.add(GregorianCalendar.MILLISECOND, -patternProvider.getPeriodOfTime()
                        .intValue());

                Collection<Message> relatedMessages = persistence.getMessagesForPattern(match,
                        calendar.getTime());
                context.add(match);
                if (relatedMessages.isEmpty()) {
                    continue;
                }
                Set<String> relatedIds = new HashSet<String>();
                for (Message relatedMessage : relatedMessages) {
                    relatedIds.add(relatedMessage.getGlobalId());
                }
                String[] relatedIdsArray = relatedIds.toArray(new String[relatedIds.size()]);
                MessageRelation relation = new MessageRelation(MessageRelationType.RELATION,
                        message.getGlobalId(), relatedIdsArray);
                LOGGER.debug("Message relation for {} = {}", message.getGlobalId(), relation);
                persistence.storeMessageRelation(message, relation);
                context.add(relation);
            }
        } catch (Exception e) {
            LOGGER.error("Error handling pattern for message=" + context.getMessage(), e);
        }
    }
}
