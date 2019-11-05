package com.kp.messaging.rabbit.client;

import com.rabbitmq.client.Channel;

import java.io.IOException;

public interface RabbitMQChannelHandler {

    void onChannelReady(Channel channel) throws IOException;
}
