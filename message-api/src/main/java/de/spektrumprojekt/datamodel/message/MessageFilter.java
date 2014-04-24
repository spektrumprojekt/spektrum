package de.spektrumprojekt.datamodel.message;

import java.util.Date;

public class MessageFilter {

    public static enum OrderDirection {
        DESC,
        ASC;
    }

    private OrderDirection publicationDateOrderDirection;
    private OrderDirection messageIdOrderDirection;

    private String messageGroupGlobalId;

    private String sourceGlobalId;

    private Date minPublicationDate;

    private int lastMessagesCount;

    private String pattern;

    public int getLastMessagesCount() {
        return lastMessagesCount;
    }

    public String getMessageGroupGlobalId() {
        return messageGroupGlobalId;
    }

    public OrderDirection getMessageIdOrderDirection() {
        return messageIdOrderDirection;
    }

    public Date getMinPublicationDate() {
        return minPublicationDate;
    }

    public String getPattern() {
        return pattern;
    }

    public OrderDirection getPublicationDateOrderDirection() {
        return publicationDateOrderDirection;
    }

    public String getSourceGlobalId() {
        return sourceGlobalId;
    }

    public void setLastMessagesCount(int lastMessagesCount) {
        this.lastMessagesCount = lastMessagesCount;
    }

    public void setMessageGroupGlobalId(String messageGroupGlobalId) {
        this.messageGroupGlobalId = messageGroupGlobalId;
    }

    public void setMessageIdOrderDirection(OrderDirection messageIdOrderDirection) {
        this.messageIdOrderDirection = messageIdOrderDirection;
    }

    public void setMinPublicationDate(Date minPublicationDate) {
        this.minPublicationDate = minPublicationDate;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setPublicationDateOrderDirection(OrderDirection publicationDateOrderDirection) {
        this.publicationDateOrderDirection = publicationDateOrderDirection;
    }

    public void setSourceGlobalId(String sourceGlobalId) {
        this.sourceGlobalId = sourceGlobalId;
    }

}
