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

package de.spektrumprojekt.communication;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MessageHandlerCommunicator implements
        Communicator {

    private boolean hasErrors;

    private final static Logger LOGGER = LoggerFactory.getLogger(MessageHandlerCommunicator.class);

    private Map<MessageHandler<? extends CommunicationMessage>, MessageHandler<? extends CommunicationMessage>> messageHandlers =
            new ConcurrentHashMap<MessageHandler<? extends CommunicationMessage>, MessageHandler<? extends CommunicationMessage>>();

    /**
     * Deliever the message to all known handlers, if an exception occurs it will be stored
     * 
     * @param message
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Map<MessageHandler<? extends CommunicationMessage>, Throwable> deliverMessage(
            CommunicationMessage message) {
        Map<MessageHandler<? extends CommunicationMessage>, Throwable> errors = new HashMap<MessageHandler<? extends CommunicationMessage>, Throwable>();

        for (MessageHandler<? extends CommunicationMessage> handler : messageHandlers.keySet()) {

            try {

                if (handler.supports(message)) {

                    // this is not nice. it assumes that supports checks for the type of the
                    // message.

                    MessageHandler hack = handler;
                    hack.deliverMessage(message);

                }

            } catch (Throwable th) {
                LOGGER.error("Error delievering message message=" + message, th);
                errors.put(handler, th);
            }
        }
        hasErrors = hasErrors || errors.size() > 0;
        return errors;
    }

    @Override
    public boolean hasErrors() {
        return hasErrors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends CommunicationMessage> void registerMessageHandler(MessageHandler<T> handler) {

        this.messageHandlers.put(handler, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends CommunicationMessage> void unregisterMessageHandler(MessageHandler<T> handler) {
        this.messageHandlers.remove(handler);
    }

}
