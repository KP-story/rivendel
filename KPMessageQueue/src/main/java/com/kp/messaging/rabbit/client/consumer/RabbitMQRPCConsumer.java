package com.kp.messaging.rabbit.client.consumer;

import com.kp.common.data.message.IMessage;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import com.rabbitmq.client.Envelope;

public abstract class RabbitMQRPCConsumer<I extends IMessage> extends RabbitMQWorkerConsumer<I> {

    protected BasicProperties getReplyProperties(BasicProperties properties) {
        Builder builder = new Builder();
        builder.correlationId(properties.getCorrelationId());
        return builder.build();
    }

    @Override
    protected void handleResult(String consumerTag, Envelope envelope, BasicProperties properties, I result) {
        if (result == null) {
            // ignore
            return;
        }


        String replyQueue = properties.getReplyTo();
        if (replyQueue != null && replyQueue.trim().length() > 0) {

            try {
                byte[] response = result == null ? null : getMessageParser().encodeMessage(result);
                BasicProperties replyProperties = this.getReplyProperties(properties);

                getChannel().basicPublish("", replyQueue, replyProperties, response);
            } catch (Exception e) {
                getLogger().error("Cannot send response to producer, queue name: " + replyQueue, e);
            }
        }
    }
}
