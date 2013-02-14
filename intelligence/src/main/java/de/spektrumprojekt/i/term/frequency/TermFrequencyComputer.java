package de.spektrumprojekt.i.term.frequency;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
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

    private long allTermCount = 0;

    private long uniqueTermCount = 0;

    private long messageCount = 0;

    public TermFrequencyComputer(Persistence persistence, boolean beMessageGroupSpecific) {
        this.persistence = persistence;
        this.beMessageGroupSpecific = beMessageGroupSpecific;

    }

    public void dumpTermCounts(String filename) throws IOException {
        List<Term> termsSorted = new ArrayList<Term>(this.persistence.getAllTerms());
        Collections.sort(termsSorted, new Comparator<Term>() {

            @Override
            public int compare(Term o1, Term o2) {
                if (o1 == o2) {
                    return 0;
                }
                if (o1.getCount() != o2.getCount()) {
                    return o1.getCount() - o2.getCount();
                }

                return o1.getValue().compareTo(o2.getValue());
            }
        });
        float termsPerMessage = (float) allTermCount / messageCount;

        StringBuilder sb = new StringBuilder();
        sb.append("##############################\n");
        sb.append("# Global Term Statistics \n");
        sb.append("# \n");
        sb.append("# messageCount " + messageCount + "\n");
        sb.append("# allTermCount " + allTermCount + "\n");
        sb.append("# uniqueTermCount " + uniqueTermCount + "\n");
        sb.append("# termsPerMessage " + termsPerMessage + "\n");

        sb.append("##############################\n");
        sb.append("# Message Group Message Counts \n");
        sb.append("# \n");
        sb.append("# messageGroup.id messageGroup.globalId messageGroupMessageCount\n");
        for (Entry<String, Integer> entry : this.messageGroupMessageCounts.entrySet()) {
            MessageGroup messageGroup = this.persistence.getMessageGroupByGlobalId(entry.getKey());
            sb.append("# " + messageGroup.getId() + " " + messageGroup.getGlobalId() + " "
                    + entry.getValue() + "\n");
        }

        sb.append("##############################");
        sb.append("# Term Counts");
        sb.append("# ");
        sb.append("# term.value term.count");
        for (Term t : termsSorted) {
            sb.append(t.getValue() + " " + t.getCount() + "\n");
        }

        FileUtils.writeStringToFile(new File(filename), sb.toString());

    }

    public long getAllTermCount() {
        return allTermCount;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    public long getMessageCount() {
        return messageCount;
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

    public long getUniqueTermCount() {
        return uniqueTermCount;
    }

    public synchronized Collection<Term> integrate(Message message) {
        if (beMessageGroupSpecific) {
            integrate(message.getMessageGroup());
        }

        Collection<Term> termsChanged = new HashSet<Term>();
        for (MessagePart part : message.getMessageParts()) {
            for (ScoredTerm st : part.getScoredTerms()) {
                Term term = st.getTerm();
                if (term.getId() == null) {
                    throw new IllegalStateException("term.id cannot be null! term=" + term);
                }
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
