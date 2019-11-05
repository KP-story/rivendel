package com.kp.messaging.rabbit.client.consumer;

import com.kp.common.data.message.IMessage;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class RabbitMQWorkerConsumer<I extends IMessage> extends RabbitMQConsumer<I> {


    private Set<Long> unackedDeliveryTags = new CopyOnWriteArraySet<>();


    protected final void ack(long deliveryTag) {
        if (!getQueueConfig().isAutoAck()) {
            try {
                getChannel().basicAck(deliveryTag, false);
                if (getUnackedDeliveryTags().contains(deliveryTag)) {
                    getUnackedDeliveryTags().remove(deliveryTag);
                }
            } catch (IOException e) {
                getLogger().error("Cannot send ack for deliveryTag: " + deliveryTag, e);
                getUnackedDeliveryTags().add(deliveryTag);
            }
        }
    }


    public Collection<Long> getUnackedDeliveryTags() {
        return unackedDeliveryTags;
    }

}
