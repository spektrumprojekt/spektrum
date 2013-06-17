package de.spektrumprojekt.informationextraction.relations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.lang3.Validate;
import org.xml.sax.InputSource;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;

import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;

/**
 * <p>
 * Read test data from an extracted Communote RSS feed.
 * </p>
 * 
 * @author Philipp Katz
 */
public final class FeedTestDataSource implements Iterable<Message> {

    /** The RSS file being read. */
    private final File feedXmlFile;

    /**
     * <p>
     * Create a new test data source with the given file.
     * </p>
     * 
     * @param feedXmlFile
     *            The file with the RSS feed, not <code>null</code>.
     */
    public FeedTestDataSource(File feedXmlFile) {
        Validate.notNull(feedXmlFile, "feedXmlFile must not be null");
        this.feedXmlFile = feedXmlFile;
    }

    @Override
    public Iterator<Message> iterator() {

        SyndFeedInput feedInput = new SyndFeedInput();
        SyndFeed syndFeed;
        try {
            InputStream inputStream = new FileInputStream(feedXmlFile);
            syndFeed = feedInput.build(new InputSource(inputStream));
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Could not find the file "
                    + feedXmlFile.getAbsolutePath());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Could not parse the feed "
                    + feedXmlFile.getAbsolutePath());
        } catch (FeedException e) {
            throw new IllegalStateException("Could not parse the feed "
                    + feedXmlFile.getAbsolutePath());
        }

        Collections.reverse(syndFeed.getEntries());
        @SuppressWarnings("unchecked")
        final Iterator<SyndEntry> entryIterator = syndFeed.getEntries().iterator();

        return new Iterator<Message>() {

            @Override
            public boolean hasNext() {
                return entryIterator.hasNext();
            }

            @Override
            public Message next() {
                return convert(entryIterator.next());
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private Message convert(SyndEntry syndEntry) {
        SyndContent description = syndEntry.getDescription();
        if (description == null) {
            throw new IllegalStateException(
                    "SyndEntry does not contain expected description element");
        }
        Date publishedDate = syndEntry.getPublishedDate();
        Message message = new Message(MessageType.CONTENT, StatusType.OK, publishedDate);
        message.addMessagePart(new MessagePart(MimeType.TEXT_HTML, description.getValue()));
        message.addProperty(new Property(Property.PROPERTY_KEY_LINK, syndEntry.getLink()));
        message.addProperty(new Property(Property.PROPERTY_KEY_TITLE, syndEntry.getTitle()));
        message.addProperty(new Property("autor.name", syndEntry.getAuthor()));
        return message;
    }

}
