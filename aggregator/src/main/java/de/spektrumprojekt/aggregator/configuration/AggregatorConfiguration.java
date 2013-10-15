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

package de.spektrumprojekt.aggregator.configuration;

import java.io.File;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.configuration.ConfigurationException;

import de.spektrumprojekt.configuration.Configuration;
import de.spektrumprojekt.configuration.SimpleConfigurationHolder;
import de.spektrumprojekt.configuration.SpektrumConfiguration;
import de.spektrumprojekt.configuration.properties.XmlPropertiesConfiguration;

/**
 * <p>
 * Helper to read configuration file from XML and provide a {@link ComponentConfigurationData}
 * instance.
 * </p>
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * @author Marius Feldmann
 * @author Philipp Katz
 */
public final class AggregatorConfiguration extends SimpleConfigurationHolder {

    /** Filename with aggregator properties. */
    private static final String AGGREGATOR_PROPERTIES_FILE = "aggregator.properties.xml";

    private static String getAggregatorXmlFilename() {
        String configFileName = SpektrumConfiguration.INSTANCE.getConfigDirectory()
                + File.separator
                + AGGREGATOR_PROPERTIES_FILE;
        return configFileName;
    }

    /**
     * Load the config using the given name in the application config directorz
     * 
     * @param filename
     * @return
     * @throws ConfigurationException
     */
    public static AggregatorConfiguration loadXmlByFilename(String filename)
            throws ConfigurationException {
        return loadXmlByFullname(SpektrumConfiguration.INSTANCE.getConfigDirectory()
                + File.separator
                + filename);
    }

    /**
     * Load the config using the given full file name
     * 
     * @param filename
     * @return
     * @throws ConfigurationException
     */
    public static AggregatorConfiguration loadXmlByFullname(String filename)
            throws ConfigurationException {
        Configuration configuration = new XmlPropertiesConfiguration(filename);

        AggregatorConfiguration aggregatorConfiguration = new AggregatorConfiguration();
        aggregatorConfiguration.setConfiguration(configuration);

        return aggregatorConfiguration;
    }

    /**
     * Loads the configuration new by standard property files
     * 
     * @return
     * @throws ConfigurationException
     */
    public static AggregatorConfiguration loadXmlConfig() throws ConfigurationException {
        return loadXmlByFullname(getAggregatorXmlFilename());

    }

    public AggregatorConfiguration() {
    }

    public AggregatorConfiguration(Configuration configuration) {
        super(configuration);
    }

    /**
     * <p>
     * Get the name of the persistence unit used by the aggregator.
     * </p>
     * 
     * @return
     */
    public String getEncryptionPassword() {
        return configuration.getStringProperty("encryption.password");
    }

    /**
     * <p>
     * Number of consecutive errors, until an error message is sent.
     * </p>
     * 
     * @return
     */
    public int getErrorsForMessage() {
        return configuration.getIntProperty("adapter.errorsForMessage");
    }

    /**
     * <p>
     * Maximum number of consecutive errors, until a subscription is blocked from being checked.
     * </p>
     * 
     * @return
     */
    public int getMaxConsecErrors() {
        return configuration.getIntProperty("adapter.errorsForBlock");
    }

    /**
     * <p>
     * maximum number hashes for duplicate detection
     * </p>
     * 
     * @return
     */
    public int getMaxHashes() {
        return configuration.getIntProperty("duplicateDetetion.maxHashes");
    }

    /**
     * <p>
     * minimum number hashes for duplicate detection
     * </p>
     * 
     * @return
     */
    public int getMinHashes() {
        return configuration.getIntProperty("duplicateDetetion.minHashes");
    }

    /**
     * 
     * @return only consider message after this date
     */
    public Date getMinimumPublicationDate() {
        String dateStr = configuration.getStringProperty("adapter.minimumPublicationDate");
        Date date;
        try {
            date = dateStr == null ? null : getDateFormat().parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing configuration date for dateStr=" + dateStr
                    + " " + e.getMessage(), e);
        }
        return date;
    }

    /**
     * <p>
     * Get the name of the persistence unit used by the aggregator.
     * </p>
     * 
     * @return
     */
    public String getPersistenceUnitName() {
        return configuration.getStringProperty("persistenceUnit");
    }

    /**
     * <p>
     * The interval used by polling adapters, in seconds.
     * </p>
     * 
     * @return
     */
    public int getPollingInterval() {
        return configuration.getIntProperty("adapter.pollingInterval");
    }

    /**
     * <p>
     * The interval used by polling adapters, in seconds.
     * </p>
     * 
     * @return
     */
    public int getThreadPoolSize() {
        return configuration.getIntProperty("adapter.threadPoolSize", 1);
    }

    /**
     * <p>
     * Get the "consumer key" necessary for the Twitter adapter.
     * </p>
     * 
     * @return
     */
    public String getTwitterConsumerKey() {
        return configuration.getStringProperty("adapter.twitter.consumerKey");
    }

    /**
     * <p>
     * Get the "consumer secret" necessary for the Twitter adapter.
     * </p>
     * 
     * @return
     */
    public String getTwitterConsumerSecret() {
        return configuration.getStringProperty("adapter.twitter.consumerSecret");
    }

}
