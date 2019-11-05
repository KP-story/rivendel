package com.kp.messaging.rabbit.client.producer;

import com.kp.messaging.rabbit.client.RabbitMQQueueConfig;
import com.kp.messaging.rabbit.client.connection.RabbitMQConnection;
import com.kp.common.data.message.IMessage;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import jdk.nashorn.internal.runtime.ParserException;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public abstract class RabbitMQRoutingRPCProducer<I extends IMessage> extends RabbitMQProducer<I, Future<I>> {

    private String replyQueueName;
    private Consumer consumer;
    private Map<String, Future<I>> futures = new ConcurrentHashMap<>();

    public RabbitMQRoutingRPCProducer(RabbitMQConnection connection, RabbitMQQueueConfig queueConfig) {
        super(connection, queueConfig);
    }


    @Override
    public Future<I> publish(I data) {
        return this.publish(data, this.getQueueConfig().getRoutingKey());
    }

    @Override
    public Future<I> publish(I data, String routingKey) {
        if (this.getChannel() == null) {
            throw new RuntimeException("RabbitMQ Brocker has not been connected yet, please start before publish");
        }
        try {
            return this.publish(routingKey, getMessageParser().encodeMessage(data));

        } catch (Exception e) {
            getLogger().error("public error {}", e);
            return null;
        }
    }

    public Future<I> publish(String routingKey, byte[] data) {
        String corrId = UUID.randomUUID().toString();
        BasicProperties properties = new BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName)
                .build();
        return this.publish(routingKey, properties, data);
    }

    private Future<I> publish(String routingKey, BasicProperties properties, byte[] data) {
        try {
            Future<I> future = new CompletableFuture<>();
            this.futures.put(properties.getCorrelationId(), future);
            getChannel().basicPublish(getQueueConfig().getExchangeName(),
                    routingKey == null ? this.getQueueConfig().getRoutingKey() : routingKey, properties, data);
            return future;
        } catch (IOException e) {
            getLogger().error("An error occurs while publishing data", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onChannelReady(Channel channel) throws IOException {

        channel.exchangeDeclare(getQueueConfig().getExchangeName(), getQueueConfig().getExchangeType());
        this.replyQueueName = channel.queueDeclare().getQueue();
        this.consumer = new DefaultConsumer(channel) {

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
                    throws IOException {
                String corrId = properties.getCorrelationId();
                CompletableFuture<I> future = (CompletableFuture<I>) futures.get(corrId);
                if (future != null) {
                    try {
                        I message = getMessageParser().decodeMessage(body);
                        future.complete(message);
                    } catch (Exception e) {


                        getLogger().error("deserialize message error {}", e);
                        future.completeExceptionally(new ParserException("deserilize message exception "));

                    } finally {
                        RabbitMQRoutingRPCProducer.this.futures.remove(corrId);
                    }
                }
            }
        };
        channel.basicConsume(replyQueueName, true, this.consumer);
    }

    @Override
    public Future<I> forward(byte[] data, BasicProperties properties, String routingKey) {
        if (properties == null) {
            return this.publish(routingKey, data);
        }
        return this.publish(routingKey, properties, data);
    }

    @Override
    protected void _start() {

    }

    @Override
    protected void _stop() {
        for (Future<?> future : this.futures.values()) {
            future.cancel(true);
        }
    }
}
