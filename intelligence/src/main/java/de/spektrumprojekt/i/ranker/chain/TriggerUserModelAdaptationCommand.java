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
import de.spektrumprojekt.datamodel.message.MessageRank;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.ranker.UserSpecificMessageFeatureContext;

/**
 * Command for computing the message rank out of the features
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class TriggerUserModelAdaptationCommand implements
        Command<UserSpecificMessageFeatureContext> {

    private float rankThreshold = 0.75f; // => interest term?
    private float confidenceThreshold = 0.5f;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " rankThreshold=" + rankThreshold
                + " confidenceThreshold=" + confidenceThreshold;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(UserSpecificMessageFeatureContext context) {

        MessageRank messageRank = context.getMessageRank();

        if (messageRank.getRank() < rankThreshold) {
            if (context.getMatchingUserModelEntries() != null) {
                int messageTerms = MessageHelper.getAllTerms(context.getMessage()).size();
                int matchingUserModelTerms = context.getMatchingUserModelEntries().size();

                float confidence = messageTerms == 1 ? 0 : (float) matchingUserModelTerms
                        / messageTerms;

                if (confidence < confidenceThreshold) {
                    // TODO trigger adaptation
                }

            }
        }

    }
}
