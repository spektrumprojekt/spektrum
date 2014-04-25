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
import de.spektrumprojekt.i.ranker.MessageFeatureContext;
import de.spektrumprojekt.i.similarity.user.IterativeUserSimilarityComputer;

/**
 * A command to invoke an {@link IterativeUserSimilarityComputer}
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class UserSimilarityIntegrationCommand implements Command<MessageFeatureContext> {

    private final IterativeUserSimilarityComputer userSimilarityComputer;

    /**
     * 
     * @param persistence
     *            the persistence to use
     */
    public UserSimilarityIntegrationCommand(IterativeUserSimilarityComputer userSimilarityComputer) {
        this.userSimilarityComputer = userSimilarityComputer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " userSimilarityComputer="
                + userSimilarityComputer.getConfigurationDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(MessageFeatureContext context) {
        if (context.isNoRankingOnlyLearning()) {
            return;
        }
        this.userSimilarityComputer.runForMessage(context.getMessage());
    }

}
