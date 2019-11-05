package com.kp.common.worker;

/**
 * Created by kukubutukandy on 30/05/2017.
 */
public interface HandleCallback<I, O> {
    static int HANDLE_ERROR = 1;
    static int SYSTEM_ERROR = 2;

    void onHandleComplete(I request, O result);

    void onHandleError(I request, Throwable exception, int reason);
}
