package de.spektrumprojekt.datamodel.subscription;

/**
 * 
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 * 
 */
public class SubscriptionAlreadyExistsException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final String subscriptionGlobalId;
    private final Subscription subscription;

    public SubscriptionAlreadyExistsException(String subscriptionGlobalId, Subscription subscription) {
        this.subscriptionGlobalId = subscriptionGlobalId;
        this.subscription = subscription;
    }

    @Override
    public String getMessage() {
        return subscriptionGlobalId + " " + subscription;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SubscriptionAlreadyExistsException [subscriptionGlobalId=");
        builder.append(subscriptionGlobalId);
        builder.append(", subscription=");
        builder.append(subscription);
        builder.append(", getMessage()=");
        builder.append(getMessage());
        builder.append("]");
        return builder.toString();
    }
    /**
     * @return the global ID of the existing subscription
     */
    public String getSubscriptionGlobalId() {
        return subscriptionGlobalId;
    }
}
