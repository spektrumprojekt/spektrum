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
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.identifiable.Identifiable;
import de.spektrumprojekt.datamodel.source.Source;
import de.spektrumprojekt.datamodel.subscription.filter.FilterExpression;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * @author Philipp Katz
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "globalId"))
public class Subscription extends Identifiable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @ManyToOne(cascade = { CascadeType.PERSIST }, fetch = FetchType.EAGER, optional = false)
    private Source source;

    private boolean suspended;

    private transient Long lastProcessedMessagedId;
    // private String serializedFilterExpression;

    /**
     * 
     * It is transient since it is (de)serialized in {@link #getSerializedFilterExpression()} and
     * {@link #setFilterExpression(FilterExpression)}.
     * 
     */
    private transient FilterExpression filterExpression;

    @OneToMany(cascade = CascadeType.ALL)
    private final Collection<Property> subscriptionParameters = new HashSet<Property>();

    protected Subscription() {
    }

    public Subscription(Source source) {
        if (source == null) {
            throw new IllegalArgumentException("source cannot be null.");
        }
        this.source = source;
    }

    public Subscription(String globalId, Source source) {
        super(globalId);
        if (source == null) {
            throw new IllegalArgumentException("source cannot be null.");
        }
        this.source = source;
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

        this.source = subscription.getSource();
        this.subscriptionParameters.addAll(subscription.getSubscriptionParameters());

        // if (subscription.getFilterExpression() != null) {
        // this.setSerializedFilterExpression(subscription.getSerializedFilterExpression());
        // }
    }

    public void addSubscriptionParameter(Property prop) {
        this.subscriptionParameters.add(prop);
    }

    public FilterExpression getFilterExpression() {
        return filterExpression;
    }

    public Long getLastProcessedMessagedId() {
        return lastProcessedMessagedId;
    }

    public Source getSource() {
        return source;
    }

    public Property getSubscriptionParameter(String propertyKey) {
        for (Property parameter : subscriptionParameters) {
            if (parameter.getPropertyKey().equals(propertyKey)) {
                return parameter;
            }
        }
        return null;
    }

    public Collection<Property> getSubscriptionParameters() {
        return subscriptionParameters;
    }

    /**
     * 
     * @return true if the subscription is suspended and new message will not be forwarded
     */
    public boolean isSuspended() {
        return suspended;
    }

    // private FilterExpression deserialze(String serializedFilterExpression) {
    // // TODO serialize the filter expression into JSON or XML or something else
    // throw new UnsupportedOperationException("Not yet implemented.");
    // }

    public void setFilterExpression(FilterExpression filterExpression) {
        this.filterExpression = filterExpression;
    }

    public void setLastProcessedMessagedId(Long lastProcessedMessagedId) {
        this.lastProcessedMessagedId = lastProcessedMessagedId;
    }

    public void setSource(Source source) {
        if (source == null) {
            throw new IllegalArgumentException("source cannot be null.");
        }
        this.source = source;
    }

    // public String getSerializedFilterExpression() {
    // serializedFilterExpression = serialize(this.filterExpression);
    // return serializedFilterExpression;
    // }

    // private String serialize(FilterExpression filterExpression) {
    // // TODO serialize the filter expression into JSON or XML or something else
    // throw new UnsupportedOperationException("Not yet implemented.");
    // }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    // public void setSerializedFilterExpression(String serializedFilterExpression) {
    // this.serializedFilterExpression = serializedFilterExpression;
    // this.filterExpression = deserialze(this.serializedFilterExpression);
    // }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Subscription [source=");
        builder.append(source);
        builder.append(", lastProcessedMessagedId=");
        builder.append(lastProcessedMessagedId);
        builder.append(", subscriptionParameters=");
        builder.append(subscriptionParameters);
        builder.append("]");
        return builder.toString();
    }

}
