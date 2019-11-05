package com.kp.messaging.rabbit.client.consumer;

import java.io.IOException;

public interface RabbitMQRoutingConsumer {

    void queueBind(String rountingKey) throws IOException;

    void queueUnbind(String rountingKey) throws IOException;
}
