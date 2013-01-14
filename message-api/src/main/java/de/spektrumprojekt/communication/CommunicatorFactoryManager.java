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

import java.util.Collection;
import java.util.HashSet;

import de.spektrumprojekt.communication.vm.VirtualMachineCommunicatorFactory;

public class CommunicatorFactoryManager {

    private final Collection<CommunicatorFactory> factories = new HashSet<CommunicatorFactory>();

    public CommunicatorFactoryManager() {
        this.factories.add(new VirtualMachineCommunicatorFactory());
        // this.factories.add(new XmppCommunicatorFactory());
    }

    public CommunicatorFactory getCommunicatorFactory(String url)
            throws CommunicatorFactoryException {
        if (url == null) {
            throw new IllegalArgumentException("url cannot be null!");
        }
        url = url.trim();
        for (CommunicatorFactory factory : factories) {
            if (url.startsWith(factory.getURLHandler())) {
                return factory;
            }
        }
        throw new CommunicatorFactoryException("No Communicator Factory found for url=" + url);
    }

    public void registerFactory(CommunicatorFactory factory) {
        this.factories.add(factory);
    }
}
