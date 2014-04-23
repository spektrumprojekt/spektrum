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

package de.spektrumprojekt.aggregator.adapter.rss;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.aggregator.Aggregator;
import de.spektrumprojekt.aggregator.adapter.AdapterException;
import de.spektrumprojekt.aggregator.adapter.ConfigurationForT;
import de.spektrumprojekt.aggregator.chain.AggregatorChain;
import de.spektrumprojekt.aggregator.configuration.AggregatorConfiguration;
import de.spektrumprojekt.commons.encryption.EncryptionException;
import de.spektrumprojekt.commons.encryption.EncryptionUtils;
import de.spektrumprojekt.communication.CommunicationMessage;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.communication.vm.VirtualMachineCommunicator;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.source.Source;
import de.spektrumprojekt.datamodel.source.SourceStatus;
import de.spektrumprojekt.persistence.Persistence;
import de.spektrumprojekt.persistence.simple.PersistenceMock;

/**
 * <p>
 * Tests for {@link FeedAdapter}.
 * </p>
 * 
 * @author Philipp Katz
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 */
public class FeedAdapterTest {

    /** The logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(FeedAdapterTest.class);

    private Communicator communicator;
    private final Persistence persistence = new PersistenceMock();

    private Aggregator aggregator;
    private AggregatorConfiguration aggregatorConfiguration;
    private AggregatorChain aggregatorChain;

    @Before
    public void readConfig() throws ConfigurationException {
        aggregatorConfiguration = AggregatorConfiguration.loadXmlConfig();
        assumeNotNull(aggregatorConfiguration);

        communicator = new VirtualMachineCommunicator(
                new LinkedBlockingQueue<CommunicationMessage>(),
                new LinkedBlockingQueue<CommunicationMessage>());

        aggregator = new Aggregator(communicator, persistence, aggregatorConfiguration);

        aggregatorChain = aggregator.getAggregatorChain();
    }

    @Test
    public void testCommunardoWiki() throws EncryptionException, AdapterException {
        Configuration config = ConfigurationForT.getInstance().getConfiguration();
        if (config == null) {
            LOGGER.warn("could not load test configuration, skipping this test");
        }
        Assume.assumeNotNull(config);

        // TODO use public available feed for test
        String url = "";

        String login = config.getString("feed.user");
        String password = EncryptionUtils.encrypt(config.getString("feed.password"));

        Source source = new Source(FeedAdapter.SOURCE_TYPE);
        SourceStatus sourceStatus = new SourceStatus(source);

        source.addAccessParameter(new Property(FeedAdapter.ACCESS_PARAMETER_URI, url));
        source.addAccessParameter(new Property(FeedAdapter.ACCESS_PARAMETER_CREDENTIALS_LOGIN,
                login));
        source.addAccessParameter(new Property(FeedAdapter.ACCESS_PARAMETER_CREDENTIALS_PASSWORD,
                password));

        FeedAdapter adapter = new FeedAdapter(aggregatorChain, aggregatorConfiguration);
        List<Message> messages = adapter.poll(sourceStatus);
        assertTrue(messages.size() > 0);
        assertTrue(messages.get(0).getMessageType().equals(MessageType.CONTENT));
    }

    private void testFeed(String feedUrl) throws AdapterException {
        Source source = new Source(FeedAdapter.SOURCE_TYPE);
        SourceStatus sourceStatus = new SourceStatus(source);

        source.addAccessParameter(new Property(FeedAdapter.ACCESS_PARAMETER_URI, feedUrl));
        FeedAdapter feedAdapter = new FeedAdapter(aggregatorChain, aggregatorConfiguration);
        List<Message> messages = feedAdapter.poll(sourceStatus);
        assertTrue(messages.size() > 0);
        assertTrue(messages.get(0) instanceof Message);
        assertNotNull(sourceStatus.getLastContentTimestamp());

        // this is dirty, and might fail.
        messages = feedAdapter.poll(sourceStatus);
        assertTrue(messages.size() == 0);
    }

    @Test
    public void testFeedFeedBurner() throws AdapterException {

        testFeed("http://feeds2.feedburner.com/stadt-bremerhaven/dqXM");
    }

    @Test
    public void testFeedNetzwertig() throws AdapterException {

        testFeed("http://netzwertig.com/feed/");
    }

    @Test(expected = AdapterException.class)
    public void testFeedUnauthorized() throws EncryptionException, AdapterException {

        // TODO use public available feed for test
        String url = "";

        Source source = new Source(FeedAdapter.SOURCE_TYPE);
        SourceStatus sourceStatus = new SourceStatus(source);

        source.addAccessParameter(new Property(FeedAdapter.ACCESS_PARAMETER_URI, url));
        source.addAccessParameter(new Property(FeedAdapter.ACCESS_PARAMETER_CREDENTIALS_LOGIN,
                "wrong"));
        source.addAccessParameter(new Property(FeedAdapter.ACCESS_PARAMETER_CREDENTIALS_PASSWORD,
                EncryptionUtils.encrypt("abcdefghijklmnopqrstuvwxyz")));
        FeedAdapter feedAdapter = new FeedAdapter(aggregatorChain, aggregatorConfiguration);
        List<Message> messages = feedAdapter.poll(sourceStatus);
        assertEquals(1, messages.size());
        assertTrue(messages.get(0).getMessageType().equals(MessageType.ERROR));

    }

    @Test
    public void testHeiseFeed() throws AdapterException {

        testFeed("http://www.heise.de/newsticker/heise-atom.xml");
    }

    @Test
    public void testManuellFeed() throws AdapterException, EncryptionException {
        String url = "";
        String login = "";
        String realPassword = "";
        String encryptedPassword = "";

        if (StringUtils.isBlank(url)) {
            return;
        }

        if (encryptedPassword == null) {
            encryptedPassword = EncryptionUtils.encrypt(realPassword, "123456");
        }

        Source source = new Source(FeedAdapter.SOURCE_TYPE);
        SourceStatus sourceStatus = new SourceStatus(source);

        source.addAccessParameter(new Property(FeedAdapter.ACCESS_PARAMETER_URI, url));
        source.addAccessParameter(new Property(FeedAdapter.ACCESS_PARAMETER_CREDENTIALS_LOGIN,
                login));
        source.addAccessParameter(new Property(FeedAdapter.ACCESS_PARAMETER_CREDENTIALS_PASSWORD,
                encryptedPassword));

        FeedAdapter feedAdapter = new FeedAdapter(aggregatorChain, aggregatorConfiguration);
        List<Message> messages = feedAdapter.poll(sourceStatus);
        assertTrue(messages.size() > 0);
        assertTrue(messages.get(0).getMessageType().equals(MessageType.CONTENT));

    }

    @Test(expected = AdapterException.class)
    public void testNoFeed() throws AdapterException {

        Source source = new Source(FeedAdapter.SOURCE_TYPE);
        SourceStatus sourceStatus = new SourceStatus(source);

        source.addAccessParameter(new Property(FeedAdapter.ACCESS_PARAMETER_URI,
                "http://example.com/nofeedhere"));
        FeedAdapter feedAdapter = new FeedAdapter(aggregatorChain, aggregatorConfiguration);
        feedAdapter.poll(sourceStatus);
    }

    @Test(expected = AdapterException.class)
    public void testNullURI() throws AdapterException {

        Source source = new Source(FeedAdapter.SOURCE_TYPE);
        SourceStatus sourceStatus = new SourceStatus(source);

        source.addAccessParameter(new Property(FeedAdapter.ACCESS_PARAMETER_URI, null));
        FeedAdapter feedAdapter = new FeedAdapter(aggregatorChain, aggregatorConfiguration);
        feedAdapter.poll(sourceStatus);
    }
}
