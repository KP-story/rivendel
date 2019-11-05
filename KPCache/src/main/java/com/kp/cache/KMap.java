package com.kp.cache;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface KMap<K, V> extends Map<K, V> {

    List<V> loadAll() throws IOException;

    void putTransient(K key, V value, long ttl, TimeUnit timeunit);

    void set(K key, V value);

    String addEntryListener(MapListener listener, boolean includeValue, EntryEvent.EventType eventType);

}
