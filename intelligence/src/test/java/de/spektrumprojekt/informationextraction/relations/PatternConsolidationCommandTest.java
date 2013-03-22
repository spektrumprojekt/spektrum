package de.spektrumprojekt.informationextraction.relations;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Assume;
import org.junit.Test;

import de.spektrumprojekt.commons.SpektrumUtils;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.informationextraction.InformationExtractionConfiguration;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.simple.SimplePersistence;

public class PatternConsolidationCommandTest {

    private static final String RELATION_SAMPLE_FILE = "/relations/testFeed.xml";

    @Test
    public void testInteractionConsolidationCommand() throws FileNotFoundException {
        File testFile = null;
        try {
            testFile = SpektrumUtils.getTestResource(RELATION_SAMPLE_FILE);
        } catch (Exception e) {
            System.out.println("Skipping " + PatternConsolidationCommandTest.class
                    + " because test file is missing.");
            Assume.assumeTrue(false);
        }

        CommunoteTestDataSource dataSource = new CommunoteTestDataSource(testFile);

        InformationExtractionConfiguration config = new InformationExtractionConfiguration();

        PatternConsolidationConfiguration patternProvider = new PatternConsolidationConfigurationImpl(
                config.getPatternsForConsolidation());

        PatternConsolidationCommand consolidationCommand = new PatternConsolidationCommand(
                patternProvider);
        Persistence persistence = new SimplePersistence();

        for (Message message : dataSource) {
            MessagePart messagePart = message.getMessageParts().iterator().next();
            InformationExtractionContext context = new InformationExtractionContext(persistence,
                    message, messagePart);
            consolidationCommand.process(context);
        }

        System.out.println(consolidationCommand.getConfigurationDescription());
    }

}
