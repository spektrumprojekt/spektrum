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

package de.spektrumprojekt.datamodel.source;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.identifiable.Identifiable;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "globalId"))
public class Source extends Identifiable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Example: RSS, Twitter, Communote; defines the associated connector.
     */
    private String connectorType;

    @OneToMany(cascade = CascadeType.ALL)
    private final Collection<Property> accessParameters = new HashSet<Property>();

    protected Source() {
    }

    public Source(String connectorType) {
        this.connectorType = connectorType;
    }

    public Source(String globalId, String connectorType) {
        super(globalId);
        this.connectorType = connectorType;
    }

    public Property addAccessParameter(final Property prop) {
        Property oldValue = null;
        for (Property property : this.accessParameters) {
            if (property.getPropertyKey().equals(prop.getPropertyKey())) {
                oldValue = property;
                break;
            }
        }
        if (oldValue != null) {
            this.accessParameters.remove(oldValue);
        }
        this.accessParameters.add(prop);
        return oldValue;
    }

    public Property getAccessParameter(String propertyKey) {
        for (Property parameter : accessParameters) {
            if (parameter.getPropertyKey().equals(propertyKey)) {
                return parameter;
            }
        }
        return null;
    }

    public Collection<Property> getAccessParameters() {
        return accessParameters;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public boolean removeAccessParameter(String propertyKey) {
        Property prop = this.getAccessParameter(propertyKey);
        if (prop == null) {
            return this.accessParameters.remove(prop);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Source [connectorType=");
        builder.append(connectorType);
        builder.append(", accessParameters=");
        builder.append(accessParameters);
        builder.append("]");
        return builder.toString();
    }

}
