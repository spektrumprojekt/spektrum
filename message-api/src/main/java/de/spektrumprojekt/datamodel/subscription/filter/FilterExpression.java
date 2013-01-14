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

package de.spektrumprojekt.datamodel.subscription.filter;

import de.spektrumprojekt.datamodel.identifiable.Identifiable;

public class FilterExpression extends Identifiable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String messageAttribute = null;

    private boolean negated = false;

    private FilterExpressionConnector filterExpressionConnector = null;

    public FilterExpressionConnector getFilterExpressionConnector() {
        return filterExpressionConnector;
    }

    public String getMessageAttribute() {
        return messageAttribute;
    }

    public boolean isNegated() {
        return negated;
    }

    public void setFilterExpressionConnector(FilterExpressionConnector newFilterExpressionConnector) {
        filterExpressionConnector = newFilterExpressionConnector;
    }

    public void setMessageAttribute(String newMessageAttribute) {
        messageAttribute = newMessageAttribute;
    }

    public void setNegated(boolean newNegated) {
        negated = newNegated;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FilterExpression [messageAttribute=");
        builder.append(messageAttribute);
        builder.append(", negated=");
        builder.append(negated);
        builder.append(", filterExpressionConnector=");
        builder.append(filterExpressionConnector);
        builder.append(", getGlobalId()=");
        builder.append(getGlobalId());
        builder.append(", getId()=");
        builder.append(getId());
        builder.append("]");
        return builder.toString();
    }

}
