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

package de.spektrumprojekt.datamodel.common;

import javax.persistence.Entity;
import javax.persistence.Lob;

import org.apache.commons.lang3.Validate;

import de.spektrumprojekt.datamodel.identifiable.Identifiable;
import de.spektrumprojekt.datamodel.message.Message;

/**
 * <p>
 * Key-value based meta information for {@link Message}s.
 * </p>
 * 
 * @author Philipp Katz
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 */
@Entity
public class Property extends Identifiable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public final static String PROPERTY_KEY_TITLE = "title";

    /**
     * the key of the property which contains the link to the original entry
     */
    public static final String PROPERTY_KEY_LINK = "link";

    /**
     * Key of the property which marks a message as external message.
     */
    public static final String PROPERTY_KEY_EXTERNAL = "contentTypes.external";
    /**
     * Value of the property which marks a message as external message.
     */
    public static final String PROPERTY_VALUE_EXTERNAL = "external";

    /**
     * Key of the property which marks a message as an activity.
     */
    public static final String PROPERTY_KEY_ACTIVITY = "contentTypes.activity";
    /**
     * Value of the property which marks a message as an activity.
     */
    public static final String PROPERTY_VALUE_ACTIVITY = "activity";

    /**
     * Key of the property which contains tags copied from the source
     */
    public static final String PROPERTY_KEY_TAGS = "tags";

    public static final String PROPERTY_KEY_DC_CREATOR = "dc:creator";

    public static final String PROPERTY_KEY_AUTHOR_NAME = "autor.name";

    private String propertyKey;

    @Lob
    private String propertyValue;

    /** Constructor for ORM layer only. */
    protected Property() {

    }

    /**
     * <p>
     * Initialize a new {@link Property} with the specified key and value.
     * </p>
     * 
     * @param key
     *            The key, not <code>null</code> or empty.
     * @param value
     *            The value.
     */
    public Property(String key, String value) {
        Validate.notEmpty(key, "key must not be empty");

        this.propertyKey = key;
        this.propertyValue = value;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Property [propertyKey=");
        builder.append(propertyKey);
        builder.append(", propertyValue=");
        builder.append(propertyValue);
        builder.append(", getGlobalId()=");
        builder.append(getGlobalId());
        builder.append(", getId()=");
        builder.append(getId());
        builder.append("]");
        return builder.toString();
    }

}
