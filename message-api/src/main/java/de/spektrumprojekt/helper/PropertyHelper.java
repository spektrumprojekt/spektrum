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

package de.spektrumprojekt.helper;

import java.util.Collection;
import java.util.Iterator;

import de.spektrumprojekt.datamodel.common.Property;

/**
 * Helper to work with {@link Property}s
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class PropertyHelper {
    /**
     * Add all properties from the passed collection to the target collection and avoid duplicates.
     * If a property with a key already exists it will be replaced.
     * 
     * @param propertiesToAdd
     *            the properties to add, can be null
     * @param target
     *            the collection to add to
     */
    public static void addAllProperties(Collection<Property> propertiesToAdd,
            Collection<Property> target) {
        if (propertiesToAdd != null) {
            for (Property propertyToAdd : propertiesToAdd) {
                addProperty(propertyToAdd, target);
            }
        }
    }

    /**
     * Add a property to the target collection. If a property with the same key already exists it
     * will be replaced by the new one.
     * 
     * @param propertyToAdd
     *            the property to add
     * @param target
     *            the collection to add the property to
     */
    public static void addProperty(Property propertyToAdd, Collection<Property> target) {
        Iterator<Property> targetIt = target.iterator();
        while (targetIt.hasNext()) {
            Property exisitingProp = targetIt.next();
            if (exisitingProp.getPropertyKey().equals(propertyToAdd.getPropertyKey())) {
                targetIt.remove();
                break;
            }
        }
        target.add(propertyToAdd);
    }
    
    /**
     * Get a property from a collection of properties that has a given key.
     * 
     * @param properties
     *            the properties to check for a matching property
     * @param propertyKey
     *            the key of the property to retrieve
     * @return the found property or null if no property was found or the collection was null
     */
    public static Property getPropertyByKey(Collection<Property> properties, String propertyKey) {
        if (properties != null) {
            for (Property property : properties) {
                if (property.getPropertyKey().equals(propertyKey)) {
                    return property;
                }
            }
        }
        return null;
    }

    private PropertyHelper() {
        // helper class thus no construction
    }
}
