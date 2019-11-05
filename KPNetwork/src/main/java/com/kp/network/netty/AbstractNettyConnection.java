package com.kp.network.netty;

import com.kp.common.data.message.IMessage;
import com.kp.network.connection.AbstractBaseConnection;
import com.kp.network.event.impl.IConnectionListenerManager;
import io.netty.channel.Channel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Future;

public abstract class AbstractNettyConnection<T extends IMessage> extends AbstractBaseConnection<T, Channel> {

    public AbstractNettyConnection(IConnectionListenerManager connectionListenerManager) {
        super(connectionListenerManager);
    }

    @Override
    public String toString() {
        return "AbstractNettyConnection{" +
                "channel=" + context +
                '}';
    }


    @Override
    protected void _checkConnected() throws IOException {
        if (context == null || !context.isActive()) {
            throw new IOException("Connection is not active: " + context.toString());
        }
    }

    @Override
    protected Future _send(T message) throws IOException {
        return context.writeAndFlush(message);
    }


    @Override
    public Future disconnect() throws IOException, InterruptedException {
        super.disconnect();
        if (context != null) {
            return context.close().sync();
        } else {
            return null;
        }


    }

    @Override
    public Future release() throws IOException {
        Future future = null;
        try {
            future = disconnect();
        } catch (Exception e) {
            getLogger().error("error on disconnect {}", e);

        }

        return future;
    }

    @Override
    public boolean isNetworkInitiated() {
        try {
            if (context != null && context.isOpen()) {
                return true;
            }

        } catch (Exception e) {
            getLogger().error("isConnected {}", e);
        }
        return false;
    }


    @Override
    public void _onConnected() {
        InetSocketAddress remote = (InetSocketAddress) getRemoteAddress();
        InetSocketAddress local = (InetSocketAddress) context.localAddress();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(remote.getHostString()).append(remote.getPort()).append(local.getHostString()).append(local.getPort());
        id = stringBuilder.toString();

    }


}
