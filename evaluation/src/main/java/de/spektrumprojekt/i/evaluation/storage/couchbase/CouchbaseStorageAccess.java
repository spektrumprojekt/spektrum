package de.spektrumprojekt.i.evaluation.storage.couchbase;

import de.spektrumprojekt.i.evaluation.storage.StorageAccess;

public class CouchbaseStorageAccess implements StorageAccess {

    private CouchbaseStorage couchbaseStorage;

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.communote.plugins.mystream.evaluation.storage.StorageAccess#getObject(java.lang.String)
     */
    public Object getObject(String key) {
        return this.couchbaseStorage.get(key);
    }

    public synchronized void init() throws Exception {
        if (couchbaseStorage != null) {
            couchbaseStorage.shutdown();
            couchbaseStorage = null;
        }
        CouchbaseStorage couchbaseStorage = new CouchbaseStorage();
        couchbaseStorage.init();
        this.couchbaseStorage = couchbaseStorage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.communote.plugins.mystream.evaluation.storage.StorageAccess#shutdown()
     */
    public synchronized void shutdown() {
        if (this.couchbaseStorage != null) {
            this.couchbaseStorage.shutdown();
            this.couchbaseStorage = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.communote.plugins.mystream.evaluation.storage.StorageAccess#storeObject(java.lang.String,
     * java.lang.Object)
     */
    public void storeObject(String key, Object value) {
        this.couchbaseStorage.store(key, value);
    }

}
