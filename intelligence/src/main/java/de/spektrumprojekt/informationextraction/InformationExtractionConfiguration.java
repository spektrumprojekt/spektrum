package de.spektrumprojekt.informationextraction;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.SpektrumUtils;
import de.spektrumprojekt.configuration.SimpleConfigurationHolder;
import de.spektrumprojekt.configuration.SpektrumConfiguration;
import de.spektrumprojekt.configuration.properties.XmlPropertiesConfiguration;
import de.spektrumprojekt.informationextraction.relations.NamePattern;

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

    public Collection<NamePattern> getPatternsForConsolidation() {
        Collection<NamePattern> result = new HashSet<NamePattern>();
        for (int i = 0;; i++) {
            // yes, this is ugly, but the the configuration does not support hierarchies.
            String name = configuration.getStringProperty("pattern-name-" + i);
            String regex = configuration.getStringProperty("pattern-regex-" + i);
            if (SpektrumUtils.nullOrEmpty(name) || SpektrumUtils.nullOrEmpty(regex)) {
                break;
            }
            result.add(new NamePattern(name, regex));
        }
        return result;
    }

}
