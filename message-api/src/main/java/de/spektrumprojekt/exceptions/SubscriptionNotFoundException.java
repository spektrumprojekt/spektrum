package de.spektrumprojekt.exceptions;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class SubscriptionNotFoundException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String subscriptionGlobalId;

    public SubscriptionNotFoundException(String message, String subscriptionGlobalId) {
        super(message);
        this.subscriptionGlobalId = subscriptionGlobalId;
    }

    public String getSubscriptionGlobalId() {
        return subscriptionGlobalId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SubscriptionNotFoundException [subscriptionGlobalId=");
        builder.append(subscriptionGlobalId);
        builder.append(", getMessage()=");
        builder.append(getMessage());
        builder.append("]");
        return builder.toString();
    }
}