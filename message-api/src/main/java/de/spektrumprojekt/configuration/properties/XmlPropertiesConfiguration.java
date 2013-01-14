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

import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

public class XmlPropertiesConfiguration extends DefaultPropertiesConfiguration {

    private HierarchicalConfiguration config;

    public XmlPropertiesConfiguration(String configFileName) throws ConfigurationException {
        this(configFileName, null);
    }

    public XmlPropertiesConfiguration(String configFileName, Map<?, ?> defaultProperties)
            throws ConfigurationException {
        super(defaultProperties);

        if (configFileName == null || configFileName.trim().length() == 0) {
            throw new IllegalArgumentException("configFileName cannot be null or empty"
                    + configFileName);
        }
        try {
            this.config = new XMLConfiguration(configFileName);
        } catch (org.apache.commons.configuration.ConfigurationException e) {
            throw new ConfigurationException("Error loading the configuration from \""
                    + configFileName + "\": "
                    + e.getMessage(), e);
        }
    }

    @Override
    protected boolean internalExistsProperty(String key) {
        return config.containsKey(key);
    }

    @Override
    protected int internalGetIntProperty(String key) {
        return config.getInt(key);
    }

    @Override
    protected String internalGetStringProperty(String key) {
        return config.getString(key);
    }
}
