package de.spektrumprojekt.informationextraction.extractors;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.spektrumprojekt.informationextraction.extractors.ExtractionUtils;

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

}
