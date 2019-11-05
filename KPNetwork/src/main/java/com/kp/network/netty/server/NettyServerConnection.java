package com.kp.network.netty.server;

import com.kp.common.data.message.IMessage;
import com.kp.network.event.impl.IConnectionListenerManager;
import com.kp.network.netty.AbstractNettyConnection;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.Future;

public abstract class NettyServerConnection<T extends IMessage> extends AbstractNettyConnection<T> {


    public NettyServerConnection(IConnectionListenerManager connectionListenerManager) throws Exception {
        super(connectionListenerManager);
        connectionListenerManager.init();
    }

    @Override
    public void setNWorker(int i) {
        throw new UnsupportedOperationException();

    }

    @Override
    public Future connect() throws IOException, InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Future _connect() throws IOException, InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addRemoteAddress(SocketAddress inetAddress) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void addLocalAddress(SocketAddress inetAddress) {
        throw new UnsupportedOperationException();

    }

    @Override
    public Future disconnect() throws IOException, InterruptedException {

        try {
            Future future = super.disconnect();
            return future;

        } finally {
            try {
                connectionListenerManager.destroy();

            } catch (Exception e) {
                throw new InterruptedException(e.getMessage());
            }

        }


    }

    @Override
    public SocketAddress getRemoteAddress() {

        return context.remoteAddress();
    }

    @Override
    public SocketAddress[] getLocalAddress() {
        SocketAddress[] a = {context.localAddress()};
        return a;
    }


}
