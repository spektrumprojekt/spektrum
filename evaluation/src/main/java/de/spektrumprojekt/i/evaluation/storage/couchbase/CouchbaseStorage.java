package de.spektrumprojekt.i.evaluation.storage.couchbase;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.internal.GetFuture;
import net.spy.memcached.internal.OperationFuture;

import org.apache.log4j.Logger;

import com.couchbase.client.CouchbaseClient;

public class CouchbaseStorage {

    private final static Logger LOGGER = Logger.getLogger(CouchbaseStorage.class);

    private String serverUrl = "http://127.0.0.1:8091/pools";
    private String bucket = "default";
    private String password = "";
    private CouchbaseClient couchbaseClient;

    public Object get(String key) {
        Object getObject = null;

        // Do an asynchronous get
        GetFuture getOp = couchbaseClient.asyncGet(key);
        // Check to see if ayncGet succeeded
        try {
            if ((getObject = getOp.get()) != null) {
                LOGGER.trace("Asynchronous Get Succeeded.");
            } else {
                LOGGER.error("Asynchronous Get failed: " + getOp.getStatus().getMessage());
            }
        } catch (Exception e) {
            LOGGER.error("Exception while doing Aynchronous Get: " + e.getMessage());
        }
        return getObject;
    }

    public synchronized void init() throws Exception {

        if (couchbaseClient != null) {
            this.shutdown();
        }
        List<URI> uris = new LinkedList<URI>();

        // Connect to localhost or to the appropriate URI
        uris.add(URI.create(serverUrl));

        try {
            couchbaseClient = new CouchbaseClient(uris, bucket, password);
        } catch (Exception e) {
            LOGGER.error("Error connecting to Couchbase: " + e.getMessage());
            throw e;
        }
    }

    public synchronized void shutdown() {
        if (couchbaseClient != null) {
            // Shutdown the client
            couchbaseClient.shutdown(3, TimeUnit.SECONDS);
            couchbaseClient = null;
        }
    }

    public void store(String key, Object value) {
        // Do an asynchronous set
        OperationFuture<Boolean> setOp = couchbaseClient.set(key, 0, value);

        // Now we want to see what happened with our data
        // Check to see if our set succeeded
        try {
            if (setOp.get().booleanValue()) {
                LOGGER.trace("Set Succeeded");
            } else {
                LOGGER.error("Set failed: " + setOp.getStatus().getMessage());
            }
        } catch (Exception e) {
            LOGGER.error("Exception while doing set: " + e.getMessage());
        }
    }

}
