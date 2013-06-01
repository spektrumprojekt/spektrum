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

package de.spektrumprojekt.i.ranker.chain;

import de.spektrumprojekt.callbacks.MessageGroupMemberRunner;
import de.spektrumprojekt.callbacks.UserCallback;
import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.commons.chain.CommandChain;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;

/**
 * Command that contains a chain to compute the user specific features
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class UserFeatureCommand implements Command<MessageFeatureContext>,
        UserCallback<MessageFeatureContext> {

    private final CommandChain<UserSpecificMessageFeatureContext> userSpecificCommandChain =
            new CommandChain<UserSpecificMessageFeatureContext>();

    private final MessageGroupMemberRunner<MessageFeatureContext> messageGroupMemberRunner;

    /**
     * 
     * @param runner
     *            defines how to resolve users for a message group
     */
    public UserFeatureCommand(MessageGroupMemberRunner<MessageFeatureContext> runner) {
        if (runner == null) {
            throw new IllegalArgumentException("runner cannot be null.");
        }
        this.messageGroupMemberRunner = runner;
    }

    public void addCommand(Command<UserSpecificMessageFeatureContext> command) {
        this.userSpecificCommandChain.addCommand(command);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " userSpecificCommandChain: "
                + userSpecificCommandChain.getConfigurationDescription();
    }

    /**
     * 
     * @return the chain
     */
    public CommandChain<UserSpecificMessageFeatureContext> getUserSpecificCommandChain() {
        return userSpecificCommandChain;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(MessageFeatureContext context) {

        messageGroupMemberRunner.runForMessageGroup(context,
                context.getMessage().getMessageGroup(), this);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(MessageFeatureContext context, String userGlobalId) {

        if (context.getUserGlobalIdsToProcess() != null
                && !context.getUserGlobalIdsToProcess().isEmpty()
                && !context.getUserGlobalIdsToProcess().contains(userGlobalId)) {
            return;
        }

        UserSpecificMessageFeatureContext userContext = new UserSpecificMessageFeatureContext(
                userGlobalId, context);
        userSpecificCommandChain.process(userContext);
        context.addUserContext(userContext);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserFeatureCommand [userSpecificCommandChain=");
        builder.append(userSpecificCommandChain);
        builder.append(", messageGroupMemberRunner=");
        builder.append(messageGroupMemberRunner);
        builder.append("]");
        return builder.toString();
    }

}
