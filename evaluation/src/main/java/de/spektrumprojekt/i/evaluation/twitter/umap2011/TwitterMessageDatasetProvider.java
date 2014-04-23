package de.spektrumprojekt.i.evaluation.twitter.umap2011;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessagePublicationDateComperator;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;
import de.spektrumprojekt.i.evaluation.AbstractMessageDataSetProvider;

public class TwitterMessageDatasetProvider extends AbstractMessageDataSetProvider {

    private SqlUmap2011TweetAccess umap2011TweetAccess;

    private final static Logger LOGGER = LoggerFactory
            .getLogger(TwitterMessageDatasetProvider.class);

    public static void main(String[] args) throws Exception {
        TwitterMessageDatasetProvider provider = new TwitterMessageDatasetProvider();
        provider.doInitalization();

        int mb = 1024 * 1024;
        System.gc();
        Runtime runtime = Runtime.getRuntime();
        // Print used memory
        LOGGER.info("Used Memory:"
                + (runtime.totalMemory() - runtime.freeMemory()) / mb);

        // Print free memory
        LOGGER.info("Free Memory:"
                + runtime.freeMemory() / mb);

        // Print total available memory
        LOGGER.info("Total Memory:" + runtime.totalMemory() / mb);

        // Print Maximum available memory
        LOGGER.info("Max Memory:" + runtime.maxMemory() / mb);
    }

    public void close() {
        closeSqlAccess();
    }

    public void closeSqlAccess() {
        if (umap2011TweetAccess != null) {
            try {
                umap2011TweetAccess.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String convertToMessageGlobalId(Long tweetId) {
        return tweetId + "";
    }

    private String convertToUserGlobalId(Long tweetUserId) {
        return tweetUserId + "";
    }

    private Message createMessage(Tweet tweet) throws Exception {

        Message message = new Message(convertToMessageGlobalId(tweet.getId()),
                MessageType.CONTENT, StatusType.OK,
                "subscription",
                tweet.getCreationTime());

        message.setAuthorGlobalId(convertToUserGlobalId(tweet.getUserId()));

        message.setId(tweet.getId());

        MessagePart messagePart = new MessagePart(MimeType.TEXT_PLAIN, tweet.getContent());
        message.addMessagePart(messagePart);

        this.userGlobalIds.add(message.getAuthorGlobalId());

        return message;
    }

    @Override
    protected void doInitalization() throws Exception {

        try {

            LOGGER.debug("Do initalization ...");
            umap2011TweetAccess = SqlUmap2011TweetAccess.getDefaultInstance();

            constructMapsAndLists();

            TweetFilter tweetFilter = new TweetFilter();
            tweetFilter.minDate = this.getStartDate();
            tweetFilter.maxDate = this.getEndDate();

            LOGGER.debug("Determining tweet count ...");
            long count = umap2011TweetAccess.getTweetCount(tweetFilter);

            LOGGER.debug("Now reading tweets {} from db ...", count);
            List<Tweet> tweets = umap2011TweetAccess.readTweets(tweetFilter);

            LOGGER.debug("Finished reading tweets {} from db ...", tweets.size());
            for (Tweet tweet : tweets) {
                Message message = createMessage(tweet);
                this.addMessage(message);
            }
            LOGGER.debug("Finished converting tweets to {} messages  from db ...",
                    this.messages.size());

            java.util.Collections.sort(this.messages, MessagePublicationDateComperator.INSTANCE);

            Message first = this.messages.get(0);
            Message last = this.messages.get(this.messages.size() - 1);
            LOGGER.debug("First message is " + first);
            LOGGER.debug("Last message is " + last);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeSqlAccess();
        }

        LOGGER.debug("Loaded ratings=" + ratings.size());
        LOGGER.debug("Loaded messages=" + messages.size());
        LOGGER.debug("Loaded users=" + userGlobalIds.size());

    }

    public String getName() {
        return "Umap2011";
    }

    @Override
    protected void loadMessages() {
        // nothing to to do
    }

    @Override
    protected void loadRatings() {
        // nothing to to do
    }

    @Override
    protected void loadUsers() {
        // nothing to to do
    }

}
