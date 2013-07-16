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

package de.spektrumprojekt.commons.chain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The command chain is itself a command. This allows to encapsulate a sub-chain into a process
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 * @param <T>
 *            the type of context
 */
public class CommandChain<T extends Object> implements Command<T> {

    private final List<Command<T>> commands = new ArrayList<Command<T>>();

    /**
     * 
     * @param command
     *            add the command to the end of the chain, the order of adding defines the order of
     *            executing the commands
     */
    public void addCommand(Command<T> command) {
        this.commands.add(command);
    }

    public List<Command<T>> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        String config = this.getClass().getSimpleName() + ":";
        for (Command<T> command : commands) {
            config += " " + command.getConfigurationDescription();
        }
        return config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(T context) {

        commands: for (Command<T> command : commands) {
            try {

                command.process(context);

            } catch (CommandException ce) {

                if (!ce.isContinueChain()) {
                    break commands;
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CommandChain [commands=");
        builder.append(commands);
        builder.append("]");
        return builder.toString();
    }

}
