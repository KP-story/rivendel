package com.kp.network.netty.codec;

import com.kp.common.data.message.IMessage;
import com.kp.common.data.message.IMessageParser;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.sctp.SctpMessage;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class NettySctpMessageDecoder<T extends IMessage> extends MessageToMessageDecoder<SctpMessage> {

    IMessageParser<SctpMessage, T> messageParser;

    public NettySctpMessageDecoder(IMessageParser<SctpMessage, T> messageParser) {
        super();
        this.messageParser = messageParser;
    }


    @Override
    protected void decode(ChannelHandlerContext ctx, SctpMessage msg, List<Object> out) throws Exception {
        T message = messageParser.decodeMessage(msg);
        if (message != null) {
            out.add(message);
        }

    }
}