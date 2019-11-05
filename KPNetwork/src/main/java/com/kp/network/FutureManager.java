package com.kp.network;

import com.kp.common.data.message.IMessage;

import java.util.concurrent.Future;

public interface FutureManager<K, T extends IMessage> extends IObjectManager<K, Future<T>> {
    void setMessageTimeout(long messageTimeout);


}
