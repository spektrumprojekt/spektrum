package de.spektrumprojekt.informationextraction.relations;

import java.io.FileNotFoundException;

import org.junit.Test;

import de.spektrumprojekt.commons.SpektrumUtils;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.simple.SimplePersistence;

public class InteractionConsolidationCommandTest {

    @Test
    public void testInteractionConsolidationCommand() throws FileNotFoundException {
        CommunoteTestDataSource dataSource = new CommunoteTestDataSource(
                SpektrumUtils.getTestResource("/relations/testFeed.xml"));

        InteractionConsolidationCommand consolidationCommand = new InteractionConsolidationCommand();
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
