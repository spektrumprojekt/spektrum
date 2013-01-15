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

package de.spektrumprojekt.i.learner;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.commons.chain.CommandChain;
import de.spektrumprojekt.communication.CommunicationMessage;
import de.spektrumprojekt.communication.MessageHandler;
import de.spektrumprojekt.configuration.ConfigurationDescriptable;
import de.spektrumprojekt.i.informationextraction.InformationExtractionCommand;
import de.spektrumprojekt.i.learner.chain.UserModelLearnerCommand;
import de.spektrumprojekt.i.ranker.Ranker;
import de.spektrumprojekt.persistence.Persistence;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class Learner implements MessageHandler<LearningMessage>, ConfigurationDescriptable {

    private CommandChain<LearnerMessageContext> learnerChain;

    private final Persistence persistence;

    private final static Logger LOGGER = LoggerFactory.getLogger(Ranker.class);

    /**
     * 
     * @param persistence
     *            the persistence
     * @param userModelEntryIntegrationStrategy
     *            the strategy to integrate the user model
     */
    public Learner(Persistence persistence,
            UserModelEntryIntegrationStrategy userModelEntryIntegrationStrategy) {
        if (persistence == null) {
            throw new IllegalArgumentException("persistence cannot be null!");
        }
        if (userModelEntryIntegrationStrategy == null) {
            throw new IllegalArgumentException("userModelEntryIntegrationStrategy cannot be null!");
        }
        this.persistence = persistence;

        this.learnerChain = new CommandChain<LearnerMessageContext>();

        // TODO this should use flags and it should be the same as the ranker
        InformationExtractionCommand<LearnerMessageContext> ieCommand = InformationExtractionCommand
                .createDefaultGermanEnglish(persistence, false, true);

        this.learnerChain.addCommand(ieCommand);
        this.learnerChain.addCommand(new UserModelLearnerCommand(this.persistence,
                userModelEntryIntegrationStrategy));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deliverMessage(LearningMessage learningMessage) {

        StopWatch stopWatch = new StopWatch();

        stopWatch.start();

        LearnerMessageContext context = new LearnerMessageContext(persistence,
                learningMessage.getMessage(), learningMessage.getMessageRelation(),
                learningMessage.getUserToLearnForGlobalId(), learningMessage.getInterest());

        this.learnerChain.process(context);

        stopWatch.stop();

        LOGGER.trace("Learner processed message {} in {} ms", context.getMessage().getGlobalId(),
                stopWatch.getTime());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getConfigurationDescription() {
        return this.getClass().getSimpleName() + " learnerChain: "
                + this.learnerChain.getConfigurationDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<LearningMessage> getMessageClass() {
        return LearningMessage.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(CommunicationMessage message) {
        return message instanceof CommunicationMessage;
    }

}