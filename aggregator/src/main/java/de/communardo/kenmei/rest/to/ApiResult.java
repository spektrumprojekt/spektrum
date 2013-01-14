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

package de.communardo.kenmei.rest.to;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * The resulting object of an api call is encapsulated into this class. A status defines if the call
 * has been succeeded or not. The message gives an hint on the error case. The actual result object
 * is stored in {@link #result}.
 * 
 * @author Torsten Lunze
 * @param <T>
 *            the capsuled resource
 */
@JsonSerialize(include = Inclusion.NON_NULL)
public class ApiResult<T> implements Serializable {

    /**
     * The result status.
     * 
     * @author Torsten Lunze
     */
    public enum ResultStatus {
        /** everything has been ok */
        OK,
        /** something went wrong - a warning **/
        WARNING,
        /** an error occured */
        ERROR
    }

    /** Logger. */
    private final static Logger LOG = Logger.getLogger(ApiResult.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String message;
    private String status;
    private T result;

    /**
     * Get a message, mainly used in case of errors
     * 
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the result object
     */
    public T getResult() {
        return result;
    }

    /**
     * @return the state
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param message
     *            an message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @param result
     *            the result object
     */
    public void setResult(T result) {
        this.result = result;
    }

    /**
     * Sets the status (must be of a value defined in {@link ResultStatus}
     * 
     * @param status
     *            status to be set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return null;
    }

    /**
     * Writes the object in json format to the given stream.
     * 
     * @param outputStream
     *            The stream to write the object to.
     * @throws IOException
     *             Exception.
     */
    public void writeToOutputStream(OutputStream outputStream) throws IOException {
        OBJECT_MAPPER.writeValue(outputStream, this);
    }
}
