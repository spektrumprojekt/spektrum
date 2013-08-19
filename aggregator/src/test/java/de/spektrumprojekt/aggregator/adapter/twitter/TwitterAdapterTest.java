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

package de.spektrumprojekt.aggregator.adapter.twitter;

import static org.junit.Assume.assumeNotNull;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.aggregator.Aggregator;
import de.spektrumprojekt.aggregator.adapter.ConfigurationForT;
import de.spektrumprojekt.aggregator.chain.AggregatorChain;
import de.spektrumprojekt.aggregator.configuration.AggregatorConfiguration;
import de.spektrumprojekt.communication.CommunicationMessage;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.communication.vm.VirtualMachineCommunicator;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.source.Source;
import de.spektrumprojekt.datamodel.source.SourceStatus;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.simple.PersistenceMock;

/**
 * 
 * @author Philipp Katz
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 */
public class TwitterAdapterTest {

    /** The logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterAdapterTest.class);

    private String accessToken;
    private String accessTokenSecret;

    private Aggregator aggregator;
    private AggregatorConfiguration aggregatorConfiguration;
    private AggregatorChain aggregatorChain;
    private Communicator communicator;
    private Persistence persistence = new PersistenceMock();

    @Before
    public void readConfig() throws ConfigurationException {
        aggregatorConfiguration = AggregatorConfiguration.loadXmlConfig();
        assumeNotNull(aggregatorConfiguration);

        communicator = new VirtualMachineCommunicator(
                new LinkedBlockingQueue<CommunicationMessage>(),
                new LinkedBlockingQueue<CommunicationMessage>());

        aggregator = new Aggregator(communicator, persistence, aggregatorConfiguration);

        aggregatorChain = aggregator.getAggregatorChain();

        Configuration config = ConfigurationForT.getInstance().getConfiguration();

        if (config == null) {
            LOGGER.warn("could not load test configuration, skipping this test");
        }
        assumeNotNull(config);
        accessToken = config.getString("twitter.accessToken");
        accessTokenSecret = config.getString("twitter.accessTokenSecret");
        assumeNotNull(accessToken, accessTokenSecret);
    }

    @Test
    public void testTwitterAdapter() throws InterruptedException {

        Source source = new Source(TwitterAdapter.SOURCE_TYPE);
        SourceStatus sourceStatus = new SourceStatus(source);

        source.addAccessParameter(new Property(TwitterAdapter.ACCESS_PARAMETER_TOKEN,
                accessToken));
        source.addAccessParameter(new Property(TwitterAdapter.ACCESS_PARAMETER_TOKEN_SECRET,
                accessTokenSecret));

        TwitterAdapter twitterAdapter = new TwitterAdapter(aggregatorChain,
                aggregatorConfiguration);
        twitterAdapter.addSource(sourceStatus);
        Thread.sleep(10 * 1000);
        // System.out.println(twitterAdapter.getMessageQueue());
    }
}
