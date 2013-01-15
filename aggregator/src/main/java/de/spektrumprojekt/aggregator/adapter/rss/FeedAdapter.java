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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.net.ssl.SSLException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.ContentEncodingHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import com.sun.syndication.feed.module.DCModule;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;

import de.spektrumprojekt.aggregator.adapter.AdapterException;
import de.spektrumprojekt.aggregator.adapter.BasePollingAdapter;
import de.spektrumprojekt.aggregator.configuration.AggregatorConfiguration;
import de.spektrumprojekt.commons.SpektrumUtils;
import de.spektrumprojekt.commons.encryption.EncryptionException;
import de.spektrumprojekt.commons.encryption.EncryptionUtils;
import de.spektrumprojekt.communication.Communicator;
import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.message.Term.TermCategory;
import de.spektrumprojekt.datamodel.subscription.SubscriptionStatus;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.persistence.Persistence;

/**
 * <p>
 * An adapter handling RSS and Atom feeds.
 * </p>
 * 
 * @author Marius Feldmann
 * @author Philipp Katz
 */
public final class FeedAdapter extends BasePollingAdapter {

    /** The logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(FeedAdapter.class);

    /** The source type of this adapter. */
    public static final String SOURCE_TYPE = "RSS";

    private HttpClient httpClient;

    /** The key for the access parameter specifying the feed's URL. */
    public static final String ACCESS_PARAMETER_URI = "feeduri";

    /**
     * The key for the access parameter specifying the login, if authentication is necessary.
     */
    public static final String ACCESS_PARAMETER_CREDENTIALS_LOGIN = "credentials_login";

    /**
     * The key for the access parameter specifying the password, if authentication is necessary.
     */
    public static final String ACCESS_PARAMETER_CREDENTIALS_PASSWORD = "credentials_password";

    /** The key for the id property **/
    public static final String MESSAGE_PROPERTY_ID = "id";

    private static final int THREAD_POOL_SIZE = 100;

    public static final String DC_CREATOR = "dc:creator";

    public static final String AUTOR_NAME = "autor.name";

    public FeedAdapter(Communicator communicator, Persistence persistence,
            AggregatorConfiguration aggregatorConfiguration) {
        super(communicator, persistence, aggregatorConfiguration, aggregatorConfiguration
                .getPollingInterval(), THREAD_POOL_SIZE);

    }

    @SuppressWarnings("unchecked")
    private Message convertMessage(String subscriptionId, SyndEntry syndEntry) {
        Date publishedDate = syndEntry.getPublishedDate();
        Message message = new Message(MessageType.CONTENT, StatusType.OK, subscriptionId,
                publishedDate);
        message.addProperty(new Property(Property.PROPERTY_KEY_TITLE, syndEntry.getTitle()));

        if (syndEntry.getUri() != null) {
            message.addProperty(new Property(MESSAGE_PROPERTY_ID, syndEntry.getUri()));
        }

        List<SyndContent> syndContents = syndEntry.getContents();
        if (syndContents != null && !syndContents.isEmpty()) {
            String text = syndContents.get(0).getValue();
            MessagePart entryText = new MessagePart(MimeType.TEXT_HTML,
                    StringEscapeUtils.unescapeHtml(text));

            List<SyndCategory> categories = syndEntry.getCategories();
            if (categories != null && !categories.isEmpty()) {
                Collection<ScoredTerm> keyMetaInfo = new HashSet<ScoredTerm>();
                for (SyndCategory category : categories) {
                    String categoryName = category.getName();
                    if (SpektrumUtils.notNullOrEmpty(categoryName)) {
                        keyMetaInfo
                                .add(new ScoredTerm(new Term(TermCategory.TERM, categoryName), 1));
                    }
                }
            }
            message.addMessagePart(entryText);
        }
        SyndContent description = syndEntry.getDescription();
        if (description != null) {
            MessagePart entrySummary = new MessagePart(MimeType.TEXT_HTML,
                    StringEscapeUtils.unescapeHtml(description.getValue()));
            message.addMessagePart(entrySummary);
        }
        String link = syndEntry.getLink();
        if (SpektrumUtils.notNullOrEmpty(link)) {
            message.addProperty(new Property("link", link));
        }
        DCModule module = (DCModule) syndEntry.getModule(DCModule.URI);
        userextraction: {
            String creator;
            if (module != null) {
                creator = module.getCreator();
                if (SpektrumUtils.notNullOrEmpty(creator)) {
                    message.addProperty(new Property(DC_CREATOR, creator));
                    break userextraction;
                }
            }
            creator = syndEntry.getAuthor();
            if (SpektrumUtils.notNullOrEmpty(creator)) {
                message.addProperty(new Property(AUTOR_NAME, creator));
            }
        }

        return message;
    }

    @Override
    public String getSourceType() {
        return SOURCE_TYPE;
    }

    @Override
    public List<Message> poll(SubscriptionStatus subscriptionStatus) throws AdapterException {
        LOGGER.trace(">handleSubscription {}", subscriptionStatus);
        boolean success = false;
        List<Message> messages = new ArrayList<Message>();

        Collection<Property> accParams = subscriptionStatus.getSubscription().getAccessParameters();
        // logger.debug("access parameters for {}: {}", subscription,
        // accParams);
        String uri = "";
        String base64EncodedCredentials = "";
        String login = "";
        String password = "";

        // FIXME shouldn't this be set via CredentialElement???
        for (Property accessParam : accParams) {
            if (accessParam.getPropertyKey().equals(ACCESS_PARAMETER_URI)) {
                uri = accessParam.getPropertyValue();
            } else if (accessParam.getPropertyKey().equals(ACCESS_PARAMETER_CREDENTIALS_LOGIN)) {
                login = accessParam.getPropertyValue();
            } else if (accessParam.getPropertyKey().equals(ACCESS_PARAMETER_CREDENTIALS_PASSWORD)) {
                try {
                    password = EncryptionUtils.decrypt(accessParam.getPropertyValue(),
                            getAggregatorConfiguration().getEncryptionPassword());
                } catch (EncryptionException e) {
                    throw new AdapterException("Error during password decryption", e,
                            StatusType.ERROR_INTERNAL_ADAPTER);
                }
            }
        }
        // only if login + password were supplied
        if (login.length() > 0 && password.length() > 0) {
            try {
                base64EncodedCredentials = Base64
                        .encodeBase64URLSafeString((login + ":" + password).getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new AdapterException("Unsupported encoding", e,
                        StatusType.ERROR_INTERNAL_ADAPTER);
            }
        }
        if (SpektrumUtils.notNullOrEmpty(uri)) {
            try {
                HttpResponse httpResult = retrieveHttpResult(uri, base64EncodedCredentials);
                int statusCode = httpResult.getStatusLine().getStatusCode();
                if (statusCode >= 400) {
                    throw new AdapterException("HTTP error code " + statusCode,
                            StatusType.ERROR_NETWORK);
                }
                HttpEntity httpEntity = httpResult.getEntity();
                if (httpEntity != null) {
                    SyndFeedInput feedInput = new SyndFeedInput();
                    SyndFeed syndFeed = feedInput.build(new InputSource(httpEntity.getContent()));
                    LOGGER.debug("retrieved {} items from {}", syndFeed.getEntries().size(), uri);
                    messages = processMessages(syndFeed, subscriptionStatus);
                    success = true;
                } else {
                    throw new AdapterException("No content", StatusType.ERROR_PROCESSING_CONTENT);
                }
            } catch (ClientProtocolException e) {
                throw new AdapterException("ClientProtocolException " + e.getMessage(), e,
                        StatusType.ERROR_NETWORK);
            } catch (SSLException e) {
                throw new AdapterException("SSLException: " + e.getMessage(), e,
                        StatusType.ERROR_SSL);
            } catch (IOException e) {
                throw new AdapterException("IOException: " + e.getMessage(), e,
                        StatusType.ERROR_NETWORK);
            } catch (IllegalArgumentException e) {
                throw new AdapterException("IllegalArgumentException " + e.getMessage(), e,
                        StatusType.ERROR_INVALID_DATA);
            } catch (IllegalStateException e) {
                throw new AdapterException("IllegalStateException " + e.getMessage(), e,
                        StatusType.ERROR_NETWORK);
            } catch (FeedException e) {
                throw new AdapterException("FeedException " + e.getMessage(), e,
                        StatusType.ERROR_PROCESSING_CONTENT);
            }
        }
        LOGGER.trace("<handleSubscription, success {}", success);
        return messages;
    }

    private List<Message> processMessages(SyndFeed feed, SubscriptionStatus subscription) {
        @SuppressWarnings("unchecked")
        List<SyndEntry> entries = feed.getEntries();
        List<Message> messages = new ArrayList<Message>();
        Date lastContentTime = subscription.getLastContentTimestamp();
        Date mostRecentTime = null;

        // TODO for feeds which provide no date information for their items,
        // implement a filtering strategy based on items' hashes.

        for (SyndEntry entry : entries) {
            Date itemPublishDate = entry.getPublishedDate();

            // ignore those messages, which were already acquired in last poll
            if (itemPublishDate != null && lastContentTime != null
                    && !itemPublishDate.after(lastContentTime)) {
                continue;
            }

            Message message = convertMessage(subscription.getSubscription().getGlobalId(), entry);
            messages.add(message);

            if (mostRecentTime == null || mostRecentTime.before(itemPublishDate)) {
                mostRecentTime = itemPublishDate;
            }
        }
        if (mostRecentTime != null) {
            subscription.setLastContentTimestamp(mostRecentTime);
        }
        LOGGER.debug("# new items in subscription {}: {}", subscription.getGlobalId(),
                messages.size());
        return messages;
    }

    private HttpResponse retrieveHttpResult(String uri, String base64EncodedCredentials)
            throws ClientProtocolException, IOException {
        HttpGet get = new HttpGet(uri);
        httpClient = new ContentEncodingHttpClient();
        if (SpektrumUtils.notNullOrEmpty(base64EncodedCredentials)) {
            get.setHeader("Authorization", "Basic " + base64EncodedCredentials);
        }
        return httpClient.execute(get);
    }

}