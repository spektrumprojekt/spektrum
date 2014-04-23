package de.spektrumprojekt.i.evaluation.recallstats;

import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.ext.EnglishStemmer;
import org.testng.annotations.Test;

public class TestStemmer {

    @Test
    public void testStemmer() {
        SnowballProgram stemmer = new EnglishStemmer();

        stemmer.setCurrent("extraction");
        stemmer.stem();

        System.out.println(stemmer.getCurrent());
    }

}
