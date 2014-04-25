package de.spektrumprojekt.i.evaluation.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.spektrumprojekt.i.evaluation.configuration.Configuration;

/**
 * 
 * @author tlu
 * 
 */
public class FileStorageAccess extends MapStorageAccess {

    private final String storageFilename;

    private final static Logger LOGGER = LoggerFactory.getLogger(FileStorageAccess.class);

    public FileStorageAccess() {
        this(Configuration.INSTANCE.getFileStorageAccessStorageName());
    }

    public FileStorageAccess(String storageFilename) {
        if (storageFilename == null) {
            throw new IllegalArgumentException("storageFilename cannot be null");
        }
        this.storageFilename = storageFilename;
    }

    @Override
    public synchronized void init() throws Exception {
        super.init();

        InputStream input = null;
        ObjectInput objectInput = null;
        try {
            File file = new File(storageFilename);
            if (file.exists()) {

                input = new FileInputStream(file);
                objectInput = new ObjectInputStream(new BufferedInputStream(input));

                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) objectInput.readObject();
                this.getStorage().putAll(map);

            }
            this.setDirty(false);
        } finally {
            if (objectInput != null) {
                objectInput.close();
            }
            IOUtils.closeQuietly(input);
        }
    }

    @Override
    public synchronized void shutdown() throws Exception {
        Map<String, Object> storage = this.getStorage();

        if (!this.isDirty()) {
            LOGGER.debug("{} Objects in storage are not dirty and will not be written (again).",
                    storage.size());
            return;
        }

        OutputStream output = null;
        ObjectOutput objectOutput = null;
        try {

            File file = new File(this.storageFilename);

            LOGGER.debug("Writing {} objects to {} ...", storage.size(), this.storageFilename);

            output = new FileOutputStream(file);
            objectOutput = new ObjectOutputStream(new BufferedOutputStream(output));
            objectOutput.writeObject(storage);
            this.setDirty(false);
        } finally {
            if (objectOutput != null) {
                objectOutput.close();
            }
            IOUtils.closeQuietly(output);
        }
        LOGGER.debug("Wrote {} objects to {} ...", storage.size(), this.storageFilename);

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
