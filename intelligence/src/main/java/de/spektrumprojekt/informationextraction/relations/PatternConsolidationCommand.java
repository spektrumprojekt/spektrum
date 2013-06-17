package de.spektrumprojekt.informationextraction.relations;

import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // /** The patterns which are used for establishing relations. */
    // private final Collection<Pattern> patterns;

    private final PatternConsolidationConfiguration patternProvider;

    /**
     * <p>
     * Initialize a new {@link PatternConsolidationCommand} with the specified collection of
     * regexes. They are used for creating relations from processed to existing messages.
     * </p>
     * 
     * @param regExes
     *            The collections of regexes.
     */
    public PatternConsolidationCommand(PatternConsolidationConfiguration patternProvider) {
        this.patternProvider = patternProvider;
        // patterns = new HashSet<Pattern>();
        // for (String regEx : regExes) {
        // patterns.add(Pattern.compile(regEx));
        // }
    }

    @Override
    public String getConfigurationDescription() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("PatternConsolidationCommand");
        stringBuilder.append(" patterns=").append(patternProvider.getPatterns());
        return stringBuilder.toString();
    }

    private Set<String> getUniqueMatches(Collection<Pattern> patterns, String text) {
        Set<String> result = new HashSet<String>();
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                result.add(matcher.group());
            }
        }
        return result;
    }

    @Override
    public void process(InformationExtractionContext context) {
        LOGGER.debug("Process {}", MessageHelper.getTitle(context.getMessage()));

        Message message = context.getMessage();
        MessagePart messagePart = context.getMessagePart();
        String messageContent = messagePart.getContent();
        Persistence persistence = context.getPersistence();

        Set<String> matches = getUniqueMatches(patternProvider.getPatterns(), messageContent);
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
    }
}
