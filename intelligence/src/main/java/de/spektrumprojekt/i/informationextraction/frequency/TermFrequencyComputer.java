package de.spektrumprojekt.i.informationextraction.frequency;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.persistence.Persistence;

public class TermFrequencyComputer implements ConfigurationDescriptable {

    private final static Logger LOGGER = LoggerFactory.getLogger(TermFrequencyComputer.class);

    private final Persistence persistence;

    public static long allTermCount = 0;

    public static long uniqueTermCount = 0;

    public static long messageCount = 0;

    public TermFrequencyComputer(Persistence persistence) {
        this.persistence = persistence;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    public Collection<Term> integrate(Message message) {
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
        return termsChanged;
    }

    public void run() {
        LOGGER.info("Starting TermFrequencyComputed ...");
        Date fromDate = new Date(0);

        this.persistence.resetTermCount();

        Collection<Message> messages = this.persistence.getMessagesSince(fromDate);
        Collection<Term> termsChanged = new HashSet<Term>();

        LOGGER.info("TermFrequencyComputed loaded messages ...");

        long allTermCount = 0;
        long uniqueTermCount = 0;
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
