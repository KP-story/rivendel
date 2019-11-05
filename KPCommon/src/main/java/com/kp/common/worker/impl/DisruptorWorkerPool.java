package com.kp.common.worker.impl;

import com.kp.common.data.message.IMessage;
import com.kp.common.data.message.ObjectHelper;
import com.kp.common.log.Loggable;
import com.kp.common.worker.HandleCallback;
import com.kp.common.worker.Handler;
import com.kp.common.worker.KWorkerPool;
 import com.kp.configuration.WorkerPoolCfg;
import com.lmax.disruptor.*;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by kukubutukandy on 30/05/2017.
 */
public class DisruptorWorkerPool<I extends IMessage, O> implements KWorkerPool<I, O>, Loggable {


    private WorkerPool<I> workerPool;
    private RingBuffer<I> ringBuffer;
    private KWorkerHanlder[] workers;
    private WorkerPoolCfg config;
    private Handler<I, O> handler;
    private HandleCallback<I, O> handleCallback;
    private ThreadPoolExecutor threadPoolExecutor;
    private WaitStrategy waitStrategy;
    private long TTL;
    private AtomicBoolean isOpen = new AtomicBoolean(false);
    private ObjectHelper messageHelper;

    @Override
    public WaitStrategy getWaitStrategy() {
        return waitStrategy;
    }

    @Override
    public void setWaitStrategy(WaitStrategy waitStrategy) {
        this.waitStrategy = waitStrategy;
    }

    public long getTTL() {
        return TTL;
    }

    @Override
    public void setTTL(long TTL) {
        this.TTL = TTL;
    }

    @Override
    public void init(WorkerPoolCfg config, ObjectHelper messageHelper) throws Exception {
        this.messageHelper = messageHelper;
        this.config = config;
        if (waitStrategy == null) {
            waitStrategy = new BlockingWaitStrategy();
        }
        this.ringBuffer = RingBuffer.createMultiProducer(new EventFactory<I>() {

            @Override
            public I newInstance() {
                try {

                    return (I) messageHelper.newInstance();
                } catch (Exception e) {
                    getLogger().error("newInstance data in ringbuffer error {}", e);
                    throw e;
                }
            }

        }, config.getRingBuferSize(), waitStrategy);


        this.workerPool = new WorkerPool<I>(this.ringBuffer, this.ringBuffer.newBarrier(), this,
                getWorkers(config.getPoolSize()));

        this.ringBuffer.addGatingSequences(this.workerPool.getWorkerSequences());


    }


    @SuppressWarnings("unchecked")
    private KWorkerHanlder<I, O>[] getWorkers(int poolSize) {
        if (workers != null) {
            return workers;
        }
        workers = new KWorkerHanlder[poolSize];
        for (int i = 0; i < poolSize; i++) {
            KWorkerHanlder<I, O> businessWorkerHandle = new KWorkerHanlder<I, O>();
            businessWorkerHandle.setHandler(this.handler);
            businessWorkerHandle.setMessageTimeout(getTTL());
            businessWorkerHandle.setHandleCallback(this.handleCallback);
            workers[i] = businessWorkerHandle;
        }
        return workers;

    }


    @Override
    public void start() throws Exception {
        threadPoolExecutor = new ThreadPoolExecutor(config.getPoolSize(), config.getPoolSize(), config.getKeepAliveTime(), TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {

            final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format(getClass().getSimpleName(), threadNumber.getAndIncrement()));
            }
        });
        this.workerPool.start(threadPoolExecutor);
        isOpen.set(true);


    }

    @Override
    public void shutdown() throws Exception {
        isOpen.set(false);
        if (this.workerPool != null && this.workerPool.isRunning()) {
            getLogger().info("shutting down worker pool...");
            this.workerPool.drainAndHalt();
            getLogger().info("Tester pool shutted down");
        }
        if (threadPoolExecutor != null) {
            threadPoolExecutor.shutdown();
        }
    }

    @Override
    public I publish(I obj) throws Exception {
        if (!isOpen.get()) {
            throw new IllegalStateException("workerPool is down");

        }
        I message;

        long sequence = this.ringBuffer.next();
        try {
            message = this.ringBuffer.get(sequence);
            message.copy(obj);
        } finally {
            this.ringBuffer.publish(sequence);
        }
        return message;
    }

    @Override
    public long remainingCapacity() {
        return this.ringBuffer.remainingCapacity();

    }

    @Override
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void setHandleCallback(HandleCallback handleCallback) {
        this.handleCallback = handleCallback;
    }


    @Override
    public void handleEventException(Throwable throwable, long sequence, I i) {
        if (handleCallback != null) {
            handleCallback.onHandleError(i, throwable, HandleCallback.SYSTEM_ERROR);
        } else {
            getLogger().error("An error occurs when handling message at sequence: {}, data: {}", sequence,
                    i, throwable);
        }
    }

    @Override
    public void handleOnStartException(Throwable throwable) {
        getLogger().error("An error occurs when shutting down handle message {}", throwable);

    }

    @Override
    public void handleOnShutdownException(Throwable throwable) {
        getLogger().error("An error occurs when starting handle message", throwable);

    }
}
