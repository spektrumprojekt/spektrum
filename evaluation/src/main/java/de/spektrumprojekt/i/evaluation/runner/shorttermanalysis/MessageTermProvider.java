package de.spektrumprojekt.i.evaluation.runner.shorttermanalysis;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageFilter;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.persistence.Persistence;

/**
 * Provides terms of all messages with the publication date and score of the term
 * 
 * 
 */
public class MessageTermProvider implements TermProvider {

    private final Persistence persistence;
    private final Date startDate;
    private final Date endDate;

    public MessageTermProvider(
            Persistence persistence,
            Date startDate,
            Date endDate) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }

        this.persistence = persistence;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Map<String, Float> extractTermScores(Message message) {
        Map<String, Float> termScores = new HashMap<String, Float>();
        for (MessagePart messagePart : message.getMessageParts()) {
            for (ScoredTerm scoredTerm : messagePart.getScoredTerms()) {
                String termValue = scoredTerm.getTerm().getValue();
                if (!termScores.containsKey(termValue)) {
                    termScores.put(termValue, scoredTerm.getWeight());
                } else {
                    termScores.put(termValue,
                            Math.max(termScores.get(termValue), scoredTerm.getWeight()));
                }
            }
        }
        return termScores;
    }

    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " startDate: " + startDate;
    }

    public String getFileAppendix() {
        return "_Message_";
    }

    public Collection<Entry> getTerms() {
        Set<Entry> entries = new HashSet<Entry>();

        MessageFilter messageFilter = new MessageFilter();
        if (startDate != null) {
            messageFilter.setMinPublicationDate(startDate);
        }

        Collection<Message> messages = persistence.getMessages(messageFilter);

        messageLoop: for (Message message : messages) {
            Date messageDate = message.getPublicationDate();
            if (endDate != null && messageDate.after(endDate)) {
                continue messageLoop;
            }
            Map<String, Float> termScores = extractTermScores(message);

            for (String termValue : termScores.keySet()) {
                entries.add(new Entry(termValue, messageDate, termScores.get(termValue)));
            }
        }
        return entries;
    }

}
