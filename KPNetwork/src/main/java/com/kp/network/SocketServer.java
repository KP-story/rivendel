package com.kp.network;

import com.kp.common.data.message.IMessage;
import com.kp.network.connection.IConnection;
import com.kp.network.event.impl.ConnectionListener;

import java.net.InetAddress;
import java.util.concurrent.Future;

public interface SocketServer<T extends IMessage> {


    void removeRemoteConnection(IConnection iConnection) throws Exception;

    void addConnectionListener(String id, ConnectionListener<T> connectionListener) throws Exception;

    void remAllConnectionListener() throws Exception;

    void broadcastMessage(T message) throws Exception;

    Future init() throws Exception;

    Future destroy() throws Exception;

    void addLocalPort(int port);

    void addLocalAddress(InetAddress address);

    int getLocalPort();

    InetAddress[] getLocalAddress();

    boolean isActive();

    void setNboot(int nboot);


    void setNworker(int nworker);


    void setRcvbuf(int rcvbuf);


    void setTimeout(int timeout);

}
