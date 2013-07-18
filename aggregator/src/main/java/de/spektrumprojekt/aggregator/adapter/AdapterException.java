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

package de.spektrumprojekt.aggregator.adapter;

import de.spektrumprojekt.datamodel.subscription.Subscription;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;

/**
 * <p>
 * An exception which might occur while an {@link Adapter} processes a {@link Subscription}.
 * </p>
 * 
 * @author Philipp Katz
 */
public class AdapterException extends Exception {

    private static final long serialVersionUID = 1L;

    private final StatusType statusType;

    /**
     * @param message
     * @param statusType
     */
    public AdapterException(String message, StatusType statusType) {
        super(message);
        this.statusType = statusType;
    }

    /**
     * @param message
     * @param cause
     * @param statusType
     */
    public AdapterException(String message, Throwable cause, StatusType statusType) {
        super(message, cause);
        this.statusType = statusType;
    }

    /**
     * @return the statusType
     */
    public StatusType getStatusType() {
        return statusType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AdapterException [");
        if (statusType != null) {
            builder.append("statusType=");
            builder.append(statusType);
            builder.append(", ");
        }
        if (getMessage() != null) {
            builder.append("getMessage()=");
            builder.append(getMessage());
            builder.append(", ");
        }
        if (getCause() != null) {
            builder.append("getCause()=");
            builder.append(getCause());
        }
        builder.append("]");
        return builder.toString();
    }

}
