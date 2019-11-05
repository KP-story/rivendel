package com.kp.async;


import com.kp.common.data.message.IMessage;
import com.kp.common.data.message.IMessageParser;

public interface Consumer<I extends IMessage> {
    void start() throws Exception;

    void stop() throws Exception;

    void init() throws Exception;

    void addMessageProvider(int id, MessageProvider messageProvider) throws Exception;

    void removeMesssageProvider(int id);
    public abstract IMessageParser<byte[], I> getMessageParser() ;

}
