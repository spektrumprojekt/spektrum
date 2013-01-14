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

package de.spektrumprojekt.datamodel.subscription.status;

import de.spektrumprojekt.datamodel.subscription.Subscription;

/**
 * <p>
 * Different status results for {@link Subscription}s polled by the Aggregator.
 * </p>
 * 
 * @author Philipp Katz
 */
public enum StatusType {

    /** The subscription was checked or added successfully. */
    OK,

    /**
     * The Aggregator provides no suitable adapter for the source type in the specified
     * subscription.
     */
    ERROR_NO_ADAPTER,

    /** A network error occurred while checking the subscription. */
    ERROR_NETWORK,

    /** An authentication error occurred while checking the subscription. */
    ERROR_AUTHENTICATION,

    /** The subscription data did not contain all necessary information for adding the subscription. */
    ERROR_INSUFFICIENT_DATA,

    /** The subscription contained invalid data (e.g. an invalid URL). */
    ERROR_INVALID_DATA,

    /** An error occurred while processing or parsing the content of the subscription. */
    ERROR_PROCESSING_CONTENT,

    /** An adapter internal error occurred. */
    ERROR_INTERNAL_ADAPTER,

    /** error in the ssl communication */
    ERROR_SSL,

    /** An error occurred which could not be specified any further. */
    ERROR_UNSPECIFIED

}
