package com.kp.messaging.rabbit.client.connection;


import com.kp.common.data.vo.HostAndPort;
import com.kp.common.data.vo.UserNameAndPassword;
import com.kp.common.log.Loggable;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.util.List;

public class RabbitMQConnectionFactory extends BasePooledObjectFactory<RabbitMQConnection> implements Loggable {

    RabbitMQConnectionGen rabbitMQConnectionGen;
    private UserNameAndPassword credential;
    private List<HostAndPort> endpoints;


    public RabbitMQConnectionFactory(UserNameAndPassword credential, List<HostAndPort> endpoints) {
        this.credential = credential;
        this.endpoints = endpoints;
        rabbitMQConnectionGen = new RabbitMQConnectionGen();
        rabbitMQConnectionGen.addEndpoints(endpoints);
        rabbitMQConnectionGen.setCredential(credential);
        rabbitMQConnectionGen.init();

    }

    public RabbitMQConnectionFactory(RabbitMQConnectionGen rabbitMQConnectionGen) {
        this.credential = credential;
        this.endpoints = endpoints;
        this.rabbitMQConnectionGen = rabbitMQConnectionGen;
    }

    @Override
    public RabbitMQConnection create() throws Exception {
        return rabbitMQConnectionGen.getConnection();
    }

    @Override
    public PooledObject<RabbitMQConnection> wrap(RabbitMQConnection producer) {
        return new DefaultPooledObject<RabbitMQConnection>(producer);
    }

    @Override
    public void destroyObject(PooledObject<RabbitMQConnection> p) throws Exception {
        if (p != null && p.getObject() != null) {
            RabbitMQConnection connection = p.getObject();
            connection.close();
            getLogger().info("Destroyed connection:ip{} port{} ", connection.getSourceConnection().getAddress().getHostAddress(), connection.getSourceConnection().getPort());

        }
    }


    @Override
    public boolean validateObject(PooledObject<RabbitMQConnection> p) {
        if (p != null && p.getObject() != null && p.getObject().getSourceConnection().isOpen()) {

            return true;
        } else {
            return false;
        }
    }

}


