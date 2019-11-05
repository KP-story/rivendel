package com.kp.async;

 import com.kp.common.data.message.IMessage;

/**
 * Created by kukubutukandy on 16/05/2017.
 */
public interface MessageProvider<T extends IMessage, M> {
    void onMessageReceived(T message, M distributor, Throwable e, Object... params);
}
