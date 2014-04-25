package de.spektrumprojekt.i.evaluation.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.i.evaluation.configuration.Configuration;

public class StorageAccessFactory {

    private final static Logger LOGGER = LoggerFactory.getLogger(StorageAccessFactory.class);

    public final static StorageAccessFactory INSTANCE = new StorageAccessFactory();

    public StorageAccess createStorageAccess() throws Exception {
        String className = Configuration.INSTANCE.getStorageAccessClassName();

        if (className == null) {
            throw new IllegalArgumentException(
                    "className cannot be null. Check your configuration "
                            + Configuration.COMMUNOTE_MYSTREAM_EVALUATION_STORAGE_ACCESS);
        }

        Class<StorageAccess> clazz = (Class<StorageAccess>) Thread.currentThread()
                .getContextClassLoader()
                .loadClass(className);

        LOGGER.info("Using storageAccess clazz=" + clazz.getName());
        StorageAccess storageAccess = clazz.newInstance();

        LOGGER.info("Created storageAccess: " + storageAccess);
        return storageAccess;
    }

}