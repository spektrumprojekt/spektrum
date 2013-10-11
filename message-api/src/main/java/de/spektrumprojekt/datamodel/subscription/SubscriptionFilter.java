package de.spektrumprojekt.datamodel.subscription;

import de.spektrumprojekt.datamodel.common.Property;

public class SubscriptionFilter {

    private String sourceGlobalId;

    private Property subscriptionProperty;

    public String getSourceGlobalId() {
        return sourceGlobalId;
    }

    public Property getSubscriptionProperty() {
        return subscriptionProperty;
    }

    public void setSourceGlobalId(String sourceGlobalId) {
        this.sourceGlobalId = sourceGlobalId;
    }

    /**
     * Usage setSubscriptionProperty(new Property("keyToMatch", "valueToMatch")
     * 
     * @param subscriptionProperty
     *            the property that should match. it will only take the key and value of the
     *            property
     */
    public void setSubscriptionProperty(Property subscriptionProperty) {
        this.subscriptionProperty = subscriptionProperty;
    }

}
