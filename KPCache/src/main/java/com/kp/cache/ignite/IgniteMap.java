package com.kp.cache.ignite;

import com.kp.cache.EntryEvent;
import com.kp.cache.KMap;
import com.kp.cache.MapListener;
import org.apache.ignite.IgniteCache;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class IgniteMap<K, V> implements KMap<K, V> {

    IgniteCache<K, V> content;

    public IgniteMap(IgniteCache<K, V> content, boolean isAsync) {
        this.content = isAsync ? content.withAsync() : content;
    }

    @Override
    public int size() {

        return content.size();
    }

    @Override
    public boolean isEmpty() {
        return content.size() <= 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return content.containsKey((K) key);
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public V get(Object key) {
        return content.get((K) key);
    }

    @Override
    public Object put(Object key, Object value) {
        return content.getAndPut((K) key, (V) value);
    }


    @Override
    public V remove(Object key) {
        return content.getAndRemove((K) key);
    }

    @Override
    public void putAll(@NotNull Map m) {
        content.putAll(m);

    }


    @Override
    public void clear() {
        content.clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return null;
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return null;
    }

    @NotNull
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return null;
    }

    @Override
    public List<V> loadAll() throws IOException {
        return null;
    }

    @Override
    public void putTransient(Object key, Object value, long ttl, TimeUnit timeunit) {
    }

    @Override
    public void set(K key, V value) {
        content.put(key, value);
    }

    @Override
    public String addEntryListener(MapListener listener, boolean includeValue, EntryEvent.EventType eventType) {
        return null;
    }

}
