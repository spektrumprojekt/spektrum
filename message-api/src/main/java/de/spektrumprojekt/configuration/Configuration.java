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

import java.util.Properties;

/**
 * Configuration interface to access configured properties.
 * 
 * This configuration allows to define default properties using {@link #getDefaultProperties()}.
 * Properties added here are evaluated on the get methods.
 * 
 * The contract order of getting a property is as follows: <br>
 * 1st Access the file, or properties besides any default (e.g. if a xml file is configured, use it)<br>
 * 2nd Consult the default properties defined in {@link #getDefaultProperties()}<br>
 * 3rd Use the default value given by the method (e.g. {@link #getStringProperty(String, String)}<br>
 * <br>
 * Giving the default properties precedence over the default value of the method assures that a call
 * to {@link #getStringProperty(String)} is equal to {@link #getStringProperty(String, String)} if a
 * default property has been defined
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public interface Configuration {

    /**
     * 
     * @param key
     *            the key to check
     * @return true if the property exists
     */
    public boolean existsProperty(String key);

    /**
     * Default properties are those, that are used in case the property does not exist in the config
     * and no default value has been givne in the method, so its the final fallback.
     * 
     * @return
     */
    public Properties getDefaultProperties();

    /**
     * 
     * @param key
     *            the key to get
     * @return the int value of the property. Throws an exception if key is null or cannot be
     *         parsed.
     */
    public int getIntProperty(String key);

    /**
     * See the class documentation. First check the underlying configuration (file, database), than
     * the {@link #getDefaultProperties()} and then if still not found use the given default value
     * 
     * @param key
     *            the key to get
     * @param defaultValue
     *            the default value
     * @return the value. Throws an exception if key cannot be parsed.
     */
    public int getIntProperty(String key, int defaultValue);

    /**
     * 
     * @param key
     *            the key
     * @return the value of the key
     */
    public String getStringProperty(String key);

    /**
     * See the class documentation. First check the underlying configuration (file, database), than
     * the {@link #getDefaultProperties()} and then if still not found use the given default value
     * 
     * @param key
     *            the key
     * @param defaultValue
     *            the default value
     * @return the value.
     */
    public String getStringProperty(String key, String defaultValue);
}
