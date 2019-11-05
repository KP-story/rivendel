package com.kp.network.connection;

import com.kp.common.data.message.IMessage;
import com.kp.network.IObjectManager;

import java.io.IOException;

public interface IConnectionManager<T extends IMessage, Y> extends IObjectManager<String, IConnection<T, Y>> {
    void disconnectAll() throws IOException;

    void broadcastMessage(T message) throws IOException;

}
