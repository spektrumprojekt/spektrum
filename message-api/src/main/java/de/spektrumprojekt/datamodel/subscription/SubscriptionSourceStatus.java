package de.spektrumprojekt.datamodel.subscription;

import de.spektrumprojekt.datamodel.source.SourceStatus;

/**
 * Object that associates a Subscription with the SourceStatus of the Source of the Subscription.
 *
 * @author Communote GmbH - <a href="http://www.communote.de/">http://www.communote.com/</a>
 */
 public class SubscriptionSourceStatus {
 
    private final Subscription subscription;
    private final SourceStatus sourceStatus;
    /**
     * Constructor
     * @param subscription The subscription to associate with its Source SourceStatus
     * @param sourceStatus The status of the source of the provided subscription
     */
	public SubscriptionSourceStatus(Subscription subscription, SourceStatus sourceStatus) {
        this.subscription = subscription;
        this.sourceStatus = sourceStatus;
	}
    
    /**
     * @return the contained subscription
     */
    public Subscription getSubscription() {
        return this.subscription;
    }
    
    /**
     * @return the status of the source of the contained subscription
     */
    public SourceStatus getSourceStatus() {
        return this.sourceStatus;
    }
 }