package de.spektrumprojekt.i.term.frequency;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.message.TermFrequency;
import de.spektrumprojekt.persistence.Persistence;

public class TermFrequencyComputer implements ConfigurationDescriptable {

    private final static Logger LOGGER = LoggerFactory.getLogger(TermFrequencyComputer.class);

    private final Persistence persistence;

    private final boolean beMessageGroupSpecific;

    private TermFrequency internalTermFrequency;

    private FileWriter termUniqueness;

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
        float termsPerMessage = (float) getTermFrequency().getAllTermCount()
                / getTermFrequency().getMessageCount();

        StringBuilder sb = new StringBuilder();
        sb.append("##############################\n");
        sb.append("# Global Term Statistics \n");
        sb.append("# \n");
        sb.append("# messageCount " + getTermFrequency().getMessageCount() + "\n");
        sb.append("# allTermCount " + getTermFrequency().getAllTermCount() + "\n");
        sb.append("# uniqueTermCount " + getTermFrequency().getUniqueTermCount() + "\n");
        sb.append("# termsPerMessage " + termsPerMessage + "\n");

        sb.append("##############################\n");
        sb.append("# Message Group Message Counts \n");
        sb.append("# \n");
        sb.append("# messageGroup.id messageGroup.globalId messageGroupMessageCount\n");
        for (Entry<String, Integer> entry : this.getTermFrequency().getMessageGroupMessageCounts()
                .entrySet()) {
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
        return getTermFrequency().getAllTermCount();
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    public long getMessageCount() {
        return getTermFrequency().getMessageCount();
    }

    public long getMessageCount(String messageGroupId) {

        if (beMessageGroupSpecific) {
            Integer count = getTermFrequency().getMessageGroupMessageCounts().get(messageGroupId);
            if (count == null) {
                return 0;
            }
            return count;
        }
        return getTermFrequency().getMessageCount();
    }

    private TermFrequency getTermFrequency() {
        if (this.internalTermFrequency == null) {
            synchronized (this) {
                if (this.internalTermFrequency == null) {
                    internalTermFrequency = persistence.getTermFrequency();
                }
            }
        }
        return this.internalTermFrequency;
    }

    public long getUniqueTermCount() {
        return getTermFrequency().getUniqueTermCount();
    }

    public void init(String filename) {
        if (filename != null) {
            try {
                termUniqueness = new FileWriter(new File(filename));
                logTermChangeHeader();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public synchronized Collection<Term> integrate(Message message) {
        getTermFrequency();
        Collection<Term> terms = internalIntegrate(message, true);
        this.updateTermFrequency();
        return terms;
    }

    private void integrate(MessageGroup messageGroup) {
        Integer count = this.getTermFrequency().getMessageGroupMessageCounts().get(
                messageGroup.getGlobalId());
        if (count == null) {
            count = 0;
        }
        count++;
        this.getTermFrequency().getMessageGroupMessageCounts()
                .put(messageGroup.getGlobalId(), count);
    }

    private Collection<Term> internalIntegrate(Message message, boolean log) {
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
                    getTermFrequency().setUniqueTermCount(
                            getTermFrequency().getUniqueTermCount() + 1);
                }
                getTermFrequency().setAllTermCount(getTermFrequency().getAllTermCount() + 1);
                term.setCount(term.getCount() + 1);
                termsChanged.add(term);
            }
        }
        getTermFrequency().setMessageCount(getTermFrequency().getMessageCount() + 1);
        if (log) {
            logTermChange(message.getPublicationDate());
        }
        return termsChanged;
    }

    private void logTermChange(Date date) {
        if (termUniqueness != null) {
            try {
                termUniqueness.write(date.getTime() + " " + getTermFrequency().getAllTermCount()
                        + " "
                        + getTermFrequency().getUniqueTermCount() + " "
                        + getTermFrequency().getMessageCount()
                        + "\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void logTermChangeHeader() throws IOException {
        termUniqueness.write("Time AllTerms UniqueTerms MessageCount \n");
    }

    public synchronized void run() {
        LOGGER.info("Starting TermFrequencyComputed ...");
        Date fromDate = new Date(0);

        long allTermCount = 0;
        long uniqueTermCount = 0;

        this.persistence.resetTermCount();
        this.getTermFrequency().getMessageGroupMessageCounts().clear();

        Collection<Message> messages = this.persistence.getMessagesSince(fromDate);
        Collection<Term> termsChanged = new HashSet<Term>();

        LOGGER.info("TermFrequencyComputed loaded messages ...");

        int i = 0;
        for (Message message : messages) {
            termsChanged.addAll(internalIntegrate(message, false));

            i++;
            if (i % (messages.size() / 10) == 0) {
                LOGGER.debug(" {} % done", i * 100 / messages.size());
            }
        }
        LOGGER.info("TermFrequencyComputed setting terms count done ...");

        this.persistence.updateTerms(termsChanged);

        this.getTermFrequency().setAllTermCount(allTermCount);
        this.getTermFrequency().setUniqueTermCount(uniqueTermCount);
        this.getTermFrequency().setMessageCount(messages.size());

        updateTermFrequency();

        LOGGER.info("Finished TermFrequencyComputed.");

    }

    public void stop() {
        if (termUniqueness != null) {
            IOUtils.closeQuietly(termUniqueness);
        }
    }

    private synchronized void updateTermFrequency() {
        if (internalTermFrequency != null) {
            this.persistence.updateTermFrequency(this.internalTermFrequency);
        }
    }
}
