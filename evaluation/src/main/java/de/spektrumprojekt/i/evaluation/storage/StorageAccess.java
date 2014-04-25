package de.spektrumprojekt.i.evaluation.storage;

public interface StorageAccess {

    public abstract Object getObject(String key);

    public abstract void init() throws Exception;

    public abstract void shutdown() throws Exception;

    public abstract void storeObject(String key, Object value);

}