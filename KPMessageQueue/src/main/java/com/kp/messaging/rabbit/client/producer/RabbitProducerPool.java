package com.kp.messaging.rabbit.client.producer;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class RabbitProducerPool<T extends RabbitMQProducer> extends GenericObjectPool<T> {
    public RabbitProducerPool(PooledObjectFactory<T> factory) {
        super(factory);
    }

    public RabbitProducerPool(PooledObjectFactory<T> factory, GenericObjectPoolConfig config) {
        super(factory, config);
    }
}
