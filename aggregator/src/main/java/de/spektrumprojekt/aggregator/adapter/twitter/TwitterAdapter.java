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

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import de.spektrumprojekt.aggregator.adapter.BaseAdapter;
import de.spektrumprojekt.aggregator.chain.AggregatorChain;
import de.spektrumprojekt.aggregator.configuration.AggregatorConfiguration;
import de.spektrumprojekt.commons.SpektrumUtils;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.source.Source;
import de.spektrumprojekt.datamodel.source.SourceStatus;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;

/**
 * <p>
 * A Twitter adapter using Twitter4J.
 * </p>
 * 
 * @see <a href="http://twitter4j.org/">Twitter4J</a>
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * @author Philipp Katz
 */
public final class TwitterAdapter extends BaseAdapter {

    public static final String SCREEN_NAME = "screenName";

    public static final String STATUS_ID = "statusId";

    /** The logger for this class. */
    static final Logger LOGGER = LoggerFactory
            .getLogger(TwitterAdapter.class);

    /** The Twitter consumer key necessary for authentication at Twitter API. */
    private final String consumerKey;

    /**
     * The Twitter consumer key secret necessary for authentication at Twitter API.
     */
    private final String consumerSecret;

    /** The key for the token access parameter. */
    public static final String ACCESS_PARAMETER_TOKEN = "access_token";

    /** The key for the token secret access parameter. */
    public static final String ACCESS_PARAMETER_TOKEN_SECRET = "access_token_secret";

    /** The source type of this Adapter. */
    public static final String SOURCE_TYPE = "twitter";

    /** Keep all mappings from Subscription IDs to Twitter streams. */
    private final ConcurrentMap<String, TwitterStream> twitterStreams;

    /**
     * <p>
     * Initialize a new {@link TwitterAdapter} with the consumer key and secret taken from the
     * {@link AggregatorConfiguration}, and the keys {@value #ACCESS_PARAMETER_TOKEN} and
     * {@value #ACCESS_PARAMETER_TOKEN_SECRET}.
     * </p>
     */
    public TwitterAdapter(AggregatorChain aggregatorChain,
            AggregatorConfiguration aggregatorConfiguration) {
        this(aggregatorChain, aggregatorConfiguration, aggregatorConfiguration
                .getTwitterConsumerKey(), aggregatorConfiguration
                .getTwitterConsumerSecret());
    }

    /**
     * <p>
     * Initialize a new {@link TwitterAdapter} with the given consumer key and secret.
     * </p>
     * 
     * @param consumerKey
     *            The consumer key, not <code>null</code> or empty.
     * @param consumerSecret
     *            The consumer secret, not <code>null</code> or empty.
     */
    public TwitterAdapter(AggregatorChain aggregatorChain,
            AggregatorConfiguration aggregatorConfiguration,
            String consumerKey, String consumerSecret) {
        super(aggregatorChain, aggregatorConfiguration);

        Validate.notEmpty(consumerKey, "consumerKey must not be empty or null");
        Validate.notEmpty(consumerSecret,
                "consumerSecret must not be empty or null");

        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.twitterStreams = new ConcurrentHashMap<String, TwitterStream>();
    }

    @Override
    public void addSource(SourceStatus sourceStatus) {

        Source source = sourceStatus.getSource();
        Property token = source
                .getAccessParameter(ACCESS_PARAMETER_TOKEN);
        Property tokenSecret = source
                .getAccessParameter(ACCESS_PARAMETER_TOKEN_SECRET);

        String sourceGlobalId = source.getGlobalId();
        if (twitterStreams.containsKey(sourceGlobalId)) {
            LOGGER.error("already subscribed to {}", sourceGlobalId);
        } else if (SpektrumUtils.notNull(token, tokenSecret)) {
            listenToSource(token.getPropertyValue(), tokenSecret.getPropertyValue(),
                    sourceStatus);
        } else {
            LOGGER.error("necessary information missing");
            triggerListener(source, StatusType.ERROR_INSUFFICIENT_DATA);
        }
    }

    /**
     * <p>
     * Allows subclasses to put acquired messages in the queue.
     * </p>
     * 
     * @param message
     *            The message to put into the queue.
     */
    void addTwitterMessage(Message message) {
        this.addMessage(message);
    }

    @Override
    public Collection<String> getSourceGlobalIds() {
        return twitterStreams.keySet();
    }

    @Override
    public String getSourceType() {
        return SOURCE_TYPE;
    }

    private void listenToSource(String accessToken, String accessTokenSecret,
            final SourceStatus sourceStatus) {
        ConfigurationBuilder configBuilder = new ConfigurationBuilder();
        configBuilder.setOAuthConsumerKey(consumerKey);
        configBuilder.setOAuthConsumerSecret(consumerSecret);
        configBuilder.setOAuthAccessToken(accessToken);
        configBuilder.setOAuthAccessTokenSecret(accessTokenSecret);
        Configuration config = configBuilder.build();
        TwitterStream twitterStream = new TwitterStreamFactory(config)
                .getInstance();
        try {
            LOGGER.info("subscription for screenName "
                    + twitterStream.getScreenName());
        } catch (IllegalStateException e) {
            LOGGER.debug("IllegalStateException for {}: {}", sourceStatus,
                    e.getMessage());
            triggerListener(sourceStatus.getSource(),
                    StatusType.ERROR_INTERNAL_ADAPTER);
            return;
        } catch (TwitterException e) {
            LOGGER.debug("TwitterException for {}: {}", sourceStatus,
                    e.getMessage());
            triggerListener(sourceStatus.getSource(),
                    StatusType.ERROR_NETWORK);
            return;
        }
        twitterStream.addListener(new UserListener(this, sourceStatus));
        twitterStream.user();
        twitterStreams.put(sourceStatus.getSource().getGlobalId(),
                twitterStream);
    }

    @Override
    public void removeSource(String sourceGlobalId) {
        TwitterStream twitterStream = twitterStreams.remove(sourceGlobalId);
        if (twitterStream != null) {
            twitterStream.shutdown();
            LOGGER.debug("removed source " + sourceGlobalId);
        } else {
            LOGGER.warn("no source for " + sourceGlobalId
                    + " to remove from");
        }
    }

    @Override
    public void stop() {
        for (TwitterStream stream : twitterStreams.values()) {
            stream.cleanUp();
        }
    }

    void triggerTwitterListener(Source source, StatusType statusType,
            Exception exception) {
        this.triggerListener(source, statusType, exception);
    }

}
