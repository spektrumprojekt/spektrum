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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.Validate;

import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.identifiable.Identifiable;
import de.spektrumprojekt.datamodel.subscription.Subscription;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;

/**
 * <p>
 * This class describes additional meta data for aggregation like error types and counts encountered
 * for a specific {@link Subscription}. This includes specific information for error handling and
 * tracking, and information necessary for determining which items are new.
 * </p>
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * @author Philipp Katz
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "globalId"))
public class SourceStatus extends Identifiable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @OneToOne(cascade = {}, optional = false)
    private Source source;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastSuccessfulCheck;

    /** Counter for the successful checks performed for this subscription. */
    private Integer successfulCheckCount;

    /** Counter for errors encountered when checking this subscription. */
    private Integer errorCount;

    /**
     * Counter for consecutive (i.e. number of sequential) errors when checking this subscription.
     * This is reset, when a successful check is performed.
     */
    private Integer consecutiveErrorCount;

    /** The status type for the last check. */
    private StatusType lastStatusType;

    /** The time, when the last error occured. */
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastError;

    /** Flag to indicate whether to exclude this subscription from being checked by the aggregator. */
    private boolean blocked;

    /** The timestamp of the most recent item in the source at the last poll. */
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastContentTimestamp;

    /** The hash of the most recent item in the source at the last poll. */
    private String lastContentHash;

    /** The last message occurred while accessing the source */
    private String lastAccessMessage;

    /** Properties for additional information */
    @OneToMany(cascade = CascadeType.ALL)
    private final Collection<Property> properties = new HashSet<Property>();

    /** Constructor for ORM layer. */
    protected SourceStatus() {

    }

    /**
     * <p>
     * Initialize a new {@link SourceStatus} for the specified {@link Subscription}, with all fields
     * set to initial values.
     * </p>
     * 
     * @param source
     *            The subscription for which to create the {@link SourceStatus}, no
     *            <code>null</code>.
     */
    public SourceStatus(Source source) {
        this.source = source;

        this.successfulCheckCount = 0;
        this.errorCount = 0;
        this.consecutiveErrorCount = 0;
        this.lastStatusType = null;
        this.lastError = null;
        this.blocked = false;
        this.lastContentTimestamp = null;
        this.lastContentHash = null;
    }

    /**
     * @param e
     *            Property
     * @return the old value of the property, null if it did not exist
     */
    public Property addProperty(Property e) {
        Property oldValue = null;
        for (Property property : properties) {
            if (property.getPropertyKey().equals(e.getPropertyKey())) {
                oldValue = property;
            }
        }
        if (oldValue != null) {
            properties.remove(oldValue);
        }
        properties.add(e);
        return oldValue;
    }

    /**
     * @return the consecutiveErrorCount
     */
    public Integer getConsecutiveErrorCount() {
        return consecutiveErrorCount;
    }

    /**
     * @return the errorCount
     */
    public Integer getErrorCount() {
        return errorCount;
    }

    public String getLastAccessMessage() {
        return lastAccessMessage;
    }

    /**
     * @return the lastContentHash
     */
    public String getLastContentHash() {
        return lastContentHash;
    }

    /**
     * @return the lastContentTimestamp
     */
    public Date getLastContentTimestamp() {
        return lastContentTimestamp;
    }

    /**
     * @return the lastError
     */
    public Date getLastError() {
        return lastError;
    }

    /**
     * @return the lastStatusType
     */
    public StatusType getLastStatusType() {
        return lastStatusType;
    }

    public Date getLastSuccessfulCheck() {
        return lastSuccessfulCheck;
    }

    /**
     * @return the last date when tried to access the subscription
     */
    public Date getLatestCheckDate() {
        return lastSuccessfulCheck.after(lastError) ? lastSuccessfulCheck : lastError;
    }

    public Collection<Property> getProperties() {
        return Collections.unmodifiableCollection(properties);
    }

    /**
     * 
     * @param key
     *            propertykey
     * @return property
     */
    public Property getProperty(String key) {
        for (Property property : properties) {
            if (property.getPropertyKey().equals(key)) {
                return property;
            }
        }
        return null;
    }

    public Source getSource() {
        return source;
    }

    /**
     * @return the successfulCheckCount
     */
    public Integer getSuccessfulCheckCount() {
        return successfulCheckCount;
    }

    /**
     * @return the blocked
     */
    public boolean isBlocked() {
        return blocked;
    }

    public boolean remove(Object o) {
        return properties.remove(o);
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public void setLastAccessMessage(String lastAccessMessage) {
        this.lastAccessMessage = lastAccessMessage;
    }

    /**
     * @param lastContentHash
     *            the lastContentHash to set
     */
    public void setLastContentHash(String lastContentHash) {
        this.lastContentHash = lastContentHash;
    }

    /**
     * @param lastContentTimestamp
     *            the lastContentTimestamp to set
     */
    public void setLastContentTimestamp(Date lastContentTimestamp) {
        this.lastContentTimestamp = lastContentTimestamp;
    }

    public void setLastSuccessfulCheck(Date lastSuccessfulCheck) {
        this.lastSuccessfulCheck = lastSuccessfulCheck;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SourceStatus [source=");
        builder.append(source);
        builder.append(", lastSuccessfulCheck=");
        builder.append(lastSuccessfulCheck);
        builder.append(", successfulCheckCount=");
        builder.append(successfulCheckCount);
        builder.append(", errorCount=");
        builder.append(errorCount);
        builder.append(", consecutiveErrorCount=");
        builder.append(consecutiveErrorCount);
        builder.append(", lastStatusType=");
        builder.append(lastStatusType);
        builder.append(", lastError=");
        builder.append(lastError);
        builder.append(", blocked=");
        builder.append(blocked);
        builder.append(", lastContentTimestamp=");
        builder.append(lastContentTimestamp);
        builder.append(", lastContentHash=");
        builder.append(lastContentHash);
        builder.append(", lastAccessMessage=");
        builder.append(lastAccessMessage);
        builder.append("]");
        return builder.toString();
    }

    /**
     * <p>
     * Updates the status based on the {@link StatusType} of a check.
     * </p>
     * 
     * @param statusType
     *            The status type of the last check, not <code>null</code>.
     */
    public void updateCheck(StatusType statusType) {
        Validate.notNull(statusType, "statusType must not be null");
        if (statusType == StatusType.OK) {
            successfulCheckCount++;
            consecutiveErrorCount = 0;
            lastError = null;
            lastAccessMessage = null;
            setLastSuccessfulCheck(new Date());
        } else {
            errorCount++;
            consecutiveErrorCount++;
            lastError = new Date();
        }
        lastStatusType = statusType;
    }

    /**
     * <p>
     * Updates this {@link SourceStatus} after a poll.
     * </p>
     * 
     * @param statusType
     *            The status type of the last check, not <code>null</code>.
     * @param lastContentTimestamp
     *            The timestamp of the most recent content item in the poll.
     * @param lastContentHash
     *            The hash of the most recent content item in the poll.
     */
    public void updateCheck(StatusType statusType, Date lastContentTimestamp, String lastContentHash) {
        Validate.notNull(statusType, "statusType must not be null");
        if (statusType == StatusType.OK) {
            this.successfulCheckCount++;
            this.consecutiveErrorCount = 0;
            this.lastError = null;
            setLastSuccessfulCheck(new Date());
        } else {
            this.errorCount++;
            this.consecutiveErrorCount++;
            this.lastError = new Date();
        }
        this.lastStatusType = statusType;
        this.lastContentTimestamp = lastContentTimestamp;
        this.lastContentHash = lastContentHash;
    }

}
