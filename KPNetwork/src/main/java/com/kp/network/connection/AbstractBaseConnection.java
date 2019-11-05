package com.kp.network.connection;

import com.kp.common.data.message.IMessage;
import com.kp.common.log.Loggable;
import com.kp.network.FutureManager;
import com.kp.network.event.impl.ConnectionListener;
import com.kp.network.event.impl.IConnectionListenerManager;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractBaseConnection<T extends IMessage, Y> implements IConnection<T, Y>, Loggable {

    protected long createdTime;
    protected SocketAddress remoteAddress;
    protected int timeout = 20000;
    protected FutureManager<String, T> futureManager;
    protected String id;
    protected Y context;
    protected Future<Y> connectedFuture;
    protected IConnectionListenerManager<T> connectionListenerManager;
    private ReentrantLock lock = new ReentrantLock();

    public AbstractBaseConnection(IConnectionListenerManager<T> connectionListenerManager) {
        this.connectionListenerManager = connectionListenerManager;


        createFuture();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public FutureManager<String, T> getFutureManager() {
        return futureManager;
    }

    public IConnectionListenerManager<T> getConnectionListenerManager() {
        return connectionListenerManager;
    }

    protected abstract FutureManager<String, T> createFutureManager();

    protected Future createFuture() {
        return connectedFuture = new CompletableFuture<>();

    }

    @Override
    public Future connect() throws IOException, InterruptedException {
        try {
            lock.lock();

            if (connectedFuture.isCancelled() || connectedFuture.isDone()) {
                throw new RuntimeException("instance is created");
            } else {
                try {
                    _connect();
                    return connectedFuture;
                } catch (Exception e) {
                    connectedFuture.cancel(true);
                    throw e;
                }
            }
        } finally

        {
            lock.unlock();
        }


    }

    @Override
    public String getId() {
        return id;

    }

    @Override
    public void remConnectionListener(String id) throws Exception {
        connectionListenerManager.remove(id);
    }

    @Override
    public void fireConnectionOpened(IConnection connection) {
        connectionListenerManager.fireConnectionOpened(connection);
    }

    @Override
    public void fireConnectionClosed(IConnection connection) {
        connectionListenerManager.fireConnectionClosed(connection);

    }

    @Override
    public void fireMessageReceived(IConnection connection, T message) {
        connectionListenerManager.fireMessageReceived(connection, message);
    }

    @Override
    public void fireInternalError(IConnection connection, T message, Throwable cause) {
        connectionListenerManager.fireInternalError(connection, message, cause);
    }

    @Override
    public void remAllConnectionListener() throws Exception {
        connectionListenerManager.removeAll();

    }

    @Override
    public void addConnectionListener(String id, ConnectionListener<T> connectionListener) throws Exception {
        connectionListenerManager.add(id, connectionListener);
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    @Override
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public long getCreatedTime() {
        return createdTime;
    }

    protected abstract void _checkConnected() throws IOException;

    protected abstract Future _send(T message) throws IOException;

    protected abstract Future _connect() throws IOException, InterruptedException;

    protected abstract void _onConnected();

    @Override
    public boolean isActive() {
        try {
            connectedFuture.get(timeout, TimeUnit.MILLISECONDS);
            if (connectedFuture.isCancelled()) {
                return false;
            }
            _checkConnected();

            return true;
        } catch (Exception e) {
            getLogger().error("isConnected ", e);
        }
        return false;
    }

    @Override
    public boolean isConnected() {
        try {
            _checkConnected();
            connectedFuture.get(timeout, TimeUnit.MILLISECONDS);

            return true;
        } catch (Exception e) {
            getLogger().error("isConnected ", e);
        }
        return false;
    }

    @Override
    public SocketAddress getRemoteAddress() {

        return remoteAddress;
    }


    @Override
    public void onConnected(Y context) throws Exception {
        this.context = context;
        _onConnected();
        futureManager = createFutureManager();
        futureManager.init();
        futureManager.setMessageTimeout(timeout);
        CompletableFuture completableFuture = (CompletableFuture) connectedFuture;
        completableFuture.complete(context);
        createdTime = System.currentTimeMillis();
    }

    @Override
    public Future disconnect() throws IOException, InterruptedException {
        try {
            connectedFuture.cancel(true);
            futureManager.destroy();

        } catch (Exception e) {
            getLogger().error("disconnect ", e);
        }
        return null;
    }

    @Override
    public Future send(T message) throws IOException {

        if (isConnected()) {
            return _send(message);
        } else {
            throw new IOException("not connected");
        }
    }

    @Override
    public Future<T> sendSync(T message) throws Exception {
        CompletableFuture<T> completableFuture = new CompletableFuture<T>();
        futureManager.add(message.getId(), completableFuture);
        try {
            send(message).get(timeout, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            futureManager.remove(message.getId());
            throw e;
        }

        return completableFuture;
    }

    @Override
    public void sendNotify(T message) throws Exception {
        send(message).get(timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public Future<T> sendAsync(T message) throws Exception {
        CompletableFuture<T> completableFuture = new CompletableFuture<T>();
        futureManager.add(message.getId(), completableFuture);
        try {
            send(message);

        } catch (Exception e) {
            futureManager.remove(message.getId());
            throw e;
        }
        return completableFuture;

    }

    @Override
    public void addRemoteAddress(SocketAddress inetAddress) {
        this.remoteAddress = inetAddress;
    }


}
