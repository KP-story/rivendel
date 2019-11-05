package com.kp.network;

import com.kp.common.log.Loggable;

public interface IObjectManager<K, T> extends Loggable {
    void add(K id, T object) throws Exception;

    T get(K id) throws Exception;

    boolean contains(K id) throws Exception;

    boolean containsAndRemove(K id);

    T remove(K id) throws Exception;

    void destroy() throws Exception;

    void init() throws Exception;

    void removeAll() throws Exception;

}
