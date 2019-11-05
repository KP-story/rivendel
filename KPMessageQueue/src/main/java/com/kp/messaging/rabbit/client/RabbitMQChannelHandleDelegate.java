package com.kp.messaging.rabbit.client;

import com.kp.messaging.rabbit.client.connection.RabbitMQConnection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ShutdownListener;

import java.io.IOException;

public class RabbitMQChannelHandleDelegate extends RabbitMQChannelWrapper {

    private RabbitMQChannelHandler handler;

    public RabbitMQChannelHandleDelegate(RabbitMQConnection connection, RabbitMQChannelHandler handler, ShutdownListener shutdownListener) {
        super(connection);
        this.handler = handler;
        addShutdownListener(shutdownListener);

    }

    @Override
    protected void onChannelReady(Channel channel) throws IOException {
        if (this.handler != null) {
            this.handler.onChannelReady(channel);
        }
    }

    @Override
    public Channel getChannel() {
        return super.getChannel();
    }

    @Override
    protected void _stop() {

    }

    @Override
    protected void _start() {

    }
}
