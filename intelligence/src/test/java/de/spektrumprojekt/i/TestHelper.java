package de.spektrumprojekt.i;

import java.io.File;

import org.junit.Assume;
import org.junit.Ignore;

import de.spektrumprojekt.commons.SpektrumUtils;
import de.spektrumprojekt.informationextraction.relations.PatternConsolidationCommandTest;

@Ignore
public final class TestHelper {

    public static final File getTestFile(String file) {
        File testFile = null;
        try {
            testFile = SpektrumUtils.getTestResource(file);
        } catch (Exception e) {
            System.out.println("Skipping " + PatternConsolidationCommandTest.class
                    + " because test file is missing.");
            Assume.assumeTrue(false);
        }
        return testFile;
    }

    private TestHelper() {

    }

}
