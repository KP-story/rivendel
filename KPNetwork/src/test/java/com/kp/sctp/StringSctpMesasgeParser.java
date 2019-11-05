package com.kp.sctp;

import com.kp.common.data.message.IMessageParser;
import com.kp.StringMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.sctp.SctpMessage;

public class StringSctpMesasgeParser implements IMessageParser<SctpMessage, StringMessage> {
    private static final byte END_MESSAGE = 59;
    private static final String END_MESSAGE_STR = ";";


    @Override
    public SctpMessage encodeMessage(StringMessage message) throws Exception {

        String content = message.content;
        if (!content.endsWith(";\n\r")) {
            message.content = content + END_MESSAGE_STR;
        }

        ByteBuf firstMessage;
        firstMessage = Unpooled.buffer(message.content.length());
        firstMessage.writeBytes(message.content.getBytes());

        SctpMessage sctpMessage = new SctpMessage(0, 0, firstMessage);
        return sctpMessage;
    }


    @Override
    public StringMessage decodeMessage(SctpMessage input) throws Exception {
        StringMessage stringMessage = new StringMessage();
        ByteBuf in = input.content();
        int bufferLength = in.writerIndex();
        int i;
        int isdnIndex = 0;
        int lastIndex = 0;
        for (i = in.readerIndex(); i < bufferLength; ++i) {
            byte readByte = in.getByte(i);
            if (readByte == END_MESSAGE) {
                lastIndex = i;
                break;
            }
        }
        if (lastIndex > in.readerIndex()) {
            byte[] data = new byte[lastIndex - in.readerIndex() + 1];
            in.readBytes(data);
            String content = new String(data);
            stringMessage.content = content;
            return stringMessage;

        }
        return null;
    }

    @Override
    public void encodeMessage(StringMessage message, SctpMessage out) throws Exception {


    }


}
