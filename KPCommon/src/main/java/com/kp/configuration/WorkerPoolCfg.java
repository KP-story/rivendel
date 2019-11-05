package com.kp.configuration;

/**
 * Created by kukubutukandy on 30/05/2017.
 */

public class WorkerPoolCfg {
    int ringBuferSize;
    int poolSize;
    int keepAliveTime = 60;

    public int getRingBuferSize() {
        return ringBuferSize;
    }

    public void setRingBuferSize(int ringBuferSize) {
        this.ringBuferSize = ringBuferSize;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public int getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(int keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }


}
