package de.spektrumprojekt.informationextraction.relations2;

import java.io.File;

import org.junit.Test;

import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.i.TestHelper;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;
import de.spektrumprojekt.informationextraction.relations.FeedTestDataSource;
import de.spektrumprojekt.persistence.simple.SimplePersistence;

public class ClusterCommandTest {

    @Test
    public void testClusterCommand() {
        File testFile = TestHelper.getTestFile("/relations/confluence.xml");

        ClusterCommand command = new ClusterCommand(DefaultMessageSimilarity.INSTANCE, 0.8f, 5000);
        FeedTestDataSource source = new FeedTestDataSource(testFile);
        SimplePersistence persistence = new SimplePersistence();

        for (Message message : source) {
            MessagePart messagePart = message.getMessageParts().iterator().next();
            InformationExtractionContext context = new InformationExtractionContext(persistence,
                    message, messagePart);
            command.process(context);
        }

        command.dumpClusters(System.out);

    }

}
