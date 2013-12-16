package de.spektrumprojekt.i.similarity.messagegroup;

import org.apache.commons.lang3.time.DateUtils;

import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.i.similarity.set.SetSimilarity;

public class MessageGroupSimilarityConfiguration implements ConfigurationDescriptable {

    private SetSimilarity setSimilarity;

    private String precomputedMessageGroupSimilaritesFilename;

    private boolean writeMessageGroupSimilaritiesToPrecomputedFile;
    private boolean readMessageGroupSimilaritiesFromPrecomputedFile;
    private boolean precomputedIsWithDate;

    private int intervallOfMGSimComputationInDays = 1;
    private long intervallOfMessagesToConsiderInMs = 30 * 24 * DateUtils.MILLIS_PER_HOUR;

    private long similarityTimeToLive = Long.MAX_VALUE;

    @Override
    public String getConfigurationDescription() {
        return toString();
    }

    public long getIntervallOfMessagesToConsiderInMs() {
        return intervallOfMessagesToConsiderInMs;
    }

    public int getIntervallOfMGSimComputationInDays() {
        return intervallOfMGSimComputationInDays;
    }

    public String getPrecomputedMessageGroupSimilaritesFilename() {
        return precomputedMessageGroupSimilaritesFilename;
    }

    public SetSimilarity getSetSimilarity() {
        return setSimilarity;
    }

    public long getSimilarityTimeToLive() {
        return similarityTimeToLive;
    }

    public boolean isPrecomputedIsWithDate() {
        return precomputedIsWithDate;
    }

    public boolean isReadMessageGroupSimilaritiesFromPrecomputedFile() {
        return readMessageGroupSimilaritiesFromPrecomputedFile;
    }

    public boolean isWriteMessageGroupSimilaritiesToPrecomputedFile() {
        return writeMessageGroupSimilaritiesToPrecomputedFile;
    }

    public void setIntervallOfMessagesToConsiderInMs(long intervallOfMessagesToConsiderInMs) {
        this.intervallOfMessagesToConsiderInMs = intervallOfMessagesToConsiderInMs;
    }

    public void setIntervallOfMGSimComputationInDays(int intervallOfMGSimComputationInDays) {
        this.intervallOfMGSimComputationInDays = intervallOfMGSimComputationInDays;
    }

    public void setPrecomputedIsWithDate(boolean precomputedIsWithDate) {
        this.precomputedIsWithDate = precomputedIsWithDate;
    }

    public void setPrecomputedMessageGroupSimilaritesFilename(
            String precomputedMessageGroupSimilaritesFilename) {
        this.precomputedMessageGroupSimilaritesFilename = precomputedMessageGroupSimilaritesFilename;
    }

    public void setReadMessageGroupSimilaritiesFromPrecomputedFile(
            boolean readMessageGroupSimilaritiesFromPrecomputedFile) {
        this.readMessageGroupSimilaritiesFromPrecomputedFile = readMessageGroupSimilaritiesFromPrecomputedFile;
    }

    public void setSetSimilarity(SetSimilarity setSimilarity) {
        this.setSimilarity = setSimilarity;
    }

    public void setSimilarityTimeToLive(long similarityTimeToLive) {
        this.similarityTimeToLive = similarityTimeToLive;
    }

    public void setWriteMessageGroupSimilaritiesToPrecomputedFile(
            boolean writeMessageGroupSimilaritiesToPrecomputedFile) {
        this.writeMessageGroupSimilaritiesToPrecomputedFile = writeMessageGroupSimilaritiesToPrecomputedFile;
    }

    @Override
    public String toString() {
        return "MessageGroupSimilarityConfiguration [setSimilarity=" + setSimilarity
                + ", precomputedMessageGroupSimilaritesFilename="
                + precomputedMessageGroupSimilaritesFilename
                + ", writeMessageGroupSimilaritiesToPrecomputedFile="
                + writeMessageGroupSimilaritiesToPrecomputedFile
                + ", readMessageGroupSimilaritiesFromPrecomputedFile="
                + readMessageGroupSimilaritiesFromPrecomputedFile + ", precomputedIsWithDate="
                + precomputedIsWithDate + ", intervallOfMGSimComputationInDays="
                + intervallOfMGSimComputationInDays + ", intervallOfMessagesToConsiderInMs="
                + intervallOfMessagesToConsiderInMs + ", similarityTimeToLive="
                + similarityTimeToLive + "]";
    }

}