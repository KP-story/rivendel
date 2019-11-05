package com.kp.messaging.rabbit.client.producer;


import com.kp.messaging.MessageProducer;
import com.kp.messaging.rabbit.client.RabbitMQChannelWrapper;
import com.kp.messaging.rabbit.client.RabbitMQQueueConfig;
import com.kp.messaging.rabbit.client.connection.RabbitMQConnection;
import com.kp.common.data.message.IMessage;
import com.rabbitmq.client.AMQP.BasicProperties;

public abstract class RabbitMQProducer<I extends IMessage, T> extends RabbitMQChannelWrapper implements MessageProducer<I, T> {

    private RabbitMQQueueConfig queueConfig;

    public RabbitMQProducer(RabbitMQConnection connection, RabbitMQQueueConfig queueConfig) {
        super(connection);
        this.queueConfig = queueConfig;

    }

    @Override
    public String toString() {
        String notif = "server=" + getConnection().getSourceConnection().getAddress().getHostAddress() + ":" + getConnection().getSourceConnection().getPort() + "\n";

        notif = notif + "queueName=" + queueConfig.getQueueName();


        return notif;


    }

    public abstract T forward(byte[] data, BasicProperties properties, String routingKey);


    @Override
    public T publish(I data) {
        // tobe override by subclass
        throw new UnsupportedOperationException(
                "Method publish(PuObject) doesn't supported in " + this.getClass().getName() + " class");
    }

    @Override
    public T publish(I data, String key) {
        // tobe override by subclass
        throw new UnsupportedOperationException("Method publish(PuObject data, String key) doesn't supported in "
                + this.getClass().getName() + " class");
    }

    public RabbitMQQueueConfig getQueueConfig() {
        return queueConfig;
    }
}
