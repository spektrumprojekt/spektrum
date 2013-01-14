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

package de.spektrumprojekt.datamodel.subscription;

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
import de.spektrumprojekt.datamodel.subscription.filter.FilterExpression;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "globalId"))
public class Subscription extends Identifiable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Example: RSS, Twitter, Communote; defines the associated connector.
     */
    private String connectorType;

    // private String serializedFilterExpression;

    /**
     * 
     * It is transient since it is (de)serialized in {@link #getSerializedFilterExpression()} and
     * {@link #setFilterExpression(FilterExpression)}.
     * 
     */
    private transient FilterExpression filterExpression;

    @OneToMany(cascade = CascadeType.ALL)
    private final Collection<Property> accessParameters = new HashSet<Property>();

    protected Subscription() {
    }

    public Subscription(String connectorType) {
        this.connectorType = connectorType;
    }

    public Subscription(String globalId, String connectorType) {
        super(globalId);
        this.connectorType = connectorType;
    }

    /**
     * <p>
     * Copy constructor for creating a shallow copy of the specified {@link Subscription}.
     * </p>
     * 
     * 
     * @param subscription
     *            The subscription for which to create a copy, not <code>null</code>.
     */
    public Subscription(Subscription subscription) {
        super(subscription.getGlobalId());

        this.connectorType = subscription.getConnectorType();
        this.accessParameters.addAll(subscription.getAccessParameters());

        // if (subscription.getFilterExpression() != null) {
        // this.setSerializedFilterExpression(subscription.getSerializedFilterExpression());
        // }
    }

    public void addAccessParameter(Property prop) {
        this.accessParameters.add(prop);
    }

    // private FilterExpression deserialze(String serializedFilterExpression) {
    // // TODO serialize the filter expression into JSON or XML or something else
    // throw new UnsupportedOperationException("Not yet implemented.");
    // }

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

    public FilterExpression getFilterExpression() {
        return filterExpression;
    }

    // public String getSerializedFilterExpression() {
    // serializedFilterExpression = serialize(this.filterExpression);
    // return serializedFilterExpression;
    // }

    // private String serialize(FilterExpression filterExpression) {
    // // TODO serialize the filter expression into JSON or XML or something else
    // throw new UnsupportedOperationException("Not yet implemented.");
    // }

    public void setFilterExpression(FilterExpression filterExpression) {
        this.filterExpression = filterExpression;
    }

    // public void setSerializedFilterExpression(String serializedFilterExpression) {
    // this.serializedFilterExpression = serializedFilterExpression;
    // this.filterExpression = deserialze(this.serializedFilterExpression);
    // }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Subscription [connectorType=");
        builder.append(connectorType);
        // builder.append(", serializedFilterExpression=");
        // builder.append(serializedFilterExpression);
        builder.append(", filterExpression=");
        builder.append(filterExpression);
        builder.append(", accessParameters=");
        builder.append(accessParameters);
        builder.append(", getGlobalId()=");
        builder.append(getGlobalId());
        builder.append(", getId()=");
        builder.append(getId());
        builder.append("]");
        return builder.toString();
    }

}
