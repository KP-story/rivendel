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

public abstract class RabbitMQRPCProducer<I extends IMessage> extends RabbitMQProducer<I, Future<I>> {

    private RabbitMQConnection connection;
    private RabbitMQQueueConfig queueConfig;
    private String replyQueueName;
    private Consumer consumer;


    private Map<String, Future<I>> futures = new ConcurrentHashMap<>();


    public RabbitMQRPCProducer(RabbitMQConnection connection, RabbitMQQueueConfig queueConfig) {
        super(connection, queueConfig);

        this.connection = connection;
        this.queueConfig = queueConfig;
    }


    @Override
    public Future<I> publish(I data) {
        if (this.getChannel() == null) {
            throw new RuntimeException("RabbitMQ Brocker has not been connected yet, please start before publish");
        }
        try {
            return this.publish(getMessageParser().encodeMessage(data));

        } catch (Exception e) {
            getLogger().error("public error {}", e);
            return null;
        }
    }

    public Future<I> publish(byte[] data) {
        String corrId = UUID.randomUUID().toString();
        BasicProperties properties = new BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName)
                .build();
        return this.publish(properties, data);
    }

    private Future<I> publish(BasicProperties properties, byte[] data) {
        try {
            Future<I> future = new CompletableFuture<>();
            this.futures.put(properties.getCorrelationId(), future);
            getChannel().basicPublish("", getQueueConfig().getQueueName(), properties, data);
            return future;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onChannelReady(Channel channel) throws IOException {
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
                        RabbitMQRPCProducer.this.futures.remove(corrId);
                    }
                } else {
                    getLogger().debug("Future not found for corrId: " + corrId);
                }
            }
        };
        channel.basicConsume(replyQueueName, true, this.consumer);
    }

    @Override
    public Future<I> forward(byte[] data, BasicProperties properties, String routingKey) {
        if (properties == null) {
            return this.publish(data);
        }
        return this.publish(properties, data);
    }

    @Override
    protected void _start() {
        getLogger().info("RabbitMQRPCProducer has started successfully!!!");
    }

    @Override
    protected void _stop() {
        for (Future<?> future : this.futures.values()) {
            future.cancel(true);
        }
    }

}
