package com.kp.messaging.rabbit.client.consumer;

import com.kp.async.Consumer;
import com.kp.async.MessageProvider;
import com.kp.messaging.rabbit.client.RabbitMQChannelHandleDelegate;
import com.kp.messaging.rabbit.client.RabbitMQChannelHandler;
import com.kp.messaging.rabbit.client.RabbitMQQueueConfig;
import com.kp.messaging.rabbit.client.connection.RabbitMQConnection;
import com.kp.common.data.message.IMessage;
import com.kp.common.log.Loggable;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownListener;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class RabbitMQConsumer<I extends IMessage>
        implements Loggable, RabbitMQChannelHandler, RabbitMQRoutingConsumer, Consumer<I> {
     protected String tagConsumer;
    ShutdownListener channelShutdownListener;
    private boolean autoRegisterConsumer = true;
    private Map<Integer, MessageProvider<I, RabbitMQChannelHandleDelegate>> messageProviders = new ConcurrentHashMap<>();
    private String queueName;
    private RabbitMQQueueConfig queueConfig;
    private RabbitMQChannelHandleDelegate channelHandlerDelegate;
    private Set<String> waitingForBindRoutingKeys = new HashSet<>();
    private RabbitMQConnection rabbitMQConnection;

    public boolean isAutoRegisterConsumer() {
        return autoRegisterConsumer;
    }

    public void setAutoRegisterConsumer(boolean autoRegisterConsumer) {
        this.autoRegisterConsumer = autoRegisterConsumer;
    }



    protected void addShutdownListener(ShutdownListener channelShutdownListener) {
        this.channelShutdownListener = channelShutdownListener;
    }

    @Override
    public void addMessageProvider(int id, MessageProvider messageProvider) {
        if (messageProvider == null) {
            throw new NullPointerException("MessageProvider has not be null");
        }
        messageProviders.put(id, messageProvider);
    }

    @Override
    public void removeMesssageProvider(int id) {
        messageProviders.remove(id);

    }


    public RabbitMQQueueConfig getQueueConfig() {
        return queueConfig;
    }

    protected void setQueueConfig(RabbitMQQueueConfig queueConfig) {
        this.queueConfig = queueConfig;
    }

    public RabbitMQConnection getRabbitMQConnection() {
        return rabbitMQConnection;
    }

    protected void setRabbitMQConnection(RabbitMQConnection rabbitMQConnection) {
        this.rabbitMQConnection = rabbitMQConnection;
    }

    @Override
    public void init() throws Exception {

        channelHandlerDelegate = new RabbitMQChannelHandleDelegate(getRabbitMQConnection(), this, channelShutdownListener);
        getLogger().info(this.getClass().getSimpleName() + ": init {}", this);

    }

    @Override
    public void start() throws Exception {
        this.channelHandlerDelegate.start();
        getLogger().info(this.getClass().getSimpleName() + ": start {}", this);

    }

    @Override
    public void stop() throws Exception {
        getLogger().info(this.getClass().getSimpleName() + ": stopping {}", this);
        this.channelHandlerDelegate.close();

        tagConsumer = null;
    }

    protected Channel getChannel() {
        return this.channelHandlerDelegate.getChannel();
    }


    protected void initQueue() throws IOException {


        Channel channel = this.getChannel();

        if (queueConfig.getQos() >= 0) {
            channel.basicQos(queueConfig.getQos());
        }
        if (!queueConfig.isDeclare()) {
            this.queueName = queueConfig.getQueueName();
            return;
        }
        // declare queue
        this.queueName = queueConfig.getQueueName();
        if (queueName != null && queueName.trim().length() > 0) {
            channel.queueDeclare(queueName, queueConfig.isDurable(), queueConfig.isExclusive(),
                    queueConfig.isAutoDelete(), queueConfig.getArguments());
        } else {
            queueName = channel.queueDeclare().getQueue();
        }

        // setting Qos (perfect count) value


        // bind to an exchange
        if (!queueConfig.getExchangeName().isEmpty()) {
            channel.exchangeDeclare(queueConfig.getExchangeName(), queueConfig.getExchangeType());
            if (queueConfig.getRoutingKey() != null) {
                // only execute by when routing key is specific
                this.queueBind(queueConfig.getRoutingKey());
            }
            if (waitingForBindRoutingKeys.size() > 0) {
                for (String routingKey : waitingForBindRoutingKeys) {
                    if (!routingKey.equals(queueConfig.getRoutingKey())) {
                        this.queueBind(routingKey);
                    }
                }
            }
        }
    }

    public boolean isReady() {
        return this.getChannel() != null && this.getChannel().isOpen();
    }

    @Override
    public final void onChannelReady(Channel channel) throws IOException {
        if (isAutoRegisterConsumer()) {
            register();
        }
    }

    public synchronized void unregister() throws IOException {
        getLogger().info("unregister queue with tag {}", tagConsumer);
        if (getChannel() != null && getChannel().isOpen()) {

            if (tagConsumer != null) {
                try {
                    getChannel().basicCancel(tagConsumer);

                } catch (Exception e) {
                    getLogger().error("unregister error {}", e);
                }
            }
        } else {
            throw new IOException("channel isn't ready ");
        }
        tagConsumer = null;
    }

    public synchronized void register() throws IOException {
        unregister();
        if (getChannel() != null && getChannel().isOpen()) {
            this.initQueue();
            tagConsumer = getChannel().basicConsume(this.getQueueName(), queueConfig.isAutoAck(),
                    new DefaultConsumer(getChannel()) {
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
                                                   byte[] body) throws IOException {

                            RabbitMQConsumer.this.handleDelivery(consumerTag, envelope, properties, body);
                            RabbitMQConsumer.this.afterDelivery(consumerTag, envelope, properties, body);


                        }
                    });
            getLogger().info("register queue with tag  {}", tagConsumer);

        } else {
            throw new IOException("channel isn't ready ");
        }
    }

    protected void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
                                  byte[] body) {


        I kMessage = null;
        Throwable throwable = null;
        try {
            kMessage = (I) getMessageParser().decodeMessage(body);

        } catch (Exception e) {
            throwable = e;
            getLogger().error("deserialize message Eror {}", e);
        }
        for (MessageProvider messageProvider : messageProviders.values()) {

            messageProvider.onMessageReceived(kMessage, channelHandlerDelegate, throwable);

        }


    }

    protected abstract void afterDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
                                          byte[] body);


    protected abstract void handleResult(String consumerTag, Envelope envelope, BasicProperties properties, I result);

    public String getQueueName() {
        return this.queueName;
    }

    @Override
    public void queueBind(String rountingKey) throws IOException {
        if (this.isReady()) {
            if (queueConfig.getExchangeName().isEmpty()) {
                getLogger().warn("Consumer has an empty exchange name, binding a new rountingKey may cause the unexpected errors");
            }
            getChannel().queueBind(this.getQueueName(), queueConfig.getExchangeName(),
                    rountingKey);
        } else {
            this.waitingForBindRoutingKeys.add(rountingKey);
        }
    }

    @Override
    public void queueUnbind(String rountingKey) throws IOException {
        if (!this.isReady()) {
            return;
        }
        if (queueConfig.getExchangeName().isEmpty()) {
            getLogger().warn("Consumer has an empty exchange name, unbinding a rountingKey may cause the unexpected errors");
        }
        getChannel().queueUnbind(this.getQueueName(), queueConfig.getExchangeName(), rountingKey);
    }

}
