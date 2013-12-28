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
import de.spektrumprojekt.i.similarity.set.SetSimilarityResult;
import de.spektrumprojekt.persistence.Persistence;

public class TermBasedMessageGroupSimilarityComputer implements MessageGroupSimilarityRetriever,
        Computer {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(TermBasedMessageGroupSimilarityComputer.class);

    private final Persistence persistence;
    private final MessageGroupSimilarityConfiguration messageGroupSimilarityConfiguration;

    private final Map<Pair<Long, Long>, MessageGroupSimilarity> cachedSimilarites = new HashMap<Pair<Long, Long>, MessageGroupSimilarity>();

    public TermBasedMessageGroupSimilarityComputer(
            Persistence persistence,
            MessageGroupSimilarityConfiguration messageGroupSimilarityConfiguration) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null.");
        }
        if (messageGroupSimilarityConfiguration == null) {
            throw new IllegalArgumentException(
                    "messageGroupSimilarityConfiguration cannot be null.");
        }
        if (messageGroupSimilarityConfiguration.getSetSimilarity() == null) {
            throw new IllegalArgumentException(
                    "messageGroupSimilarityConfiguration.getSetSimilarity() cannot be null if outputSimilarities=true.");
        }
        if (messageGroupSimilarityConfiguration.isReadMessageGroupSimilaritiesFromPrecomputedFile()
                && messageGroupSimilarityConfiguration
                        .isWriteMessageGroupSimilaritiesToPrecomputedFile()) {
            throw new IllegalArgumentException(
                    "Cannot read and write precomputed sims at the same time.");
        }
        this.persistence = persistence;
        this.messageGroupSimilarityConfiguration = messageGroupSimilarityConfiguration;
    }

    private SetSimilarityResult computeSimilarity(Long messageGroupId1, Long messageGroupId2) {

        StopWatch time = new StopWatch();
        time.start();
        long currentTime = TimeProviderHolder.DEFAULT.getCurrentTime();
        currentTime -= this.messageGroupSimilarityConfiguration
                .getIntervallOfMessagesToConsiderInMs();
        Date minPublicationDate = new Date(currentTime);

        Set<Term> termsOfMG1 = getTermsForMessageGroup(messageGroupId1, minPublicationDate);
        Set<String> termValuesOfMG1 = getMGFreeTermValues(termsOfMG1);
        long timeToGetTerms1 = time.getTime();

        Set<Term> termsOfMG2 = getTermsForMessageGroup(messageGroupId2, minPublicationDate);
        Set<String> termValuesOfMG2 = getMGFreeTermValues(termsOfMG2);
        long timeToGetTerms2 = time.getTime();

        SetSimilarityResult result = this.messageGroupSimilarityConfiguration.getSetSimilarity()
                .computeSimilarity(termValuesOfMG1, termValuesOfMG2);

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
        return this.getClass().getSimpleName() + " messageGroupSimilarityConfiguration: "
                + this.messageGroupSimilarityConfiguration.getConfigurationDescription();
    }

    @Override
    public float getMessageGroupSimilarity(Long messageGroupId1, Long messageGroupId2) {
        MessageGroupSimilarity messageGroupSimilarity = internalGetMessageGroupSimilarity(
                messageGroupId1, messageGroupId2);

        float sim;

        if (messageGroupSimilarity == null) {
            sim = 0f;
            LOGGER.warn(
                    "No messagegroup similarity for {} {} (iterative={})", new Object[] {
                            messageGroupId1,
                            messageGroupId2,
                            new Boolean(this.messageGroupSimilarityConfiguration
                                    .isAllowIterativeRecomputation()) });
        } else {
            sim = messageGroupSimilarity.getSim();
        }
        return sim;
    }

    private String getMessageGroupSimilarityDumpFilename() {
        String fname = this.messageGroupSimilarityConfiguration
                .getPrecomputedMessageGroupSimilaritesFilename();

        if (this.messageGroupSimilarityConfiguration.isPrecomputedIsWithDate()) {

            fname = fname.replace("TIME", "" + TimeProviderHolder.DEFAULT.getCurrentTime());
        }

        return fname;

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

    private MessageGroupSimilarity internalComputeAndCacheSimilarity(Long messageGroupId1,
            Long messageGroupId2) {

        MessageGroupSimilarity simValue;
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

        Pair<Long, Long> mgPair = internalGetMessageGroupIdPair(messageGroupId1, messageGroupId2);
        cachedSimilarites.put(mgPair, simValue);
        return simValue;
    }

    private Pair<Long, Long> internalGetMessageGroupIdPair(Long messageGroupId1,
            Long messageGroupId2) {
        if (messageGroupId1 > messageGroupId2) {
            Long mg = messageGroupId1;
            messageGroupId1 = messageGroupId2;
            messageGroupId2 = mg;
        }
        Pair<Long, Long> mgPair = new ImmutablePair<Long, Long>(messageGroupId1, messageGroupId2);
        return mgPair;
    }

    private MessageGroupSimilarity internalGetMessageGroupSimilarity(Long messageGroupId1,
            Long messageGroupId2) {
        Pair<Long, Long> mgPair = internalGetMessageGroupIdPair(messageGroupId1, messageGroupId2);

        long similarityTimeToLive = this.messageGroupSimilarityConfiguration
                .getSimilarityTimeToLive();
        long currentTime = TimeProviderHolder.DEFAULT.getCurrentTime();

        MessageGroupSimilarity simValue = cachedSimilarites.get(mgPair);
        if (this.messageGroupSimilarityConfiguration.isAllowIterativeRecomputation()) {
            if (simValue == null
                    || simValue.getComputationDate() + similarityTimeToLive < currentTime) {

                simValue = internalComputeAndCacheSimilarity(messageGroupId1, messageGroupId2);
            }
        }

        return simValue;
    }

    private List<MessageGroupSimilarity> readPrecomputedUserSimilarites() throws IOException {
        String fname = getMessageGroupSimilarityDumpFilename();

        MessageGroupSimilarityOutput mgSimilarityOutput = new MessageGroupSimilarityOutput();

        mgSimilarityOutput.read(fname);

        LOGGER.info("Read {} userSims from {}", mgSimilarityOutput.getElements().size(),
                fname);
        return mgSimilarityOutput.getElements();
    }

    @Override
    public void run() throws IOException {

        StopWatch runTime = new StopWatch();
        runTime.start();

        this.cachedSimilarites.clear();

        if (this.messageGroupSimilarityConfiguration
                .isReadMessageGroupSimilaritiesFromPrecomputedFile()) {
            List<MessageGroupSimilarity> sims = readPrecomputedUserSimilarites();
            for (MessageGroupSimilarity sim : sims) {
                Pair<Long, Long> mgPair = new ImmutablePair<Long, Long>(sim.getMessageGroupId1(),
                        sim.getMessageGroupId2());
                this.cachedSimilarites.put(mgPair, sim);
            }
        } else {

            Collection<MessageGroup> messageGroups = persistence.getAllMessageGroups();
            for (MessageGroup one : messageGroups) {
                for (MessageGroup two : messageGroups) {
                    MessageGroupSimilarity similarity = internalComputeAndCacheSimilarity(
                            one.getId(),
                            two.getId());
                    similarity.setMessageGroupGlobalId1(one.getGlobalId());
                    similarity.setMessageGroupGlobalId2(two.getGlobalId());

                }
            }
        }

        if (this.messageGroupSimilarityConfiguration
                .isWriteMessageGroupSimilaritiesToPrecomputedFile()) {

            MessageGroupSimilarityOutput output = new MessageGroupSimilarityOutput();
            output.getElements().addAll(this.cachedSimilarites.values());
            output.write(getMessageGroupSimilarityDumpFilename());

            LOGGER.info("Wrote {} message group sims to {}.", output.getElements().size(),
                    getMessageGroupSimilarityDumpFilename());
        }

        runTime.stop();

        LOGGER.info("Computing all message group similarities took {} ms.", runTime.getTime());
    }

}
