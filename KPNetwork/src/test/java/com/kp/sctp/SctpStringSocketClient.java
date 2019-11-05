package com.kp.sctp;

import com.kp.common.data.message.IMessageParser;
import com.kp.network.DefaultFutureManager;
import com.kp.network.FutureManager;
import com.kp.network.event.impl.IConnectionListenerManager;
import com.kp.network.netty.client.sctp.NettySctpClient;
import com.kp.StringMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.sctp.SctpMessage;

public class SctpStringSocketClient extends NettySctpClient<StringMessage> {


    public SctpStringSocketClient(IConnectionListenerManager connectionListenerManager, IMessageParser<SctpMessage, StringMessage> messageParser) {
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
