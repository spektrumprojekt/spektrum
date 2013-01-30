package de.spektrumprojekt.i.informationextraction.frequency;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.persistence.Persistence;

public class TermFrequencyComputer implements ConfigurationDescriptable {

    private final static Logger LOGGER = LoggerFactory.getLogger(TermFrequencyComputer.class);

    private final Persistence persistence;

    private final boolean beMessageGroupSpecific;

    private final Map<String, Integer> messageGroupMessageCounts = new HashMap<String, Integer>();

    public long allTermCount = 0;

    public long uniqueTermCount = 0;

    public long messageCount = 0;

    public TermFrequencyComputer(Persistence persistence, boolean beMessageGroupSpecific) {
        this.persistence = persistence;
        this.beMessageGroupSpecific = beMessageGroupSpecific;

    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    public long getMessageCount(String messageGroupId) {

        if (beMessageGroupSpecific) {
            Integer count = this.messageGroupMessageCounts.get(messageGroupId);
            if (count == null) {
                return 0;
            }
            return count;
        }
        return this.messageCount;
    }

    public synchronized Collection<Term> integrate(Message message) {
        if (beMessageGroupSpecific) {
            integrate(message.getMessageGroup());
        }

        Collection<Term> termsChanged = new HashSet<Term>();
        for (MessagePart part : message.getMessageParts()) {
            for (ScoredTerm st : part.getScoredTerms()) {
                Term term = st.getTerm();

                if (term.getCount() == 0) {
                    uniqueTermCount++;
                }
                allTermCount++;
                term.setCount(term.getCount() + 1);
                termsChanged.add(term);
            }
        }
        messageCount++;
        return termsChanged;
    }

    private void integrate(MessageGroup messageGroup) {
        Integer count = this.messageGroupMessageCounts.get(messageGroup.getGlobalId());
        if (count == null) {
            count = 0;
        }
        count++;
        this.messageGroupMessageCounts.put(messageGroup.getGlobalId(), count);
    }

    public synchronized void run() {
        LOGGER.info("Starting TermFrequencyComputed ...");
        Date fromDate = new Date(0);

        long allTermCount = 0;
        long uniqueTermCount = 0;

        this.persistence.resetTermCount();
        this.messageGroupMessageCounts.clear();

        Collection<Message> messages = this.persistence.getMessagesSince(fromDate);
        Collection<Term> termsChanged = new HashSet<Term>();

        LOGGER.info("TermFrequencyComputed loaded messages ...");

        int i = 0;
        for (Message message : messages) {
            termsChanged.addAll(integrate(message));

            i++;
            if (i % (messages.size() / 10) == 0) {
                LOGGER.debug(" {} % done", i * 100 / messages.size());
            }
        }
        LOGGER.info("TermFrequencyComputed setting terms count done ...");

        this.persistence.updateTerms(termsChanged);

        this.allTermCount = allTermCount;
        this.uniqueTermCount = uniqueTermCount;
        this.messageCount = messages.size();

        LOGGER.info("Finished TermFrequencyComputed.");

        // TODO where to store the overall term counts ?

    }
}
