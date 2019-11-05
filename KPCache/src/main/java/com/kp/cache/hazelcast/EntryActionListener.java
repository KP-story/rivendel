package com.kp.cache.hazelcast;

import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryExpiredListener;
import com.hazelcast.map.listener.EntryRemovedListener;

public interface EntryActionListener <K, V> extends EntryAddedListener<K, V>,EntryExpiredListener<K, V>, EntryRemovedListener<K, V>,EntryEvictedListener<K, V> {
}
