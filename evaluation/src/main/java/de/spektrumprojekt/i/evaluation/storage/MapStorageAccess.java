package de.spektrumprojekt.i.evaluation.storage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.i.evaluation.configuration.Configuration;

public class MapStorageAccess implements StorageAccess {

    private Map<String, Object> storage = new HashMap<String, Object>();

    private final String storageFilename;

    private final static Logger LOGGER = LoggerFactory.getLogger(MapStorageAccess.class);

    private boolean dirty;

    public MapStorageAccess() {
        this(Configuration.INSTANCE.getFileStorageAccessStorageName());
    }

    public MapStorageAccess(String storageFilename) {
        if (storageFilename == null) {
            throw new IllegalArgumentException("storageFilename cannot be null");
        }
        this.storageFilename = storageFilename;
    }

    public Object getObject(String key) {
        return this.storage.get(key);
    }

    protected Map<String, Object> getStorage() {
        return storage;
    }

    public synchronized void init() throws Exception {
        dirty = false;
        this.storage.clear();
    }

    public boolean isDirty() {
        return dirty;
    }

    protected void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public synchronized void shutdown() throws Exception {
        OutputStream output = null;
        ObjectOutput objectOutput = null;
        try {
            File file = new File(this.storageFilename);

            LOGGER.debug("Writing {} objects to {} ...", this.storage.size(), this.storageFilename);

            output = new FileOutputStream(file);
            objectOutput = new ObjectOutputStream(new BufferedOutputStream(output));
            objectOutput.writeObject(this.storage);

        } finally {
            if (objectOutput != null) {
                objectOutput.close();
            }
            IOUtils.closeQuietly(output);
        }
        LOGGER.debug("Wrote {} objects to {} ...", this.storage.size(), this.storageFilename);

    }

    public void storeObject(String key, Object value) {
        dirty = true;
        this.storage.put(key, value);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FileStorageAccess [storageFilename=");
        builder.append(storageFilename);
        builder.append("]");
        return builder.toString();
    }

}
