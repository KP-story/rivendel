package com.kp.cache;

import com.kp.common.log.Loggable;
import com.kp.thread.ManageableObject;

public interface KCache extends Loggable, ManageableObject {
    boolean isAlive();

    <K, V> KMap<K, V> getMap(String name);
}
