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

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserStreamAdapter;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import de.spektrumprojekt.aggregator.adapter.BaseAdapter;
import de.spektrumprojekt.aggregator.configuration.AggregatorConfiguration;
import de.spektrumprojekt.commons.SpektrumUtils;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.subscription.Subscription;
import de.spektrumprojekt.datamodel.subscription.SubscriptionStatus;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.persistence.Persistence;

/**
 * <p>
 * A Twitter adapter using Twitter4J.
 * </p>
 * 
 * @see <a href="http://twitter4j.org/">Twitter4J</a>
 * @author Philipp Katz
 */
public final class TwitterAdapter extends BaseAdapter {

    /**
     * Listener adapter for Twitter4j, responsible for transforming the {@link Status} items to
     * {@link Message}s.
     */
    private final class UserListener extends UserStreamAdapter {
        private final SubscriptionStatus subscriptionStatus;

        private UserListener(SubscriptionStatus subscription) {
            this.subscriptionStatus = subscription;
        }

        @Override
        public void onException(Exception ex) {
            LOGGER.warn("twitter exception " + ex);
            triggerListener(subscriptionStatus.getSubscription(),
                    StatusType.ERROR_INTERNAL_ADAPTER, ex);
        }

        @Override
        public void onStatus(Status status) {
            LOGGER.debug("status " + status);
            String text = status.getText();
            if (SpektrumUtils.notNullOrEmpty(text)) {
                Message message = new Message(MessageType.CONTENT,
                        StatusType.OK, subscriptionStatus.getSubscription()
                                .getGlobalId(), status.getCreatedAt());
                MessagePart messagePart = new MessagePart(MimeType.TEXT_PLAIN,
                        text);
                message.addMessagePart(messagePart);
                User user = status.getUser();
                if (user != null) {
                    String screenName = user.getScreenName();
                    message.addProperty(new Property(SCREEN_NAME, screenName));
                    if (user.getProfileImageURL() != null) {
                        String profileImageUrl = user.getProfileImageURL()
                                .toString();
                        message.addProperty(new Property("profileImageUrl",
                                profileImageUrl));
                    }
                }
                String statusId = String.valueOf(status.getId());
                String replyToStatusId = String.valueOf(status
                        .getInReplyToStatusId());
                message.addProperty(new Property(STATUS_ID, statusId));
                message.addProperty(new Property("replyToStatusId",
                        replyToStatusId));
                addMessage(message);
            }
        }
    }

    public static final String SCREEN_NAME = "screenName";

    public static final String STATUS_ID = "statusId";

    /** The logger for this class. */
    private static final Logger LOGGER = LoggerFactory
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
    public TwitterAdapter(Communicator communicator, Persistence persistence,
            AggregatorConfiguration aggregatorConfiguration) {
        this(communicator, persistence, aggregatorConfiguration, aggregatorConfiguration
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
    public TwitterAdapter(Communicator communicator, Persistence persistence,
            AggregatorConfiguration aggregatorConfiguration,
            String consumerKey, String consumerSecret) {
        super(communicator, persistence, aggregatorConfiguration);
        Validate.notEmpty(consumerKey, "consumerKey must not be empty or null");
        Validate.notEmpty(consumerSecret,
                "consumerSecret must not be empty or null");
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.twitterStreams = new ConcurrentHashMap<String, TwitterStream>();
    }

    @Override
    public void addSubscription(SubscriptionStatus subscriptionStatus) {

        Subscription subscription = subscriptionStatus.getSubscription();
        Property token = subscription
                .getAccessParameter(ACCESS_PARAMETER_TOKEN);
        Property tokenSecret = subscription
                .getAccessParameter(ACCESS_PARAMETER_TOKEN_SECRET);

        String subscriptionID = subscription.getGlobalId();
        if (twitterStreams.containsKey(subscriptionID)) {
            LOGGER.error("already subscribed to {}", subscriptionID);
        } else if (SpektrumUtils.notNull(token, tokenSecret)) {
            subscribe(token.getPropertyValue(), tokenSecret.getPropertyValue(),
                    subscriptionStatus);
        } else {
            LOGGER.error("necessary information missing");
            triggerListener(subscription, StatusType.ERROR_INSUFFICIENT_DATA);
        }
    }

    @Override
    public String getSourceType() {
        return SOURCE_TYPE;
    }

    @Override
    public Collection<String> getSubscriptionGlobalIds() {
        return twitterStreams.keySet();
    }

    @Override
    public void removeSubscription(String subscriptionId) {
        TwitterStream twitterStream = twitterStreams.remove(subscriptionId);
        if (twitterStream != null) {
            twitterStream.shutdown();
            LOGGER.debug("unsubscribed from " + subscriptionId);
        } else {
            LOGGER.warn("no subscription for " + subscriptionId
                    + " to unsubscribe from");
        }
    }

    @Override
    public void stop() {
        for (TwitterStream stream : twitterStreams.values()) {
            stream.cleanUp();
        }
    }

    private void subscribe(String accessToken, String accessTokenSecret,
            final SubscriptionStatus subscription) {
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
            LOGGER.debug("IllegalStateException for {}: {}", subscription,
                    e.getMessage());
            triggerListener(subscription.getSubscription(),
                    StatusType.ERROR_INTERNAL_ADAPTER);
            return;
        } catch (TwitterException e) {
            LOGGER.debug("TwitterException for {}: {}", subscription,
                    e.getMessage());
            triggerListener(subscription.getSubscription(),
                    StatusType.ERROR_NETWORK);
            return;
        }
        twitterStream.addListener(new UserListener(subscription));
        twitterStream.user();
        twitterStreams.put(subscription.getSubscription().getGlobalId(),
                twitterStream);
    }

}
