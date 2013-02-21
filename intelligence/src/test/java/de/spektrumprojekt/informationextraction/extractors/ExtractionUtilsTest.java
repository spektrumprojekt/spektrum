package de.spektrumprojekt.informationextraction.extractors;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ExtractionUtilsTest {
    
    @Test
    public void testCreateNGrams() {
        List<String> tokens = Arrays.asList("the","quick","brown","fox","jumps","over","the","lazy","dog");
        List<String> nGramTokens = ExtractionUtils.createNGrams(tokens, 3);
        assertEquals(7, nGramTokens.size());
        assertEquals("the quick brown", nGramTokens.get(0));
        assertEquals("quick brown fox", nGramTokens.get(1));
        assertEquals("brown fox jumps", nGramTokens.get(2));
    }

    @Test
    public void testCreateCharNGrams() {
        String text = "the quick brown fox";
        List<String> charNGrams = ExtractionUtils.createCharNGrams(text, 3);
        assertEquals(17, charNGrams.size());
        assertEquals("the", charNGrams.get(0));
        assertEquals("he ", charNGrams.get(1));
        assertEquals("e q", charNGrams.get(2));

        charNGrams = ExtractionUtils.createCharNGrams(text, 2, 5);
        assertEquals(66, charNGrams.size());
    }

}
