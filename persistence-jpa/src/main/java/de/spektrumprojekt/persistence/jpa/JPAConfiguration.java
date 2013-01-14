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

package de.spektrumprojekt.persistence.jpa;

import java.util.Properties;

import de.spektrumprojekt.configuration.Configuration;
import de.spektrumprojekt.configuration.SimpleConfigurationHolder;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class JPAConfiguration extends SimpleConfigurationHolder {

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

    private Properties jpaProperties;

    private final static String DEFAULT_PERSISTENCE_UNITE_NAME = "de.spektrumprojekt.datamodel";

    public JPAConfiguration(Configuration configuration) {
        super(configuration);
    }

    /**
     * Builds the jpa properties by taking the properties and take only the JPA depending ones
     */
    private void buildJpaProperties() {
        jpaProperties = new Properties();
        String[] jpaPropertyKeys = new String[] { JAVAX_PERSISTENCE_JDBC_USER,
                JAVAX_PERSISTENCE_JDBC_PASSWORD, JAVAX_PERSISTENCE_JDBC_URL,
                JAVAX_PERSISTENCE_JDBC_DRIVER };
        for (String key : jpaPropertyKeys) {
            String value = this.getConfiguration().getStringProperty(key);
            if (value != null) {
                jpaProperties.put(key, value);
            }
        }
    }

    /**
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
     * <p>
     * Get the name of the persistence unit. If not defined the
     * {@value #DEFAULT_PERSISTENCE_UNITE_NAME} will be used
     * </p>
     * 
     * @return
     */
    public String getPersistenceUnitName() {
        String pUnit = this.getConfiguration().getStringProperty("persistenceUnit",
                DEFAULT_PERSISTENCE_UNITE_NAME);
        return pUnit;
    }

}
