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

package de.spektrumprojekt.configuration.properties;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import de.spektrumprojekt.configuration.Configuration;

/**
 * Configuration implementing the default properties behavior
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public abstract class DefaultPropertiesConfiguration implements Configuration {

    private final Properties defaultProperties = new Properties();

    /**
     * 
     * @param defaultProperties
     *            the default properties, null or empty if there are no1ne.
     */
    public DefaultPropertiesConfiguration(Map<?, ?> defaultProperties) {
        if (defaultProperties != null) {
            this.defaultProperties.putAll(defaultProperties);
        }
    }

    /**
     * {@inheritDoc}
     */

    @Override
    public boolean existsProperty(String key) {
        return this.defaultProperties.containsKey(key) || internalExistsProperty(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Properties getDefaultProperties() {
        return defaultProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getIntProperty(String key) {
        if (existsProperty(key)) {
            return internalGetIntProperty(key);
        }
        if (defaultProperties.containsKey(key)) {
            return Integer.parseInt(defaultProperties.getProperty(key));
        }
        // this assure defined behavior of getting a not existing null property.
        throw new NumberFormatException("property key=" + key + " not defined.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIntProperty(String key, int defaultValue) {
        if (internalExistsProperty(key)) {
            return internalGetIntProperty(key);
        }
        if (defaultProperties.containsKey(key)) {
            return Integer.parseInt(defaultProperties.getProperty(key));
        }
        return defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getStringProperty(String key) {
        return getStringProperty(key, defaultProperties.getProperty(key));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStringProperty(String key, String defaultValue) {
        if (internalExistsProperty(key)) {
            return internalGetStringProperty(key);
        }
        if (defaultProperties.containsKey(key)) {
            return defaultProperties.getProperty(key);
        }
        return defaultValue;
    }

    @Override
    public List<String> getListProperty(String key) {
        return getListProperty(key, Collections.singletonList(defaultProperties.getProperty(key)));
    }

    @Override
    public List<String> getListProperty(String key, List<String> defaultValues) {
        if (internalExistsProperty(key)) {
            return internalGetListProperty(key);
        }
        if (defaultProperties.containsKey(key)) {
            return Collections.singletonList(defaultProperties.getProperty(key));
        }
        return defaultValues;
    }

    /**
     * Internal method the actually get the property from an underlying configuration (file,
     * database, etc)
     * 
     * @param key
     *            the key
     * @return true if the key exists
     */
    protected abstract boolean internalExistsProperty(String key);

    /**
     * Internal method the actually get the property from an underlying configuration (file,
     * database, etc)
     * 
     * @param key
     *            the key
     * @return the value to the key. throws exception if key is null or cannot be parsed.
     */
    protected abstract int internalGetIntProperty(String key);

    /**
     * Internal method the actually get the property from an underlying configuration (file,
     * database, etc)
     * 
     * @param key
     *            the key
     * @return the value.
     */
    protected abstract String internalGetStringProperty(String key);

    /**
     * Internal method the actually get the property from an underlying configuration (file,
     * database, etc)
     * 
     * @param key the key.
     * @return the value.
     */
    protected abstract List<String> internalGetListProperty(String key);

}
