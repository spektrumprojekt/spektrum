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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
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
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import com.sun.syndication.feed.module.DCModule;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndCategoryImpl;
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
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 */
public final class FeedAdapter extends BasePollingAdapter {

    /** The logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(FeedAdapter.class);

    /** The source type of this adapter. */
    public static final String SOURCE_TYPE = "RSS";

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

    /**
     * @deprecated Use {@link Property#PROPERTY_KEY_DC_CREATOR} instead.
     */
    @Deprecated
    public static final String DC_CREATOR = Property.PROPERTY_KEY_DC_CREATOR;

    /**
     * @deprecated Use {@link Property#PROPERTY_KEY_AUTHOR_NAME} instead.
     */
    @Deprecated
    public static final String AUTOR_NAME = Property.PROPERTY_KEY_AUTHOR_NAME;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Encode the login and password as base 64
     * 
     * @param login
     *            the login
     * @param password
     *            the password
     * @return the credentials string for the base authentication, that is login + ":" + password
     *         and encoded as Base64, using utf8
     * @throws UnsupportedEncodingException
     */
    public static String getBaseAuthenticationCredentials(String login, String password)
            throws UnsupportedEncodingException {
        String auth = login + ":" + password;
        String base64EncodedCredentials = new String(Base64.encodeBase64(auth.getBytes("UTF-8")),
                "UTF-8");
        return base64EncodedCredentials;
    }

    public FeedAdapter(Communicator communicator, Persistence persistence,
            AggregatorConfiguration aggregatorConfiguration) {
        super(communicator, persistence, aggregatorConfiguration, aggregatorConfiguration
                .getPollingInterval(), THREAD_POOL_SIZE);

    }

    /**
     * Abort the request due to an error before
     * 
     * @param get
     */
    private void abortRequest(HttpGet get) {
        try {
            get.abort();
        } catch (Exception e) {
            LOGGER.warn("Error in aborting request: " + e.getMessage());
            LOGGER.debug("Error in aborting request: " + e.getMessage(), e);
        }
    }

    private void cleanUpResources(InputStream in, HttpGet get, boolean success) {
        try {
            if (in != null) {
                in.close();
            }
        } catch (Exception e) {
            LOGGER.warn("Error closing feed input stream " + e.getMessage());
            LOGGER.debug("Error closing feed input stream " + e.getMessage(), e);
        }
        try {
            if (!success && get != null) {
                this.abortRequest(get);
            }
        } catch (Exception e) {
            LOGGER.warn("Error aborting request " + e.getMessage());
            LOGGER.debug("Error aborting request " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Message convertMessage(String subscriptionId, SyndEntry syndEntry) {
        Date publishedDate = extractDate(subscriptionId, syndEntry);
        String tags = extractTags(syndEntry);
        Message message = new Message(MessageType.CONTENT, StatusType.OK, subscriptionId,
                publishedDate);
        message.addProperty(new Property(Property.PROPERTY_KEY_TITLE, syndEntry.getTitle()));
        if (tags != null) {
            message.addProperty(new Property(Property.PROPERTY_KEY_TAGS, tags));
        }

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
            message.addProperty(new Property(Property.PROPERTY_KEY_LINK, link));
        }
        DCModule module = (DCModule)syndEntry.getModule(DCModule.URI);
        userextraction: {
            String creator;
            if (module != null) {
                creator = module.getCreator();
                if (SpektrumUtils.notNullOrEmpty(creator)) {
                    message.addProperty(new Property(Property.PROPERTY_KEY_DC_CREATOR, creator));
                    break userextraction;
                }
            }
            creator = syndEntry.getAuthor();
            if (SpektrumUtils.notNullOrEmpty(creator)) {
                message.addProperty(new Property(Property.PROPERTY_KEY_AUTHOR_NAME, creator));
            }
        }

        return message;
    }

    private HttpGet createHttpGetRequest(String uri, String base64EncodedCredentials) {
        HttpGet get = new HttpGet(uri);
        if (SpektrumUtils.notNullOrEmpty(base64EncodedCredentials)) {
            get.setHeader("Authorization", "Basic " + base64EncodedCredentials);
        }
        return get;
    }

    private Date extractDate(String subscriptionId, SyndEntry syndEntry) {
        Date publishedDate = syndEntry.getPublishedDate();
        if (publishedDate == null) {
            publishedDate = syndEntry.getUpdatedDate();
        }
        if (publishedDate == null) {
            publishedDate = new Date();
            LOGGER.debug("For an entry in subscription {} no date was found. Using current time.",
                    subscriptionId);
        }
        return publishedDate;
    }

    private String extractTags(SyndEntry syndEntry) {
        List<String> tags = new LinkedList<String>();
        for (Object category : syndEntry.getCategories()) {
            SyndCategoryImpl categoryImpl = (SyndCategoryImpl) category;
            tags.add(categoryImpl.getName());
        }
        if (tags.size() > 0) {
            try {
                return MAPPER.writeValueAsString(tags.toArray(new String[] {}));
            } catch (JsonGenerationException e) {
                LOGGER.error("Error processing tags: {}", e);
            } catch (JsonMappingException e) {
                LOGGER.error("Error processing tags: {}", e);
            } catch (IOException e) {
                LOGGER.error("Error processing tags: {}", e);
            }
        }
        return null;
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
                base64EncodedCredentials = getBaseAuthenticationCredentials(login, password);

            } catch (UnsupportedEncodingException e) {
                throw new AdapterException("Unsupported encoding", e,
                        StatusType.ERROR_INTERNAL_ADAPTER);
            }
        }
        if (SpektrumUtils.notNullOrEmpty(uri)) {
            HttpGet get = null;
            HttpResponse httpResult = null;
            InputStream in = null;
            HttpClient httpClient = null;

            try {

                httpClient = new ContentEncodingHttpClient();
                get = createHttpGetRequest(uri, base64EncodedCredentials);
                httpResult = httpClient.execute(get);
                int statusCode = httpResult.getStatusLine().getStatusCode();

                if (statusCode >= 400) {
                    throw new AdapterException("HTTP error code " + statusCode,
                            StatusType.ERROR_NETWORK);
                }
                HttpEntity httpEntity = httpResult.getEntity();
                if (httpEntity != null) {
                    SyndFeedInput feedInput = new SyndFeedInput();
                    in = httpEntity.getContent();
                    SyndFeed syndFeed = feedInput.build(new InputSource(in));
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
            } finally {
                cleanUpResources(in, get, success);
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

}