package de.spektrumprojekt.aggregator.adapter.twitter;

import org.apache.commons.lang3.Validate;

import twitter4j.Status;
import twitter4j.User;
import twitter4j.UserStreamAdapter;
import de.spektrumprojekt.commons.SpektrumUtils;
import de.spektrumprojekt.datamodel.common.MimeType;
import de.spektrumprojekt.datamodel.common.Property;
import de.spektrumprojekt.datamodel.message.Message;
import de.spektrumprojekt.datamodel.message.MessagePart;
import de.spektrumprojekt.datamodel.message.MessageType;
import de.spektrumprojekt.datamodel.source.SourceStatus;
import de.spektrumprojekt.datamodel.subscription.status.StatusType;

/**
 * Listener adapter for Twitter4j, responsible for transforming the {@link Status} items to
 * {@link Message}s.
 */
final class UserListener extends UserStreamAdapter {

    private final TwitterAdapter twitterAdapter;

    private final SourceStatus sourceStatus;

    UserListener(TwitterAdapter twitterAdapter, SourceStatus sourceStatus) {
        Validate.notNull(twitterAdapter, "twitterAdapter cannot be null");
        Validate.notNull(sourceStatus, "sourceStatus cannot be null");

        this.twitterAdapter = twitterAdapter;
        this.sourceStatus = sourceStatus;
    }

    @Override
    public void onException(Exception ex) {
        TwitterAdapter.LOGGER.warn("twitter exception " + ex);
        this.twitterAdapter.triggerTwitterListener(sourceStatus.getSource(),
                StatusType.ERROR_INTERNAL_ADAPTER, ex);
    }

    @Override
    public void onStatus(Status status) {
        TwitterAdapter.LOGGER.debug("status " + status);
        String text = status.getText();
        if (SpektrumUtils.notNullOrEmpty(text)) {
            Message message = new Message(MessageType.CONTENT,
                    StatusType.OK, sourceStatus.getSource()
                            .getGlobalId(), status.getCreatedAt());
            MessagePart messagePart = new MessagePart(MimeType.TEXT_PLAIN,
                    text);
            message.addMessagePart(messagePart);
            User user = status.getUser();
            if (user != null) {
                String screenName = user.getScreenName();
                message.addProperty(new Property(TwitterAdapter.SCREEN_NAME, screenName));
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
            message.addProperty(new Property(TwitterAdapter.STATUS_ID, statusId));
            message.addProperty(new Property("replyToStatusId",
                    replyToStatusId));
            this.twitterAdapter.addTwitterMessage(message);
        }
    }
}