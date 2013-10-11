package de.spektrumprojekt.aggregator.adapter.rss;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
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
import de.spektrumprojekt.aggregator.chain.AggregatorChain;
import de.spektrumprojekt.aggregator.configuration.AggregatorConfiguration;
import de.spektrumprojekt.commons.SpektrumUtils;
import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.message.ScoredTerm;
import de.spektrumprojekt.datamodel.message.Term;
import de.spektrumprojekt.datamodel.message.Term.TermCategory;
import de.spektrumprojekt.datamodel.source.SourceStatus;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;

/**
 * <p>
 * An adapter handling RSS and Atom feeds.
 * </p>
 * 
 * @author Philipp Katz
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 */
public abstract class XMLAdapter extends BasePollingAdapter {

    /** The logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(FeedAdapter.class);

    /** The key for the id property **/
    public static final String MESSAGE_PROPERTY_ID = "id";

    private static final int THREAD_POOL_SIZE = 100;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    protected static final String CONTEXT_SOURCE_STATUS = "source_status";

    /** the property key for the property containing the copyright information of the source */
    public static final String SOURCE_PROPERTY_KEY_COPYRIGHT = "copyright";

    /** the property key for the property containing the copyright information of the source */
    public static final String SOURCE_PROPERTY_KEY_TITLE = "title";

    /** the property key for the property containing the copyright information of the source */
    public static final String SOURCE_PROPERTY_KEY_DESCRIPTION = "description";

    public XMLAdapter(AggregatorChain aggregatorChain,
            AggregatorConfiguration aggregatorConfiguration) {
        super(aggregatorChain, aggregatorConfiguration, aggregatorConfiguration
                .getPollingInterval(), THREAD_POOL_SIZE);
    }

    /**
     * 
     * @param sourceStatus
     * @param property
     * @param value
     *            propertyValue
     * @param key
     *            propertyKey
     * @return true if the sourceStatus was modified
     */
    private boolean changePropertyIfNecessary(SourceStatus sourceStatus, Property property,
            String value, String key) {
        if (value == null) {
            if (property != null) {
                sourceStatus.remove(property);
                return true;
            }
        } else {
            if (property == null || !value.equals(property.getPropertyValue())) {
                sourceStatus.addProperty(new Property(key, value));
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * @param in
     *            InputStream to close
     * @param context
     *            additional information
     * @param success
     *            the retrieval ended in success or not
     */
    protected void cleanUpResources(InputStream in, Map<String, Object> context, boolean success) {
        try {
            if (in != null) {
                in.close();
            }
        } catch (Exception e) {
            LOGGER.warn("Error closing feed input stream " + e.getMessage());
            LOGGER.debug("Error closing feed input stream " + e.getMessage(), e);
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
        DCModule module = (DCModule) syndEntry.getModule(DCModule.URI);
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

    /**
     * 
     * @param syndFeed
     *            current Information
     * @param sourceStatus
     * @return true if the source was modified
     */
    private boolean extractFeedPropertiesIfUnset(SyndFeed syndFeed, SourceStatus sourceStatus) {
        sourceStatus = getAggregatorChain().getPersistence().getSourceStatusBySourceGlobalId(
                sourceStatus.getSource().getGlobalId());
        if (sourceStatus != null) {
            boolean modified = false;
            Property property = sourceStatus.getProperty(SOURCE_PROPERTY_KEY_COPYRIGHT);
            String currentValue = syndFeed.getCopyright();
            if (currentValue == null) {
                Object module = syndFeed.getModule(DCModule.URI);
                if (module != null && module instanceof DCModule) {
                    currentValue = ((DCModule) syndFeed.getModule(DCModule.URI)).getRights();
                }
            }
            modified = changePropertyIfNecessary(sourceStatus, property, currentValue,
                    SOURCE_PROPERTY_KEY_COPYRIGHT);
            property = sourceStatus.getProperty(SOURCE_PROPERTY_KEY_DESCRIPTION);
            currentValue = syndFeed.getDescription();
            modified = changePropertyIfNecessary(sourceStatus, property, currentValue,
                    SOURCE_PROPERTY_KEY_DESCRIPTION);
            property = sourceStatus.getProperty(SOURCE_PROPERTY_KEY_TITLE);
            currentValue = syndFeed.getTitle();
            modified = changePropertyIfNecessary(sourceStatus, property, currentValue,
                    SOURCE_PROPERTY_KEY_TITLE);
            if (modified) {
                getAggregatorChain().getPersistence().updateSourceStatus(sourceStatus);
            }
            return modified;
        } else {
            return false;
        }
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

    protected abstract InputStream getInputStream(Map<String, Object> context)
            throws AdapterException;

    @Override
    public List<Message> poll(SourceStatus subscriptionStatus) throws AdapterException {
        LOGGER.trace(">handleSubscription {}", subscriptionStatus);
        boolean success = false;
        List<Message> messages = new ArrayList<Message>();
        Map<String, Object> context = new HashMap<String, Object>();
        context.put(CONTEXT_SOURCE_STATUS, subscriptionStatus);

        // logger.debug("access parameters for {}: {}", subscription,
        // accParams);

        // if (SpektrumUtils.notNullOrEmpty(uri)) { TODO
        InputStream in = getInputStream(context);
        try {
            if (in != null) {
                SyndFeedInput feedInput = new SyndFeedInput();
                SyndFeed syndFeed = feedInput.build(new InputSource(in));
                extractFeedPropertiesIfUnset(syndFeed, subscriptionStatus);
                // LOGGER.debug("retrieved {} items from {}", syndFeed.getEntries().size(), uri);
                messages = processMessages(syndFeed, subscriptionStatus);
                success = true;
            } else {
                throw new AdapterException("No content", StatusType.ERROR_PROCESSING_CONTENT);
            }
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
            cleanUpResources(in, context, success);
        }
        // }
        LOGGER.trace("<handleSubscription, success {}", success);
        return messages;
    }

    private List<Message> processMessages(SyndFeed feed, SourceStatus sourceStatus) {
        @SuppressWarnings("unchecked")
        List<SyndEntry> entries = feed.getEntries();
        List<Message> messages = new ArrayList<Message>();
        Date lastContentTime = sourceStatus.getLastContentTimestamp();
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

            Message message = convertMessage(sourceStatus.getSource().getGlobalId(), entry);
            messages.add(message);

            if (mostRecentTime == null || mostRecentTime.before(itemPublishDate)) {
                mostRecentTime = itemPublishDate;
            }
        }
        if (mostRecentTime != null) {
            sourceStatus.setLastContentTimestamp(mostRecentTime);
        }
        LOGGER.debug("# new items in subscription {}: {}", sourceStatus.getGlobalId(),
                messages.size());
        return messages;
    }
}
