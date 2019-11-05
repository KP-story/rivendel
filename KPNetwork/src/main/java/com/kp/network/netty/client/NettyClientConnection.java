package com.kp.network.netty.client;

import com.kp.common.data.message.IMessage;
import com.kp.common.data.message.IMessageParser;
import com.kp.network.event.impl.IConnectionListenerManager;
import com.kp.network.netty.AbstractNettyConnection;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;

import java.io.IOException;
import java.util.concurrent.Future;

public abstract class NettyClientConnection<Y, T extends IMessage> extends AbstractNettyConnection<T> {


    protected IMessageParser<Y, T> messageParser;
    protected EventLoopGroup workerGroup;
    protected int nworker;

    public NettyClientConnection(IConnectionListenerManager connectionListenerManager, IMessageParser<Y, T> messageParser) {
        super(connectionListenerManager);
        this.messageParser = messageParser;
    }

    @Override
    public void setNWorker(int nworker) {
        this.nworker = nworker;

    }

    public int getNworker() {
        return nworker;
    }


    @Override
    public Future release() throws IOException {
        Future[] futures = new Future[2];
        Future future = super.release();
        if (future != null) {
            futures[0] = future;
        }
        try {
            if (workerGroup != null) {
                future = workerGroup.shutdownGracefully().sync();
                if (future != null) {
                    futures[1] = future;
                }
            }
        } catch (Exception e) {
            getLogger().error("error on workerGroup.shutdownGracefully {}", e);
        }


        try {
            connectionListenerManager.destroy();
        } catch (Exception e) {
            getLogger().error("error on connectionListenerManager.destroy {}", e);
        }
        return future;
     }

    public abstract void applyOptions(Bootstrap b);

    public abstract void applyChannelHandler(Channel ch);


}
