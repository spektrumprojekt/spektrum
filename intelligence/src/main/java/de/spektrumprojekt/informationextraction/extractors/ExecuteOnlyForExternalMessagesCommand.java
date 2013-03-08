package de.spektrumprojekt.informationextraction.extractors;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.informationextraction.InformationExtractionContext;

/**
 * decorator which makes a command only be executed if the {@link Message} is a external one, it has
 * to have a {@link Property} with key {@link Property#PROPERTY_KEY_EXTERNAL} and value
 * {@link Property#PROPERTY_VALUE_EXTERNAL}
 * 
 * 
 */
public class ExecuteOnlyForExternalMessagesCommand implements Command<InformationExtractionContext> {

    private final Command<InformationExtractionContext> command;

    public ExecuteOnlyForExternalMessagesCommand(Command<InformationExtractionContext> command) {
        super();
        this.command = command;
    }

    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + "[" + command.getConfigurationDescription() + "]";
    }

    @Override
    public void process(InformationExtractionContext context) {
        Property externalProperty = context.getMessage().getPropertiesAsMap()
                .get(Property.PROPERTY_KEY_EXTERNAL);
        if (externalProperty != null
                && externalProperty.getPropertyValue().equals(Property.PROPERTY_VALUE_EXTERNAL)) {
            command.process(context);
        }
    }

}
