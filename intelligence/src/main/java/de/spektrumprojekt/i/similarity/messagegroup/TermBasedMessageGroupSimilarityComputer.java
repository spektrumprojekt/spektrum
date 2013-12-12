package de.spektrumprojekt.i.similarity.messagegroup;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.time.TimeProviderHolder;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageFilter;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.similarity.set.SetSimilarity;
import de.spektrumprojekt.persistence.Persistence;

public class TermBasedMessageGroupSimilarityComputer implements MessageGroupSimilarityRetriever {

    public class SimValue {
        public double sim;
        public long computationDate;
    }

    private final static Logger LOGGER = LoggerFactory
            .getLogger(TermBasedMessageGroupSimilarityComputer.class);

    private final Persistence persistence;
    private final SetSimilarity setSimilarity;

    private final long intervallOfMessagesToConsiderInMs = 30 * 24 * DateUtils.MILLIS_PER_HOUR;

    private final long similarityTimeToLive = 7 * 24 * DateUtils.MILLIS_PER_HOUR;

    private final Map<Pair<Long, Long>, SimValue> cachedSimilarites = new HashMap<Pair<Long, Long>, TermBasedMessageGroupSimilarityComputer.SimValue>();

    public TermBasedMessageGroupSimilarityComputer(
            Persistence persistence,
            SetSimilarity setSimilarity) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (setSimilarity == null) {
            throw new IllegalArgumentException("setSimilarity cannot be null.");
        }
        this.persistence = persistence;
        this.setSimilarity = setSimilarity;
    }

    private double computeSimilarity(Long messageGroupId1, Long messageGroupId2) {

        StopWatch time = new StopWatch();
        time.start();
        long currentTime = TimeProviderHolder.DEFAULT.getCurrentTime();
        currentTime -= intervallOfMessagesToConsiderInMs;
        Date minPublicationDate = new Date(currentTime);

        Set<Term> termsOfMG1 = getTermsForMessageGroup(messageGroupId1, minPublicationDate);
        Set<String> termValuesOfMG1 = getMGFreeTermValues(termsOfMG1);
        long timeToGetTerms1 = time.getTime();

        Set<Term> termsOfMG2 = getTermsForMessageGroup(messageGroupId2, minPublicationDate);
        Set<String> termValuesOfMG2 = getMGFreeTermValues(termsOfMG2);
        long timeToGetTerms2 = time.getTime();

        double sim = setSimilarity.computeSimilarity(termValuesOfMG1, termValuesOfMG2);

        time.stop();
        long timeForAll = time.getTime();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Message Group Similarity ({} and {} = {}) took {} ms. {} ms and {} ms for getting terms",
                    new String[] {
                            "" + messageGroupId1, "" + messageGroupId2, "" + sim, "" + timeForAll,
                            ""
                                    + timeToGetTerms1,
                            "" + timeToGetTerms2 });
        }

        return sim;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " intervallOfMessagesToConsiderInMs: "
                + intervallOfMessagesToConsiderInMs;
    }

    @Override
    public double getMessageGroupSimilarity(Long messageGroupId1, Long messageGroupId2) {

        if (messageGroupId1.equals(messageGroupId2)) {
            return 1;
        }

        long currentTime = TimeProviderHolder.DEFAULT.getCurrentTime();
        Pair<Long, Long> mgPair = new ImmutablePair<Long, Long>(messageGroupId1, messageGroupId2);

        SimValue simValue = cachedSimilarites.get(mgPair);
        if (simValue == null || simValue.computationDate + similarityTimeToLive < currentTime) {
            simValue = new SimValue();
            simValue.sim = computeSimilarity(messageGroupId1, messageGroupId2);
            simValue.computationDate = TimeProviderHolder.DEFAULT.getCurrentTime();
            cachedSimilarites.put(mgPair, simValue);
        }

        return simValue.sim;
    }

    private Set<String> getMGFreeTermValues(Set<Term> termsOfMG1) {
        Set<String> values = new HashSet<String>();
        for (Term t : termsOfMG1) {
            values.add(t.extractMessageGroupFreeTermValue());
        }
        return values;
    }

    private Set<Term> getTermsForMessageGroup(Long messageGroupId, Date minPublicationDate) {
        MessageFilter mf = new MessageFilter();
        mf.setMessageGroupId(messageGroupId);
        mf.setMinPublicationDate(minPublicationDate);

        List<Message> messagesForTopic1 = persistence.getMessages(mf);
        Set<Term> terms = new HashSet<Term>();
        for (Message message : messagesForTopic1) {
            terms.addAll(MessageHelper.getAllTerms(message));
        }

        return terms;
    }

}
