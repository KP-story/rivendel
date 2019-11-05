package com.kp.common.worker.impl;

import com.kp.common.data.message.IMessage;
import com.kp.common.worker.HandleCallback;
import com.kp.common.worker.Handler;
 import com.lmax.disruptor.WorkHandler;

import java.util.concurrent.TimeoutException;

/**
 * Created by kukubutukandy on 30/05/2017.
 */

public class KWorkerHanlder<I extends IMessage, O> implements WorkHandler<I> {
    HandleCallback<I, O> handleCallback;
    Handler<I, O> handler;
    private long messageTimeout;

    public HandleCallback<I, O> getHandleCallback() {
        return handleCallback;
    }

    public void setHandleCallback(HandleCallback<I, O> handleCallback) {
        this.handleCallback = handleCallback;
    }

    public Handler<I, O> getHandler() {
        return handler;
    }

    public void setHandler(Handler<I, O> handler) {
        this.handler = handler;
    }

    public long getMessageTimeout() {
        return messageTimeout;
    }

    public void setMessageTimeout(long messageTimeout) {
        this.messageTimeout = messageTimeout;
    }

    @Override
    public void onEvent(I message) throws Exception {
        if (this.getHandler() != null) {
            O result = null;
            boolean hasError = false;
            try {
                long timeNow = System.currentTimeMillis();
                if (messageTimeout > 0 && message.getCreatedTime() > 0 && (message.getCreatedTime() + messageTimeout) <= timeNow) {
                    throw new TimeoutException("message timeout {}" + message.toString());

                }
                result = this.getHandler().handle(message);
            } catch (Exception e) {
                if (this.getHandleCallback() != null) {
                    this.getHandleCallback().onHandleError(message, e, HandleCallback.HANDLE_ERROR);
                }
                hasError = true;
            }
            if (!hasError && this.getHandleCallback() != null) {
                this.getHandleCallback().onHandleComplete(message, result);
            }

        }
    }
}
