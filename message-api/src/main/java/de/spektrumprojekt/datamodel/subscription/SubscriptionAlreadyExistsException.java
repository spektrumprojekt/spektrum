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

    private final String globalId;
    private final Subscription subscription;

    public SubscriptionAlreadyExistsException(String globalId, Subscription subscription) {
        this.globalId = globalId;
        this.subscription = subscription;
    }

    @Override
    public String getMessage() {
        return globalId + " " + subscription;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SubscriptionAlreadyExistsException [globalId=");
        builder.append(globalId);
        builder.append(", subscription=");
        builder.append(subscription);
        builder.append(", getMessage()=");
        builder.append(getMessage());
        builder.append("]");
        return builder.toString();
    }

}
