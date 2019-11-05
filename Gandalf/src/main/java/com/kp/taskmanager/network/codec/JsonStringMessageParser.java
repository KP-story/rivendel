package com.kp.taskmanager.network.codec;

import com.kp.common.data.message.IMessageParser;
import com.kp.common.data.vo.VObject;

public class JsonStringMessageParser implements IMessageParser<String, VOMessage> {


    @Override
    public String encodeMessage(VOMessage message) throws Exception {
        return message.toJson();
    }

    @Override
    public VOMessage decodeMessage(String input) throws Exception {
         VObject vObject= VObject.fromJSON(input);
        VOMessage voMessage = new VOMessage();
        voMessage.putAll(vObject);
        return voMessage;
    }

    @Override
    public void encodeMessage(VOMessage message, String out) throws Exception {

    }
}
