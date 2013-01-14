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

public class FilterExpressionConnector extends Identifiable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private FilterExpression filterExpression = null;

    private BooleanOperator booleanOperator = BooleanOperator.AND;

    public BooleanOperator getBooleanOperator() {
        return booleanOperator;
    }

    public FilterExpression getFilterExpression() {
        return filterExpression;
    }

    public void setBooleanOperator(BooleanOperator newBooleanOperator) {
        booleanOperator = newBooleanOperator;
    }

    public void setFilterExpression(FilterExpression newFilterExpression) {
        filterExpression = newFilterExpression;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FilterExpressionConnector [filterExpression=");
        builder.append(filterExpression);
        builder.append(", booleanOperator=");
        builder.append(booleanOperator);
        builder.append(", getGlobalId()=");
        builder.append(getGlobalId());
        builder.append(", getId()=");
        builder.append(getId());
        builder.append("]");
        return builder.toString();
    }

}
