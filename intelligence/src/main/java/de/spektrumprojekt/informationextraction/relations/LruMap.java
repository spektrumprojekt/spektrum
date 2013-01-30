package de.spektrumprojekt.informationextraction.relations;

import java.util.LinkedHashMap;

public class LruMap<K, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = 1L;
    private final int capacity;

    /**
     * @param capacity
     */
    public LruMap(int capacity) {
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
        return size() > capacity;
    }

}
