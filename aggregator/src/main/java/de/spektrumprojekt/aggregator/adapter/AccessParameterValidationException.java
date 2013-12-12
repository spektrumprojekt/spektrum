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

/**
 * Exception to be thrown by an adapter when an access parameter is not valid.
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class AccessParameterValidationException extends Exception {

    /**
     * default serial version UID
     */
    private static final long serialVersionUID = 1L;

    private final String parameterKey;

    /**
     * create a new exception
     * 
     * @param parameterKey
     *            the key of the parameter whose validation failed
     * @param message
     *            the detail message
     */
    public AccessParameterValidationException(String parameterKey, String message) {
        super(message);
        this.parameterKey = parameterKey;
    }

    /**
     * create a new exception
     * 
     * @param parameterKey
     *            the key of the parameter whose validation failed
     * @param message
     *            the detail message
     * @param cause
     *            the cause of the exception
     */
    public AccessParameterValidationException(String parameterKey, String message, Throwable cause) {
        super(message, cause);
        this.parameterKey = parameterKey;
    }

    /**
     * @return the key of the parameter whose validation failed
     */
    public String getParameterKey() {
        return parameterKey;
    }

}
