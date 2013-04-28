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

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.message.InteractionLevel;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;
import de.spektrumprojekt.i.ranker.chain.features.Feature;

/**
 * Command for computing the message rank out of the features
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class DetermineInteractionLevelCommand implements Command<UserSpecificMessageFeatureContext> {

    public DetermineInteractionLevelCommand() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(UserSpecificMessageFeatureContext context) {

        InteractionLevel interactionLevel = InteractionLevel.NONE;

        if (context.check(Feature.AUTHOR_FEATURE, 1)) {
            interactionLevel = InteractionLevel.DIRECT;

        } else if (context.check(Feature.MENTION_FEATURE, 1)) {
            interactionLevel = InteractionLevel.DIRECT;

        } else if (context.check(Feature.LIKE_FEATURE, 1)) {
            interactionLevel = InteractionLevel.DIRECT;

        } else if (context.check(Feature.DISCUSSION_PARTICIPATION_FEATURE, 1)) {
            interactionLevel = InteractionLevel.INDIRECT;

        } else if (context.check(Feature.DISCUSSION_MENTION_FEATURE, 1)) {
            interactionLevel = InteractionLevel.INDIRECT;

        }

        context.setInteractionLevel(interactionLevel);

    }
}
