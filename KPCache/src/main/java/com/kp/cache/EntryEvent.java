package com.kp.cache;

public class EntryEvent<K, V> {
    private static final long serialVersionUID = -2296203982913729851L;
    protected K key;
    protected V oldValue;
    protected V value;
    protected V mergingValue;
    protected EventType eventType;

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public K getKey() {
        return this.key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getOldValue() {
        return this.oldValue;
    }

    public void setOldValue(V oldValue) {
        this.oldValue = oldValue;
    }

    public V getValue() {
        return this.value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public V getMergingValue() {
        return this.mergingValue;
    }

    public void setMergingValue(V mergingValue) {
        this.mergingValue = mergingValue;
    }

    public String toString() {
        return "EntryEvent{" + super.toString() + ", key=" + this.getKey() + ", oldValue=" + this.getOldValue() + ", value=" + this.getValue() + ", mergingValue=" + this.getMergingValue() + '}';
    }

    public enum EventType {
        EVT_CACHE_OBJECT_PUT, EVT_CACHE_OBJECT_READ, EVT_CACHE_OBJECT_REMOVED, EVT_CACHE_OBJECT_LOCKED, EVT_CACHE_OBJECT_UNLOCKED, EVT_CACHE_OBJECT_EXPIRED, EVT_CACHE_OBJECT_EVICTED;
    }
}
