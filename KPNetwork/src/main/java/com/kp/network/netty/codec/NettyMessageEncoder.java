package com.kp.network.netty.codec;

import com.kp.common.data.message.IMessage;
import com.kp.common.data.message.IMessageParser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by kukubutukandy on 10/05/2017.
 */
public class NettyMessageEncoder<T extends IMessage> extends MessageToByteEncoder<IMessage> implements ChannelHandler {

    IMessageParser<ByteBuf, T> messageParser;

    public NettyMessageEncoder(IMessageParser<ByteBuf, T> messageParser) {
        super();
        this.messageParser = messageParser;

    }


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, IMessage message, ByteBuf out) throws Exception {
        messageParser.encodeMessage((T) message, out);
    }
}
