package com.kp.network.netty.codec;

import com.kp.common.data.message.IMessage;
import com.kp.common.data.message.IMessageParser;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.sctp.SctpMessage;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class NettySctpMessageEncoder<T extends IMessage> extends MessageToMessageEncoder<IMessage> {

    IMessageParser<SctpMessage, T> messageParser;

    public NettySctpMessageEncoder(IMessageParser<SctpMessage, T> messageParser) {
        super();
        this.messageParser = messageParser;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, IMessage msg, List<Object> out) throws Exception {

        SctpMessage sctpMessage = this.messageParser.encodeMessage((T) msg);
        if (sctpMessage != null) {
            out.add(sctpMessage);
        }


    }
}
