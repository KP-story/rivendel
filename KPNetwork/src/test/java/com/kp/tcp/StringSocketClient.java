package com.kp.tcp;

import com.kp.common.data.message.IMessageParser;
import com.kp.network.DefaultFutureManager;
import com.kp.network.FutureManager;
import com.kp.network.event.impl.IConnectionListenerManager;
import com.kp.network.netty.client.tcp.NettyTcpClient;
import com.kp.StringMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public class StringSocketClient extends NettyTcpClient<StringMessage> {


    public StringSocketClient(IConnectionListenerManager connectionListenerManager, IMessageParser<ByteBuf, StringMessage> messageParser) {
        super(connectionListenerManager, messageParser);
    }

    @Override
    public void applyOptions(Bootstrap b) {

    }

    @Override
    public void applyChannelHandler(Channel ch) {

    }


    @Override
    protected FutureManager<String, StringMessage> createFutureManager() {
        return new DefaultFutureManager<>();
    }
}
