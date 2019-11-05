package com.kp.common.data.message;

public interface IMessageParser<T, M extends IMessage> {


    T encodeMessage(M message) throws Exception;

    M decodeMessage(T input) throws Exception;

    void encodeMessage(M message, T out) throws Exception;


}
