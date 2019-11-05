package com.kp;

import com.kp.common.data.message.IMessageParser;
import io.netty.buffer.ByteBuf;

public class StringMesasgeParser implements IMessageParser<ByteBuf, StringMessage> {
    private static final byte END_MESSAGE = 59;
    private static final String END_MESSAGE_STR = ";\n\r";


    @Override
    public ByteBuf encodeMessage(StringMessage message) throws Exception {
        return null;
    }

    @Override
    public StringMessage decodeMessage(ByteBuf in) throws Exception {
        StringMessage stringMessage = new StringMessage();
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
    public void encodeMessage(StringMessage message, ByteBuf out) throws Exception {
        String content = message.content;
        if (!content.endsWith(";\n\r")) {
            content = content + END_MESSAGE_STR;
        }

        out.writeBytes(content.getBytes());
    }


}
