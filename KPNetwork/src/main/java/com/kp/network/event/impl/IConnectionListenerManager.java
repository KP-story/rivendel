package com.kp.network.event.impl;

import com.kp.common.data.message.IMessage;
import com.kp.network.connection.IConnection;
import com.kp.network.event.EventListenerManager;


public interface IConnectionListenerManager<T extends IMessage> extends EventListenerManager<String, ConnectionListener<T>> {
    void fireConnectionOpened(IConnection connection);


    void fireConnectionClosed(IConnection connection);


    void fireMessageReceived(IConnection connection, T message);


    void fireInternalError(IConnection connection, T message, Throwable cause);
}
