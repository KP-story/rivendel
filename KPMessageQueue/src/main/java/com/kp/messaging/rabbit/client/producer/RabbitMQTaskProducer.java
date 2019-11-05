package com.kp.messaging.rabbit.client.producer;

import com.kp.messaging.rabbit.client.RabbitMQQueueConfig;
import com.kp.messaging.rabbit.client.connection.RabbitMQConnection;
import com.kp.common.data.message.IMessage;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;

import java.io.IOException;

public abstract class RabbitMQTaskProducer<I extends IMessage> extends RabbitMQProducer<I, Boolean> {

    public RabbitMQTaskProducer(RabbitMQConnection connection, RabbitMQQueueConfig queueConfig) {
        super(connection, queueConfig);
    }


    @Override
    public Boolean publish(I data) {
        if (this.getChannel() == null) {
            throw new RuntimeException("RabbitMQ Brocker has not been connected yet, please start before publish");
        }


        try {
            if (data != null) {
                return this.publish(getMessageParser().encodeMessage(data));
            }
        } catch (Exception e) {
            getLogger().error("public error {}", e);
            return false;
        }
        return false;
    }

    @Override
    public Boolean publish(I data, String key) {
        return null;
    }


    public boolean publish(byte[] data) {
        return this.publish(data, null);
    }

    private boolean publish(byte[] data, BasicProperties properties) {
        try {

            getChannel().basicPublish("", getQueueConfig().getQueueName(), properties, data);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Error while publishing message", e);
        }
    }

    @Override
    protected void onChannelReady(Channel channel) throws IOException {
        if (!getQueueConfig().isDeclare()) {
            return;
        }
        channel.queueDeclare(getQueueConfig().getQueueName(), getQueueConfig().isDurable(), getQueueConfig().isExclusive(), getQueueConfig().isAutoDelete(), getQueueConfig().getArguments());
    }

    @Override
    public Boolean forward(byte[] data, BasicProperties properties, String routingKey) {
        return this.publish(data, properties);
    }

    @Override
    protected void _start() {

    }

    @Override
    protected void _stop() {

    }
}
