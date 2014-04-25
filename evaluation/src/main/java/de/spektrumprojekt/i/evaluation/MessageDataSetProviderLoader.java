package de.spektrumprojekt.i.evaluation;

import java.util.Date;

public class MessageDataSetProviderLoader<P extends MessageDataSetProvider> {

    private final Class<P> dataSetProviderClass;

    public MessageDataSetProviderLoader(Class<P> dataSetProviderClass) {
        this.dataSetProviderClass = dataSetProviderClass;
    }

    public P getMessageDataSetProvider() {
        return this.getMessageDataSetProvider(null, null);
    }

    public P getMessageDataSetProvider(Date startDate, Date endDate) {
        P provider;
        try {
            provider = dataSetProviderClass.newInstance();
            provider.setStartDate(startDate);
            provider.setEndDate(endDate);

        } catch (InstantiationException e) {
            throw new RuntimeException("Error creation dataset provider " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error creation dataset provider " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error init dataset provider " + e.getMessage(), e);
        }
        return provider;
    }
}