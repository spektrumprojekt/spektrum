package de.spektrumprojekt.i.informationextraction;

import de.spektrumprojekt.i.term.frequency.TermFrequencyComputer;
import de.spektrumprojekt.informationextraction.extractors.TagSource;

public class InformationExtractionConfiguration {

    private TagSource tagSource;

    private TermFrequencyComputer termFrequencyComputer;

    private boolean addTagsToText;

    private boolean beMessageGroupSpecific;

    private boolean charNGramsRemoveStopwords;

    private boolean doTokens = true;

    private boolean doTags;

    private boolean doKeyphrase;

    private boolean matchTextAgainstTagSource;

    private int minimumTermLength;

    private int nGramsLength = 2;

    private boolean useWordNGramsInsteadOfStemming;

    private boolean useCharNGramsInsteadOfStemming;

    public int getMinimumTermLength() {
        return minimumTermLength;
    }

    public int getnGramsLength() {
        return nGramsLength;
    }

    public TagSource getTagSource() {
        return tagSource;
    }

    public TermFrequencyComputer getTermFrequencyComputer() {
        return termFrequencyComputer;
    }

    public boolean isAddTagsToText() {
        return addTagsToText;
    }

    public boolean isBeMessageGroupSpecific() {
        return beMessageGroupSpecific;
    }

    public boolean isCharNGramsRemoveStopwords() {
        return charNGramsRemoveStopwords;
    }

    public boolean isDoKeyphrase() {
        return doKeyphrase;
    }

    public boolean isDoTags() {
        return doTags;
    }

    public boolean isDoTokens() {
        return doTokens;
    }

    public boolean isMatchTextAgainstTagSource() {
        return matchTextAgainstTagSource;
    }

    public boolean isUseCharNGramsInsteadOfStemming() {
        return useCharNGramsInsteadOfStemming;
    }

    public boolean isUseWordNGramsInsteadOfStemming() {
        return useWordNGramsInsteadOfStemming;
    }

    public void setAddTagsToText(boolean addTagsToText) {
        this.addTagsToText = addTagsToText;
    }

    public void setBeMessageGroupSpecific(boolean beMessageGroupSpecific) {
        this.beMessageGroupSpecific = beMessageGroupSpecific;
    }

    public void setCharNGramsRemoveStopwords(boolean charNGramsRemoveStopwords) {
        this.charNGramsRemoveStopwords = charNGramsRemoveStopwords;
    }

    public void setDoKeyphrase(boolean doKeyphrase) {
        this.doKeyphrase = doKeyphrase;
    }

    public void setDoTags(boolean doTags) {
        this.doTags = doTags;
    }

    public void setDoTokens(boolean doTokens) {
        this.doTokens = doTokens;
    }

    public void setMatchTextAgainstTagSource(boolean matchTextAgainstTagSource) {
        this.matchTextAgainstTagSource = matchTextAgainstTagSource;
    }

    public void setMinimumTermLength(int minimumTermLength) {
        this.minimumTermLength = minimumTermLength;
    }

    public void setnGramsLength(int nGramsLength) {
        this.nGramsLength = nGramsLength;
    }

    public void setTagSource(TagSource tagSource) {
        this.tagSource = tagSource;
    }

    public void setTermFrequencyComputer(TermFrequencyComputer termFrequencyComputer) {
        this.termFrequencyComputer = termFrequencyComputer;
    }

    public void setUseCharNGramsInsteadOfStemming(boolean useCharNGramsInsteadOfStemming) {
        this.useCharNGramsInsteadOfStemming = useCharNGramsInsteadOfStemming;
    }

    public void setUseWordNGramsInsteadOfStemming(boolean useWordNGramsInsteadOfStemming) {
        this.useWordNGramsInsteadOfStemming = useWordNGramsInsteadOfStemming;
    }

    @Override
    public String toString() {
        return "InformationExtractionConfiguration [tagSource=" + tagSource
                + ", termFrequencyComputer=" + termFrequencyComputer + ", addTagsToText="
                + addTagsToText + ", beMessageGroupSpecific=" + beMessageGroupSpecific
                + ", charNGramsRemoveStopwords=" + charNGramsRemoveStopwords + ", doTokens="
                + doTokens + ", doTags=" + doTags + ", doKeyphrase=" + doKeyphrase
                + ", matchTextAgainstTagSource=" + matchTextAgainstTagSource
                + ", minimumTermLength=" + minimumTermLength + ", nGramsLength=" + nGramsLength
                + ", useWordNGramsInsteadOfStemming=" + useWordNGramsInsteadOfStemming
                + ", useCharNGramsInsteadOfStemming=" + useCharNGramsInsteadOfStemming + "]";
    }
}