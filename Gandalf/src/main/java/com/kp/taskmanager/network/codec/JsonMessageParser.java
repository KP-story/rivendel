package com.kp.taskmanager.network.codec;

import com.kp.common.data.message.IMessageParser;
import com.kp.common.data.vo.VObject;
import io.netty.buffer.ByteBuf;


public class JsonMessageParser implements IMessageParser<ByteBuf, VOMessage> {
    @Override
    public ByteBuf encodeMessage(VOMessage message) throws Exception {
        return null;
    }

    @Override
    public VOMessage decodeMessage(ByteBuf input) throws Exception {
        byte[] raw = new byte[input.readableBytes()];
        input.readBytes(raw);

         VObject vObject= VObject.fromJSON(new String(raw));
        VOMessage voMessage = new VOMessage();
        voMessage.putAll(vObject);
        return voMessage;
    }

    @Override
    public void encodeMessage(VOMessage message, ByteBuf out) throws Exception {
        out.writeBytes(message.toJson().getBytes());
    }
}
