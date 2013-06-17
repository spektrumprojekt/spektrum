package de.spektrumprojekt.informationextraction.relations;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.junit.Assume;
import org.junit.Test;

import de.spektrumprojekt.commons.SpektrumUtils;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.informationextraction.InformationExtractionConfiguration;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;
import de.spektrumprojekt.persistence.simple.SimplePersistence;

public class PatternConsolidationCommandTest {

    private static final String RELATION_SAMPLE_FILE = "/relations/testFeed.xml";

    private static final String RELATION_SAMPLE_FILE_2 = "/relations/confluence.xml";

    @Test
    public void testPatternConsolidationCommand() throws FileNotFoundException {
        assertEquals(119, process(RELATION_SAMPLE_FILE).size());
    }

    @Test
    public void testPatternConsolidationCommand2() throws FileNotFoundException {
        assertEquals(39, process(RELATION_SAMPLE_FILE_2).size());
    }

    private final Map<String, List<Message>> process(String file) {
        File testFile = getTestFile(file);

        FeedTestDataSource dataSource = new FeedTestDataSource(testFile);

        InformationExtractionConfiguration config = new InformationExtractionConfiguration();

        PatternConsolidationConfiguration patternProvider = new PatternConsolidationConfigurationImpl(
                config.getPatternsForConsolidation());

        PatternConsolidationCommand consolidationCommand = new PatternConsolidationCommand(
                patternProvider);
        SimplePersistence persistence = new SimplePersistence();

        for (Message message : dataSource) {
            MessagePart messagePart = message.getMessageParts().iterator().next();
            InformationExtractionContext context = new InformationExtractionContext(persistence,
                    message, messagePart);
            consolidationCommand.process(context);
        }

        Map<String, List<Message>> relations = persistence.getPatternMessages();
        return relations;
    }

    private final File getTestFile(String file) {
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

}
