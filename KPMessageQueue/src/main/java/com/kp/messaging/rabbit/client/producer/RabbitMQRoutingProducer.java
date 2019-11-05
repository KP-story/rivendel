package com.kp.messaging.rabbit.client.producer;

import com.kp.messaging.rabbit.client.RabbitMQQueueConfig;
import com.kp.messaging.rabbit.client.connection.RabbitMQConnection;
import com.kp.common.data.message.IMessage;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.UUID;

public abstract class RabbitMQRoutingProducer<I extends IMessage> extends RabbitMQProducer<I, Boolean> {


    public RabbitMQRoutingProducer(RabbitMQConnection connection, RabbitMQQueueConfig queueConfig) {
        super(connection, queueConfig);
    }

    public abstract String getReplyQueueName();

    @Override
    public Boolean publish(I data) {
        return this.publish(data, this.getQueueConfig().getRoutingKey());
    }

    @Override
    public Boolean publish(I data, String routingKey) {

        if (this.getChannel() == null) {
            throw new RuntimeException("RabbitMQ Brocker has not been connected yet, please start before publish");
        }
        try {
            return this.publish(routingKey, getMessageParser().encodeMessage(data));

        } catch (Exception e) {
            getLogger().error("public error {}", e);
            return false;
        }
    }


    public Boolean publish(String routingKey, byte[] data) {
        String corrId = UUID.randomUUID().toString();
        BasicProperties props = new BasicProperties.Builder().correlationId(corrId).replyTo(getReplyQueueName()).build();
        return this.publish(routingKey, props, data);
    }

    private Boolean publish(String routingKey, BasicProperties properties, byte[] data) {
        try {
            getChannel().basicPublish(getQueueConfig().getExchangeName(),
                    routingKey == null ? this.getQueueConfig().getRoutingKey() : routingKey, properties, data);
            return true;
        } catch (IOException e) {
            getLogger().error("An error occurs while publishing data", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onChannelReady(Channel channel) throws IOException {

        if (!getQueueConfig().isDeclare()) {
            return;
        }
        channel.exchangeDeclare(getQueueConfig().getExchangeName(), getQueueConfig().getExchangeType());
    }

    @Override
    public Boolean forward(byte[] data, BasicProperties properties, String routingKey) {
        return this.publish(routingKey, properties, data);
    }


}
