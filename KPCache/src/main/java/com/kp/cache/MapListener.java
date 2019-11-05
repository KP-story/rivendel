package com.kp.cache;

import java.util.EventListener;

public interface MapListener<K, V> extends EventListener {

    public void onEvent(EntryEvent<K, V> entryEvent);
}
