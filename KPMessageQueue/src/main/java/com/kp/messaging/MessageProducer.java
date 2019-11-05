package com.kp.messaging;

import com.kp.common.data.message.IMessage;
import com.kp.common.data.message.IMessageParser;

public interface MessageProducer<I extends IMessage, T> {
    ;

    public T publish(I data);

    public T publish(I data, String key);

    public abstract IMessageParser<byte[], I> getMessageParser();

}
