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

package de.spektrumprojekt.aggregator.adapter;

import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Helper class for loading the test configuration file. This file contains data like logins and passwords for testing
 * adapters which we do not want to commit to the repository.
 * </p>
 * 
 * @author Philipp Katz
 */
public final class ConfigurationForT {
    /** The logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationForT.class);
    /** File path from where the test configuration file is loaded. */
    private static final String CONFIGURATION_FILE = "src/test/resources/testConfiguration.properties";
    /** The actual configuration instance. */
    private Configuration configuration;

    private ConfigurationForT() {
    }

    private static class SingletonHolder {
        public static final ConfigurationForT INSTANCE = new ConfigurationForT();
    }

    public static ConfigurationForT getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * <p>
     * Get the test configuration.
     * </p>
     * 
     * @return The test configuration, or <code>null</code> if test configuration cannot be loaded.
     */
    public Configuration getConfiguration() {
        if (configuration == null) {
            File configurationFile = new File(CONFIGURATION_FILE);
            if (configurationFile.exists()) {
                try {
                    configuration = new PropertiesConfiguration(CONFIGURATION_FILE);
                } catch (ConfigurationException e) {
                    throw new IllegalStateException("Error loading the test configuration from \"" + CONFIGURATION_FILE
                            + "\": " + e);
                }
            } else {
                LOGGER.trace("No configuration file at \"" + configurationFile.getAbsolutePath() + "\"");
            }
        }
        return configuration;
    }

}
