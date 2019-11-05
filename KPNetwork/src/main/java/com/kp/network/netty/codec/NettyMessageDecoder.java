package com.kp.network.netty.codec;

import com.kp.common.data.message.IMessage;
import com.kp.common.data.message.IMessageParser;
import com.kp.common.log.Loggable;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by kukubutukandy on 09/05/2017.
 */

public class NettyMessageDecoder<T extends IMessage> extends ByteToMessageDecoder implements Loggable {

    IMessageParser<ByteBuf, T> messageParser;

    public NettyMessageDecoder(IMessageParser<ByteBuf, T> messageParser) {
        super();
        this.messageParser = messageParser;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) throws Exception {
        IMessage iMessage = messageParser.decodeMessage(in);
        if (iMessage != null) {
            list.add(iMessage);
        }
    }
}
