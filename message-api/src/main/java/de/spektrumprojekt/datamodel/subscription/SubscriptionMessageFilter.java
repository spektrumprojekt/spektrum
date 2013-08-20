package de.spektrumprojekt.datamodel.subscription;

import java.io.Serializable;
import java.util.Date;

/**
 * This non entity filter defines filter criterias once a new subscriptions is created and already
 * existing messages should be returned.
 * 
 * If both values lastXMessages and startDate are defined it will be combined by OR. For example if
 * lastXMessages is 10 and startDate is 2013-01-01 than at least 10 messages are returned plus all
 * message since january 2013.
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class SubscriptionMessageFilter implements Serializable {

    /**
     * Filter that will match no message, hence return nothing on a new subscription creation
     */
    public final static SubscriptionMessageFilter NONE = new SubscriptionMessageFilter();

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private int lastXMessages;

    private Date startDate;

    public SubscriptionMessageFilter() {
        this(0, null);
    }

    public SubscriptionMessageFilter(int lastXMessages, Date startDate) {
        if (lastXMessages < 0) {
            throw new IllegalArgumentException("lastXMessages must >= 0 but is " + lastXMessages);
        }
        this.lastXMessages = lastXMessages;
        this.startDate = startDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SubscriptionMessageFilter other = (SubscriptionMessageFilter) obj;
        if (lastXMessages != other.lastXMessages) {
            return false;
        }
        if (startDate == null) {
            if (other.startDate != null) {
                return false;
            }
        } else if (!startDate.equals(other.startDate)) {
            return false;
        }
        return true;
    }

    /**
     * if the subscriptions is registered also send the last x messages back defined by this value
     * 
     * @return last x messages to return
     */
    public int getLastXMessages() {
        return lastXMessages;
    }

    /**
     * if the subscriptions is registered also send the messages what have been created after this
     * date
     * 
     * @return the day of the oldest message to return. null to ignore this value.
     */
    public Date getStartDate() {
        return startDate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + lastXMessages;
        result = prime * result + (startDate == null ? 0 : startDate.hashCode());
        return result;
    }

    public void setLastXMessages(int lastXMessages) {
        this.lastXMessages = lastXMessages;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SubscriptionMessageFilter [lastXMessages=");
        builder.append(lastXMessages);
        builder.append(", startDate=");
        builder.append(startDate);
        builder.append("]");
        return builder.toString();
    }
}