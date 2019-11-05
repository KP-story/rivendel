package com.kp.cache.hazelcast;

import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryExpiredListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.kp.cache.EntryEvent;
import com.kp.cache.KMap;
import com.kp.cache.MapListener;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.kp.cache.EntryEvent.EventType.*;

public class HazelcastMap<K, V> implements KMap<K, V> {
    IMap<K, V> content;

    public HazelcastMap(IMap<K, V> content) {
        this.content = content;
    }

    @Override
    public int size() {

        return content.size();
    }

    @Override
    public List<V> loadAll() throws IOException {
        content.loadAll(true);
        return null;
    }

    @Override
    public void putTransient(K key, V value, long ttl, TimeUnit timeunit) {
        content.putTransient(key, value, ttl, timeunit);
    }

    @Override
    public void set(K key, V value) {
        content.set(key, value);
    }

    @Override
    public String addEntryListener(MapListener listener, boolean includeValue, EntryEvent.EventType eventType) {
        com.hazelcast.map.listener.MapListener mapListener;
        switch (eventType) {
            case EVT_CACHE_OBJECT_EXPIRED: {
                mapListener = new EntryExpiredListener<K, V>() {
                    @Override
                    public void entryExpired(com.hazelcast.core.EntryEvent<K, V> event) {
                        EntryEvent<K, V> entryEvent = new EntryEvent<>();
                        entryEvent.setEventType(EVT_CACHE_OBJECT_EXPIRED);
                        entryEvent.setKey(event.getKey());
                        entryEvent.setValue(event.getValue());
                        listener.onEvent(entryEvent);
                    }
                };
            }
            break;

            case EVT_CACHE_OBJECT_PUT: {
                mapListener = new EntryAddedListener<K, V>() {
                    @Override
                    public void entryAdded(com.hazelcast.core.EntryEvent<K, V> event) {

                        EntryEvent<K, V> entryEvent = new EntryEvent<>();
                        entryEvent.setEventType(EVT_CACHE_OBJECT_PUT);
                        entryEvent.setKey(event.getKey());
                        entryEvent.setValue(event.getValue());
                        listener.onEvent(entryEvent);
                    }
                };
            }
            break;

            case EVT_CACHE_OBJECT_REMOVED: {
                mapListener = new EntryRemovedListener<K, V>() {
                    @Override
                    public void entryRemoved(com.hazelcast.core.EntryEvent<K, V> event) {
                        EntryEvent<K, V> entryEvent = new EntryEvent<>();
                        entryEvent.setEventType(EVT_CACHE_OBJECT_REMOVED);
                        entryEvent.setKey(event.getKey());
                        entryEvent.setValue(event.getValue());
                        listener.onEvent(entryEvent);
                    }
                };
            }
            break;
            case EVT_CACHE_OBJECT_EVICTED: {
                mapListener = new EntryEvictedListener<K, V>() {
                    @Override
                    public void entryEvicted(com.hazelcast.core.EntryEvent<K, V> event) {
                        EntryEvent<K, V> entryEvent = new EntryEvent<>();
                        entryEvent.setEventType(EVT_CACHE_OBJECT_EVICTED);
                        entryEvent.setKey(event.getKey());
                        entryEvent.setValue(event.getValue());
                        listener.onEvent(entryEvent);
                    }
                };
            }
            break;


            default:
                mapListener = new EntryActionListener<K, V>() {
                    @Override
                    public void entryRemoved(com.hazelcast.core.EntryEvent<K, V> event) {
                        EntryEvent<K, V> entryEvent = new EntryEvent<>();
                        entryEvent.setEventType(EVT_CACHE_OBJECT_REMOVED);
                        entryEvent.setKey(event.getKey());
                        entryEvent.setValue(event.getValue());
                        listener.onEvent(entryEvent);
                    }

                    @Override
                    public void entryExpired(com.hazelcast.core.EntryEvent<K, V> event) {
                        EntryEvent<K, V> entryEvent = new EntryEvent<>();
                        entryEvent.setEventType(EVT_CACHE_OBJECT_EXPIRED);
                        entryEvent.setKey(event.getKey());
                        entryEvent.setValue(event.getValue());
                        listener.onEvent(entryEvent);
                    }

                    @Override
                    public void entryAdded(com.hazelcast.core.EntryEvent<K, V> event) {
                        EntryEvent<K, V> entryEvent = new EntryEvent<>();
                        entryEvent.setEventType(EVT_CACHE_OBJECT_PUT);
                        entryEvent.setKey(event.getKey());
                        entryEvent.setValue(event.getValue());
                        listener.onEvent(entryEvent);
                    }

                    @Override
                    public void entryEvicted(com.hazelcast.core.EntryEvent<K, V> event) {
                        EntryEvent<K, V> entryEvent = new EntryEvent<>();
                        entryEvent.setEventType(EVT_CACHE_OBJECT_EVICTED);
                        entryEvent.setKey(event.getKey());
                        entryEvent.setValue(event.getValue());
                        listener.onEvent(entryEvent);
                    }
                };
                break;




        }

        return content.addEntryListener(mapListener, includeValue);
    }


    @Override
    public boolean isEmpty() {
        return content.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return content.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return content.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return content.get(key);
    }

    @Override
    public V put(K key, V value) {
        return content.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return content.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        content.putAll(m);
    }

    @Override
    public void clear() {
        content.clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return content.keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return content.values();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return content.entrySet();
    }
}
