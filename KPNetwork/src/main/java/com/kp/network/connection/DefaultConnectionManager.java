package com.kp.network.connection;

import com.kp.common.data.message.IMessage;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultConnectionManager<T extends IMessage, Y> implements IConnectionManager<T, Y> {
    Map<String, IConnection<T, Y>> connections;

    @Override
    public void add(String id, IConnection<T, Y> object) throws Exception {
        connections.put(id, object);
    }

    @Override
    public IConnection<T, Y> get(String id) throws Exception {
        return connections.get(id);
    }

    @Override
    public boolean contains(String id) throws Exception {
        return connections.containsKey(id);
    }

    @Override
    public boolean containsAndRemove(String id) {
        return false;
    }

    @Override
    public IConnection<T, Y> remove(String id) throws Exception {
        return connections.remove(id);
    }

    @Override
    public void destroy() throws Exception {
        disconnectAll();
        connections.clear();

    }

    @Override
    public void init() throws Exception {
        connections = new ConcurrentHashMap<>();
    }

    @Override
    public void removeAll() throws Exception {
        connections.clear();
    }

    @Override
    public void disconnectAll() throws IOException {
        connections.forEach((s, iConnection) ->
                {
                    try {
                        iConnection.release();
                    } catch (Exception e) {
                        getLogger().error("disconnectAll {}", e);
                    }
                }
        );
    }

    @Override
    public void broadcastMessage(T message) throws IOException {
        connections.forEach((s, iConnection) ->
                {
                    try {
                        iConnection.send(message);
                    } catch (Exception e) {
                        getLogger().error("disconnectAll {}", e);
                    }
                }
        );
    }
}
