package de.spektrumprojekt.informationextraction;

import java.io.File;
import java.util.Collection;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.configuration.SimpleConfigurationHolder;
import de.spektrumprojekt.configuration.SpektrumConfiguration;
import de.spektrumprojekt.configuration.properties.XmlPropertiesConfiguration;

public final class InformationExtractionConfiguration extends SimpleConfigurationHolder {

    /** The logger for this class. */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(InformationExtractionConfiguration.class);

    private static final String EXTRACTION_PROPERTIES_FILE = "information-extraction.properties.xml";

    private static String getExtractionXmlFilename() {
        String configFileName = SpektrumConfiguration.INSTANCE.getConfigDirectory()
                + File.separator
                + EXTRACTION_PROPERTIES_FILE;
        return configFileName;
    }

    public InformationExtractionConfiguration() {
        String fileName = getExtractionXmlFilename();
        XmlPropertiesConfiguration config;
        try {
            config = new XmlPropertiesConfiguration(fileName);
            LOGGER.debug("Loaded configuration from {}", fileName);
        } catch (ConfigurationException e) {
            throw new IllegalStateException("Error while loading the configuration from "
                    + fileName + ": " + e.getMessage());
        }
        setConfiguration(config);
    }

    public Collection<String> getPatternsForConsolidation() {
        return configuration.getListProperty("pattern");
    }

}
