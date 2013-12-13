package de.spektrumprojekt.i.similarity.messagegroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

import de.spektrumprojekt.commons.computer.Computer;
import de.spektrumprojekt.commons.time.TimeProviderHolder;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageFilter;
import de.spektrumprojekt.datamodel.message.MessageGroup;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.similarity.set.SetSimilarity;
import de.spektrumprojekt.i.similarity.set.SetSimilarityResult;
import de.spektrumprojekt.persistence.Persistence;

public class TermBasedMessageGroupSimilarityComputer implements MessageGroupSimilarityRetriever,
        Computer {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(TermBasedMessageGroupSimilarityComputer.class);

    private final Persistence persistence;
    private final SetSimilarity setSimilarity;

    private final long intervallOfMessagesToConsiderInMs = 30 * 24 * DateUtils.MILLIS_PER_HOUR;

    private final long similarityTimeToLive = 7 * 24 * DateUtils.MILLIS_PER_HOUR;

    private final Map<Pair<Long, Long>, MessageGroupSimilarity> cachedSimilarites = new HashMap<Pair<Long, Long>, MessageGroupSimilarity>();

    private final boolean outputSimilarities;
    private final String outputSimilaritiesFilename;

    public TermBasedMessageGroupSimilarityComputer(
            Persistence persistence,
            SetSimilarity setSimilarity) {
        this(persistence, setSimilarity, false, null);
    }

    public TermBasedMessageGroupSimilarityComputer(
            Persistence persistence,
            SetSimilarity setSimilarity,
            boolean outputSimilarities,
            String outputSimilaritiesFilename) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (setSimilarity == null) {
            throw new IllegalArgumentException("setSimilarity cannot be null.");
        }
        if (outputSimilarities && outputSimilaritiesFilename == null) {
            throw new IllegalArgumentException(
                    "outputSimilaritiesFilename cannot be null if outputSimilarities=true.");
        }
        this.persistence = persistence;
        this.setSimilarity = setSimilarity;
        this.outputSimilarities = outputSimilarities;
        this.outputSimilaritiesFilename = outputSimilaritiesFilename;
    }

    private SetSimilarityResult computeSimilarity(Long messageGroupId1, Long messageGroupId2) {

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

        SetSimilarityResult result = setSimilarity.computeSimilarity(termValuesOfMG1,
                termValuesOfMG2);

        time.stop();
        long timeForAll = time.getTime();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Message Group Similarity ({} and {} = {}) took {} ms. {} ms and {} ms for getting terms",
                    new String[] {
                            "" + messageGroupId1, "" + messageGroupId2, "" + result.getSim(),
                            "" + timeForAll,
                            ""
                                    + timeToGetTerms1,
                            "" + timeToGetTerms2 });
        }

        return result;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " intervallOfMessagesToConsiderInMs: "
                + intervallOfMessagesToConsiderInMs;
    }

    @Override
    public float getMessageGroupSimilarity(Long messageGroupId1, Long messageGroupId2) {
        return internalGetMessageGroupSimilarity(messageGroupId1, messageGroupId2).getSim();
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

    @Override
    public List<MessageGroupSimilarity> getTopSimilarities(Long messageGroupId, int topN) {
        List<MessageGroupSimilarity> mgs = new ArrayList<MessageGroupSimilarity>();
        for (MessageGroupSimilarity mgSim : this.cachedSimilarites.values()) {
            if (mgSim.matchesMessageGroupId(messageGroupId, true)) {
                mgs.add(mgSim);
            }
        }
        Collections.sort(mgs, MessageGroupSimilarityComparator.INSTANCE);

        if (mgs.size() <= topN || topN == 0) {
            return mgs;
        }

        return new ArrayList<MessageGroupSimilarity>(mgs.subList(0, topN));
    }

    private MessageGroupSimilarity internalGetMessageGroupSimilarity(Long messageGroupId1,
            Long messageGroupId2) {
        if (messageGroupId1 > messageGroupId2) {
            Long mg = messageGroupId1;
            messageGroupId1 = messageGroupId2;
            messageGroupId2 = mg;
        }

        long currentTime = TimeProviderHolder.DEFAULT.getCurrentTime();
        Pair<Long, Long> mgPair = new ImmutablePair<Long, Long>(messageGroupId1, messageGroupId2);

        MessageGroupSimilarity simValue = cachedSimilarites.get(mgPair);
        if (simValue == null || simValue.getComputationDate() + similarityTimeToLive < currentTime) {

            SetSimilarityResult result;
            if (messageGroupId1.equals(messageGroupId2)) {
                result = new SetSimilarityResult();
                result.setSim(1);
            } else {
                result = computeSimilarity(messageGroupId1, messageGroupId2);
            }

            simValue = new MessageGroupSimilarity(messageGroupId1, messageGroupId2);
            simValue.setSim(result.getSim());
            simValue.setIntersectedTermCount(result.getIntersectionSize());
            simValue.setUnionTermsCount(result.getUnionSize());
            simValue.setTermsMG1Count(result.getSet1Size());
            simValue.setTermsMG2Count(result.getSet2Size());
            simValue.setComputationDate(TimeProviderHolder.DEFAULT.getCurrentTime());

            cachedSimilarites.put(mgPair, simValue);
        }

        return simValue;
    }

    @Override
    public void run() throws IOException {

        StopWatch runTime = new StopWatch();
        runTime.start();

        Collection<MessageGroup> messageGroups = persistence.getAllMessageGroups();

        this.cachedSimilarites.clear();
        for (MessageGroup one : messageGroups) {
            for (MessageGroup two : messageGroups) {
                MessageGroupSimilarity similarity = internalGetMessageGroupSimilarity(one.getId(),
                        two.getId());
                similarity.setMessageGroupGlobalId1(one.getGlobalId());
                similarity.setMessageGroupGlobalId2(two.getGlobalId());

            }
        }
        runTime.stop();

        LOGGER.info("Computing all message group similarities took {} ms.", runTime.getTime());

        if (this.outputSimilarities) {

            MessageGroupSimilarityOutput output = new MessageGroupSimilarityOutput();
            output.getElements().addAll(this.cachedSimilarites.values());
            output.write(outputSimilaritiesFilename);
        }
    }

}
