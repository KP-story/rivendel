package com.kp.messaging.rabbit.client.connection;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class WrapRabbitMQConnectPool extends GenericObjectPool<RabbitMQConnection> {
    public WrapRabbitMQConnectPool(PooledObjectFactory<RabbitMQConnection> factory) {
        super(factory);
    }

    public WrapRabbitMQConnectPool(PooledObjectFactory<RabbitMQConnection> factory, GenericObjectPoolConfig config) {
        super(factory, config);
    }
}
