package com.kp.common.worker;


import com.kp.common.data.message.ObjectHelper;
import com.kp.configuration.WorkerPoolCfg;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.WaitStrategy;

/**
 * Created by kukubutukandy on 30/05/2017.
 */
public interface KWorkerPool<I, O> extends ExceptionHandler<I> {

    void init(WorkerPoolCfg config, ObjectHelper helpGenerateMessage) throws Exception;

    void start() throws Exception;

    void shutdown() throws Exception;

    void setTTL(long TTL);

    I publish(I obj) throws Exception;

    long remainingCapacity();

    public WaitStrategy getWaitStrategy();

    public void setWaitStrategy(WaitStrategy waitStrategy);

    void setHandler(Handler handler);

    void setHandleCallback(HandleCallback handleCallback);


}
