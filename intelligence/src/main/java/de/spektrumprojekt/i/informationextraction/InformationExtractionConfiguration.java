package de.spektrumprojekt.i.informationextraction;

import de.spektrumprojekt.i.term.frequency.TermFrequencyComputer;
import de.spektrumprojekt.persistence.Persistence;

public class InformationExtractionConfiguration {

    public Persistence persistence;
    public TermFrequencyComputer termFrequencyComputer;
    public boolean addTagsToText;
    public boolean doTokens;
    public boolean doTags;
    public boolean doKeyphrase;
    public boolean beMessageGroupSpecific;
    public int minimumTermLength;
    public boolean useWordNGramsInsteadOfStemming;
    public int nGramsLength;
    public boolean useCharNGramsInsteadOfStemming;
    public boolean charNGramsRemoveStopwords;

    public InformationExtractionConfiguration(Persistence persistence,
            TermFrequencyComputer termFrequencyComputer, boolean addTagsToText, boolean doTokens,
            boolean doTags, boolean doKeyphrase, boolean beMessageGroupSpecific,
            int minimumTermLength) {
        this.persistence = persistence;
        this.termFrequencyComputer = termFrequencyComputer;
        this.addTagsToText = addTagsToText;
        this.doTokens = doTokens;
        this.doTags = doTags;
        this.doKeyphrase = doKeyphrase;
        this.beMessageGroupSpecific = beMessageGroupSpecific;
        this.minimumTermLength = minimumTermLength;
    }
}