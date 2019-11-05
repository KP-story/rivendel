package com.kp.messaging.rabbit.client.connection;

import com.kp.common.log.Loggable;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ShutdownListener;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RabbitMQConnection implements Closeable, Loggable {

    private Connection sourceConnection;
    private Map<String, Channel> channels;
    private ShutdownListener channelShutdownListener;

    public RabbitMQConnection(Connection newConnection) {
        this.sourceConnection = newConnection;
        this.channels = new ConcurrentHashMap<>();
        if (sourceConnection.getId() == null) {
            sourceConnection.setId(UUID.randomUUID().toString());
        }
    }

    @Override
    public void close() throws IOException {
        for (Channel channel : channels.values()) {
            this.releaseChannel(channel);
        }
        if (getSourceConnection() != null && getSourceConnection().isOpen()) {
            getSourceConnection().close();
        }
    }

    public Connection getSourceConnection() {
        return sourceConnection;
    }

    public void setSourceConnection(Connection sourceConnection) {
        this.sourceConnection = sourceConnection;
    }

    public Channel createChannel() throws IOException {
        Channel result = this.sourceConnection.createChannel();
        if (result != null) {
            this.channels.put(result.getConnection().getId() + ":" + result.getChannelNumber(), result);
            if (channelShutdownListener != null) {
                result.addShutdownListener(this.channelShutdownListener);
            }
        } else {
            throw new RuntimeException("create channel error, null");
        }
        return result;
    }

    public void releaseChannel(Channel channel) {
        if (channel != null) {

            try {

                String id = channel.getConnection().getId() + ":" + channel.getChannelNumber();
                if (this.channels.containsKey(id)) {

                    if (channelShutdownListener != null) {
                        channel.addShutdownListener(this.channelShutdownListener);
                    }
                    this.channels.remove(id);

                }
                if (channel.isOpen()) {
                    channel.close();
                }
            } catch (Exception e) {
                getLogger().error("cannot close channel", e);

            }
        }
    }
}
