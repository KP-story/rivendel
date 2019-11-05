package com.kp.messaging.rabbit.client.producer;


import com.kp.messaging.rabbit.client.RabbitMQQueueConfig;
import com.kp.messaging.rabbit.client.connection.RabbitMQConnection;
import com.kp.messaging.rabbit.client.connection.WrapRabbitMQConnectPool;
import com.kp.common.data.message.ObjectHelper;
import com.kp.common.log.Loggable;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class RabbitProducerFactory<T extends RabbitMQProducer> extends BasePooledObjectFactory<T> implements Loggable {


    private WrapRabbitMQConnectPool connectionPool;
    private RabbitMQQueueConfig rabbitMQQueueConfig;
    private ObjectHelper<T> objectHelper;

    public RabbitProducerFactory(WrapRabbitMQConnectPool connectionPool, RabbitMQQueueConfig rabbitMQQueueConfig, ObjectHelper<T> objectHelper) throws Exception {
        this.connectionPool = connectionPool;
        this.rabbitMQQueueConfig = rabbitMQQueueConfig;
        this.objectHelper = objectHelper;
    }


    @Override
    public T create() throws Exception {
        RabbitMQConnection rabbitMQConnection = null;
        T a = null;
        try {
            rabbitMQConnection = connectionPool.borrowObject();


            a = objectHelper.newInstance(rabbitMQConnection, rabbitMQQueueConfig);
            a.start();
            return a;
        } catch (Exception ex) {
            if (a != null) {
                a.close();
            }
            throw ex;
        } finally {
            if (rabbitMQConnection != null) {
                connectionPool.returnObject(rabbitMQConnection);
            }

        }
    }

    @Override
    public PooledObject<T> wrap(T producer) {
        return new DefaultPooledObject<T>(producer);
    }

    @Override
    public void destroyObject(PooledObject<T> p) throws Exception {
        if (p != null && p.getObject() != null) {

            p.getObject().close();
            super.destroyObject(p);


            getLogger().info("Destroyed Producer:{} {}", p, p.getObject());
        }
    }


    @Override
    public boolean validateObject(PooledObject<T> p) {
        if (p.getObject() != null) {
            if (p.getObject().isOpen()) {
                return true;
            } else {
                getLogger().error("ValidateObject is False! producer{} {}", p, p.getObject());
                return false;
            }
        }

        getLogger().warn("ValidateObject is False! producer{} {}", p, p.getObject());

        return false;


    }

}
