package com.kp.network.event.impl;

import com.kp.common.data.message.IMessage;
import com.kp.network.connection.IConnection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultConnectionListenerManager<T extends IMessage> implements IConnectionListenerManager<T> {
    private Map<String, ConnectionListener<T>> listeners;


    @Override
    public void add(String id, ConnectionListener<T> object) throws Exception {
        listeners.put(id, object);
    }

    @Override
    public ConnectionListener<T> get(String id) throws Exception {
        return listeners.get(id);
    }

    @Override
    public boolean contains(String id) throws Exception {
        return listeners.containsKey(id);
    }

    @Override
    public boolean containsAndRemove(String id) {
        return false;
    }

    @Override
    public ConnectionListener<T> remove(String id) throws Exception {
        return listeners.remove(id);
    }

    @Override
    public void destroy() throws Exception {
        if (listeners != null) {
            listeners.clear();
            listeners = null;
        }
    }


    @Override
    public void init() throws Exception {
        listeners = new ConcurrentHashMap<>();

    }


    @Override
    public void fireConnectionOpened(IConnection connection) {

        listeners.forEach((s, tConnectionListener) -> {
            try {
                tConnectionListener.connectionOpened(connection);
            } catch (Exception e) {
            }
        });

    }

    @Override
    public void fireConnectionClosed(IConnection connection) {
        listeners.forEach((s, tConnectionListener) -> {
            try {
                tConnectionListener.connectionClosed(connection);
            } catch (Exception e) {
            }
        });

    }

    @Override
    public void fireMessageReceived(IConnection connection, T message) {
        listeners.forEach((s, tConnectionListener) -> {
            try {
                tConnectionListener.messageReceived(connection, message);
            } catch (Exception e) {
            }
        });

    }

    @Override
    public void fireInternalError(IConnection connection, T message, Throwable cause) {
        listeners.forEach((s, tConnectionListener) -> {
            try {
                tConnectionListener.internalError(connection, message, cause);
            } catch (Exception e) {
            }
        });

    }

    @Override
    public void removeAll() throws Exception {
        listeners.clear();
    }
}
