package de.spektrumprojekt.aggregator.adapter.rss;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import de.spektrumprojekt.datamodel.message.MessagePublicationDateComperator;
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

    /**
     * this class applies changes of a property of a source to its internal representation if
     * necessary
     */
    private abstract class PropertyExtractor {

        /**
         * the key of the {@link Property}
         */
        private final String propertyKey;

        public PropertyExtractor(String propertyKey) {
            super();
            this.propertyKey = propertyKey;
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
                    sourceStatus.removeProperty(property.getPropertyKey());
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
         * the method is used to extract Information from the dc module, its only called if no
         * information where extracted by extractSimple and there is a dc module
         * 
         * @param dcModule
         * @return the property's value
         */
        protected abstract String extractFrom(DCModule dcModule);

        /**
         * the method is used to extract Information from the syndFeed
         * 
         * @param syndFeed
         * @return the property's value
         */
        protected abstract String extractSimple(SyndFeed syndFeed);

        public boolean getPropertyValue(SyndFeed syndFeed, SourceStatus sourceStatus) {
            Property property = sourceStatus.getProperty(propertyKey);
            String currentValue = extractSimple(syndFeed);
            if (currentValue == null) {
                Object module = syndFeed.getModule(DCModule.URI);
                if (module != null && module instanceof DCModule) {
                    currentValue = extractFrom((DCModule) syndFeed.getModule(DCModule.URI));
                }
            }
            return changePropertyIfNecessary(sourceStatus, property, currentValue, propertyKey);
        }
    }

    /** The logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(FeedAdapter.class);

    /** The key for the id property **/
    public static final String MESSAGE_PROPERTY_ID = "id";

    private static final int THREAD_POOL_SIZE = 100;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    protected static final String CONTEXT_SOURCE_STATUS = "source_status";

    /** used to copy the copyright information */
    private final PropertyExtractor copyrightExtractor = new PropertyExtractor(
            Property.SOURCE_PROPERTY_KEY_COPYRIGHT) {
        @Override
        protected String extractFrom(DCModule dcModule) {
            return dcModule.getRights();
        }

        @Override
        protected String extractSimple(SyndFeed syndFeed) {
            return syndFeed.getCopyright();
        }
    };

    /** used to copy the title information */
    private final PropertyExtractor titleExtractor = new PropertyExtractor(
            Property.SOURCE_PROPERTY_KEY_TITLE) {
        @Override
        protected String extractFrom(DCModule dcModule) {
            return dcModule.getTitle();
        }

        @Override
        protected String extractSimple(SyndFeed syndFeed) {
            return syndFeed.getTitle();
        }
    };

    /** used to copy the description information */
    private final PropertyExtractor descriptionExtractor = new PropertyExtractor(
            Property.SOURCE_PROPERTY_KEY_DESCRIPTION) {
        @Override
        protected String extractFrom(DCModule dcModule) {
            return dcModule.getDescription();
        }

        @Override
        protected String extractSimple(SyndFeed syndFeed) {
            return syndFeed.getDescription();
        }
    };

    public XMLAdapter(AggregatorChain aggregatorChain,
            AggregatorConfiguration aggregatorConfiguration) {
        super(aggregatorChain, aggregatorConfiguration, aggregatorConfiguration
                .getPollingInterval(), aggregatorConfiguration.getThreadPoolSize());
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
            modified = copyrightExtractor.getPropertyValue(syndFeed, sourceStatus);
            modified = titleExtractor.getPropertyValue(syndFeed, sourceStatus);
            modified = descriptionExtractor.getPropertyValue(syndFeed, sourceStatus);
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
                return MAPPER.writeValueAsString(tags.toArray(new String[] { }));
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

    /**
     * Filter the messages on the first poll by removing messages that exceed a certain amount and
     * are older than 30 days. This mimics the behavior of a SubscriptionFilter which is used when a
     * subscription to an existing source is added.
     * 
     * @param messages
     *            the messages to filter
     */
    private void filterMessagesOnFirstPoll(List<Message> messages) {
        // TODO limits should be configurable. Maybe store as source properties so that they are
        // available during creation?
        int lastXMessages = 25;
        if (messages.size() > lastXMessages) {
            Calendar cal = new GregorianCalendar();
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, -30);
            Date lastMessageDate = cal.getTime();
            Collections.sort(messages, MessagePublicationDateComperator.INSTANCE);
            Iterator<Message> messageIterator = messages.iterator();
            while (messageIterator.hasNext() && messages.size() > lastXMessages) {
                Message message = messageIterator.next();
                if (lastMessageDate.after(message.getPublicationDate())) {
                    messageIterator.remove();
                } else {
                    break;
                }
            }
        }
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
        if (lastContentTime == null) {
            filterMessagesOnFirstPoll(messages);
        }
        if (mostRecentTime != null) {
            // TODO probably not the best idea to let every Adapter handle it. Should probably be
            // done in addMessages but only if chain handled the message successfully, which is
            // currently not possible since Chain does not provide an info if it was a success or
            // not.
            sourceStatus.setLastContentTimestamp(mostRecentTime);
            getAggregatorChain().getPersistence().updateSourceStatus(sourceStatus);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Number of new items in source {}: {}", sourceStatus.getSource()
                    .getGlobalId(), messages.size());
        }
        return messages;
    }
}
