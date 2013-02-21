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

package de.spektrumprojekt.i.ranker.chain.features;

import de.spektrumprojekt.commons.chain.Command;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.helper.MessageHelper;
import de.spektrumprojekt.i.datamodel.MessageFeature;
import de.spektrumprojekt.i.ranker.MessageFeatureContext;

/**
 * Compute if the message is the root of the discussion or not
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class DiscussionRootFeatureCommand implements Command<MessageFeatureContext> {
    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName();
    }

    /**
     * 
     * @return the feature id
     */
    public Feature getFeatureId() {
        return Feature.DISCUSSION_ROOT_FEATURE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(MessageFeatureContext context) {
        MessageFeature feature = new MessageFeature(getFeatureId());

        Property parentId = MessageHelper.getParentMessage(context.getMessage());
        if (parentId == null || parentId.getPropertyValue() == null) {

            // it is a root message
            feature.setValue(1f);

        }

        context.addMessageFeature(feature);
    }
}
