/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
* 
* http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package de.spektrumprojekt.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for common Spektrum configuration constants and methods.
 * 
 * It looks for a Spektrum.properties file in the Spektrum configuration directory. That must be a
 * default Java Properties file. If you want to override the JPA default just define the JPA
 * properties (url, username, password, driver) in there.
 * 
 * @author Torsten Lunze
 * @author Philipp Katz
 */
public class SpektrumConfiguration {

    public static SpektrumConfiguration INSTANCE = new SpektrumConfiguration();

    /** The logger for this class. */
    private static final Logger logger = LoggerFactory
            .getLogger(SpektrumConfiguration.class);

    /** The default path to the configuration file. */
    private static final String DEFAULT_CONFIG_PATH = "src/main/resources/cfg";

    /**
     * Name of the system property there the config files are stored. The directory of the server
     * properties can be configured using a system variable "de.spektrumprojekt.conf.dir", e.g<br>
     * -Dde.spektrumprojekt.conf.dir="conf"
     */
    public static String PROPERTY_CONFIG_DIR = "de.spektrumprojekt.conf.dir";

    /** Filename with aggregator properties. */
    private static final String PROPERTIES_FILE = "spektrum.properties";

    /**
     * Property Key Name for JPA Config. The password.
     */
    public static final String JAVAX_PERSISTENCE_JDBC_PASSWORD = "javax.persistence.jdbc.password";
    /**
     * Property Key Name for JPA Config. The user.
     */
    public static final String JAVAX_PERSISTENCE_JDBC_USER = "javax.persistence.jdbc.user";
    /**
     * Property Key Name for JPA Config. The url.
     */
    public static final String JAVAX_PERSISTENCE_JDBC_URL = "javax.persistence.jdbc.url";
    /**
     * Property Key Name for JPA Config. The jdbc driver.
     */
    public static final String JAVAX_PERSISTENCE_JDBC_DRIVER = "javax.persistence.jdbc.driver";

    private Properties properties;
    private Properties jpaProperties;

    private SpektrumConfiguration() {
    }

    /**
     * Builds the jpa properties by taking the spektrum properties and take only the JPA depending
     * ones
     */
    private void buildJpaProperties() {
        jpaProperties = new Properties();
        String[] jpaPropertyKeys = new String[] { JAVAX_PERSISTENCE_JDBC_USER,
                JAVAX_PERSISTENCE_JDBC_PASSWORD, JAVAX_PERSISTENCE_JDBC_URL,
                JAVAX_PERSISTENCE_JDBC_DRIVER };
        for (String key : jpaPropertyKeys) {
            String value = this.getProperties().getProperty(key);
            if (value != null) {
                jpaProperties.put(key, value);
            }
        }
    }

    /**
     * <p>
     * The directory of the server properties can be configured using a system variable
     * "de.spektrumprojekt.conf.dir", e. g. <code>-Dde.spektrumprojekt.conf.dir="conf"</code>
     * </p>
     * 
     * @return the path to the directory with the properties.
     */
    public String getConfigDirectory() {
        String configDirectory = System
                .getProperty(PROPERTY_CONFIG_DIR, DEFAULT_CONFIG_PATH);
        logger.debug("config directory: " + configDirectory);
        return configDirectory;
    }

    /**
     * The jpa properties are build by taking the communote properties and overwriting the values if
     * set in the spektrum properties
     * 
     * @return The JPA properties
     */
    public Properties getJpaProperties() {
        if (this.jpaProperties == null) {
            buildJpaProperties();
        }
        return jpaProperties;
    }

    /**
     * {@inheritDoc}
     */
    public Properties getProperties() {
        if (properties == null) {
            loadProperties();
        }
        return properties;
    }

    /**
     * <p>
     * The directory of the server properties can be configured using a system variable
     * "de.spektrumprojekt.conf.dir", e. g. <code>-Dde.spektrumprojekt.conf.dir="conf"</code>
     * </p>
     * 
     * @return the file path to the server properties
     */
    private String getPropertiesFileName() {
        String path = System.getProperty(
                SpektrumConfiguration.PROPERTY_CONFIG_DIR,
                DEFAULT_CONFIG_PATH);
        path += File.separator + PROPERTIES_FILE;
        logger.debug("Properties File=" + path);
        return path;
    }

    /**
     * Load the properties based on {@link #getPropertiesFileName()}
     */
    private synchronized void loadProperties() {
        String propertiesFileName = getPropertiesFileName();

        Properties props = new Properties();
        FileInputStream fis = null;

        try {
            File file = new File(propertiesFileName);
            if (file.exists()) {
                fis = new FileInputStream(file);

                props.load(fis);
            } else {
                logger.info("File "
                        + propertiesFileName
                        + " not found. Not loading any specific properties, assuming defaults");
            }
        } catch (Exception e) {
            logger.error("Error configurating: " + e.getMessage(), e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e2) {
                }
            }
        }
        this.properties = props;
    }

}
