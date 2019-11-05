package com.kp.network.netty.server;

import com.kp.common.data.message.IMessage;
import com.kp.common.log.Loggable;
import com.kp.network.FutureManager;
import com.kp.network.SocketServer;
import com.kp.network.connection.IConnection;
import com.kp.network.connection.IConnectionManager;
import com.kp.network.event.impl.ConnectionListener;
import com.kp.network.event.impl.IConnectionListenerManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractNettySocketServer<T extends IMessage> implements SocketServer<T>, Loggable {
    protected IConnectionManager<T, Channel> connectionManager;
    protected int localPort;
    protected EventLoopGroup bossGroup;
    protected EventLoopGroup workerGroup;
    protected Channel serverChannel;
    protected int nboot;
    protected int timeout;
    protected int nworker;
    protected int rcvbuf;
    protected IConnectionListenerManager<T> connectionListenerManager;
    protected AtomicBoolean isInit = new AtomicBoolean(false);

    public AbstractNettySocketServer(IConnectionManager<T, Channel> connectionManager, IConnectionListenerManager<T> connectionListenerManager) throws Exception {
        this.connectionManager = connectionManager;
        this.connectionManager.init();
        this.connectionListenerManager = connectionListenerManager;
        this.connectionListenerManager.init();
    }

    protected abstract Future _init() throws Exception;

    protected abstract IConnectionListenerManager<T> createEntryConnectionListenerManager();

    @Override
    public boolean isActive() {
        if (serverChannel != null && serverChannel.isActive()) {
            return true;
        }
        return false;
    }

    @Override
    public Future init() throws Exception {
        if (isInit.get()) {
            throw new InstantiationException("instance is created");
        }
        isInit.set(true);
        return _init();
    }

    public int getNboot() {
        return nboot;
    }

    @Override
    public void setNboot(int nboot) {
        this.nboot = nboot;
    }

    public int getNworker() {
        return nworker;
    }

    @Override
    public void setNworker(int nworker) {
        this.nworker = nworker;
    }

    public int getRcvbuf() {
        return rcvbuf;
    }

    @Override
    public void setRcvbuf(int rcvbuf) {
        this.rcvbuf = rcvbuf;
    }

    public int getTimeout() {
        return timeout;
    }

    @Override
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public abstract void applyOptions(ServerBootstrap b);


    @Override
    public void removeRemoteConnection(IConnection iConnection) throws Exception {
        IConnection connection = connectionManager.remove(iConnection.getId());
        if (connection != null && connection.isNetworkInitiated()) {
            connection.release();
        }
    }

    @Override
    public void addConnectionListener(String id, ConnectionListener<T> connectionListener) throws Exception {
        connectionListenerManager.add(id, connectionListener);
    }

    @Override
    public void remAllConnectionListener() throws Exception {
        connectionListenerManager.removeAll();

    }

    @Override
    public void broadcastMessage(T message) throws Exception {
        connectionManager.broadcastMessage(message);
    }

    @Override
    public Future destroy() throws Exception {
        try {
            if (serverChannel != null) {
                serverChannel.close().sync();
            }
        } catch (Exception e) {
            getLogger().error("channel.close {}", e);
        }
        try {
            if (workerGroup != null) {
                workerGroup.shutdownGracefully().sync();
            }
        } catch (Exception e) {
            getLogger().error("workerGroup.shutdownGracefully {}", e);


        }
        try {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully().sync();
            }

        } catch (Exception e) {
            getLogger().error("bossGroup.shutdownGracefully {}", e);

        }
        try {

            if (connectionManager != null) {
                connectionManager.destroy();
            }
        } catch (Exception e) {
            getLogger().error("connectionManager.destroy {}", e);

        }

        try {

            if (connectionListenerManager != null) {
                connectionListenerManager.destroy();
            }
        } catch (Exception e) {
            getLogger().error("connectionListenerManager.destroy {}", e);

        }

        return null;

    }

    @Override
    public void addLocalPort(int port) {
        this.localPort = port;
    }


    @Override
    public int getLocalPort() {
        return localPort;
    }

    protected abstract FutureManager<String, T> newFutureManager();

}
