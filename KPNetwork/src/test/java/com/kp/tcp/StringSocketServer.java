package com.kp.tcp;

import com.kp.common.data.message.IMessageParser;
import com.kp.network.DefaultFutureManager;
import com.kp.network.FutureManager;
import com.kp.network.connection.IConnectionManager;
import com.kp.network.event.impl.DefaultConnectionListenerManager;
import com.kp.network.event.impl.IConnectionListenerManager;
import com.kp.network.netty.server.tcp.NettyTcpServer;
import com.kp.StringMessage;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public class StringSocketServer extends NettyTcpServer<StringMessage> {

    public StringSocketServer(IConnectionManager<StringMessage, Channel> connectionManager, IConnectionListenerManager<StringMessage> connectionListenerManager, IMessageParser<ByteBuf, StringMessage> messageParser) throws Exception {
        super(connectionManager, connectionListenerManager, messageParser);
    }

    @Override
    protected IConnectionListenerManager<StringMessage> createEntryConnectionListenerManager() {
        return new DefaultConnectionListenerManager<>();
    }

    @Override
    public void applyOptions(ServerBootstrap b) {

    }

    @Override
    public FutureManager<String, StringMessage> newFutureManager() {
        return new DefaultFutureManager<>();
    }
}
