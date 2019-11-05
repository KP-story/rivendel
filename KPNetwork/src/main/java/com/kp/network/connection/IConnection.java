package com.kp.network.connection;

import com.kp.common.data.message.IMessage;
import com.kp.network.event.impl.ConnectionListener;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.Future;

public interface IConnection<T extends IMessage, Y> {


    /**
     * Return created time
     *
     * @return created time
     */
    long getCreatedTime();

    /**
     * Return identifier of connection. For example:
     * "[remote_host_name]:[remote_port]"
     *
     * @return identifier of connection.
     */
    String getId();

    /**
     * Connect with remote host
     */

    Future connect() throws IOException, InterruptedException;

    void onConnected(Y context) throws Exception;

    /**
     * Disconnect wit remote host
     *
     * @throws InternalError
     */
    Future disconnect() throws IOException, InterruptedException;


    /**
     * Clear all attachec resources (close socket)
     *
     * @throws IOException
     */
    Future release() throws IOException;

    /**
     * Return true if connection is incomming
     *
     * @return true if connection is incomming
     */
    boolean isNetworkInitiated();

    /**
     * Return true if is connection is valid
     *
     * @return true if is connection is valid
     */
    boolean isConnected();

    boolean isActive();


    /**
     * Append connection listener
     * @param connectionListener listener instance
     */

    /**
     * Remove all connection listeners
     */
    void remAllConnectionListener() throws Exception;

    Future send(T message) throws IOException;

    void sendNotify(T message) throws Exception;

    Future<T> sendSync(T message) throws Exception;

    Future<T> sendAsync(T message) throws Exception;

    void addConnectionListener(String id, ConnectionListener<T> connectionListener) throws Exception;

    void addLocalAddress(SocketAddress socketAddress);

    void addRemoteAddress(SocketAddress socketAddress);

    int getTimeout();

    void setTimeout(int timeout);

    void setNWorker(int i);

    SocketAddress getRemoteAddress();

    SocketAddress[] getLocalAddress();

    void remConnectionListener(String id) throws Exception;

    void fireConnectionOpened(IConnection connection);


    void fireConnectionClosed(IConnection connection);


    void fireMessageReceived(IConnection connection, T message);


    void fireInternalError(IConnection connection, T message, Throwable cause);


}
