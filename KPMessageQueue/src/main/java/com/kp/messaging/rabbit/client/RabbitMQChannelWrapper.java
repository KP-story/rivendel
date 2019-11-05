package com.kp.messaging.rabbit.client;

import com.kp.messaging.rabbit.client.connection.RabbitMQConnection;
import com.kp.common.data.message.IMessage;
import com.kp.common.log.Loggable;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ShutdownListener;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class RabbitMQChannelWrapper<I extends IMessage, T> implements Closeable, Loggable {

    private AtomicBoolean isConnected = new AtomicBoolean(false);
    private Channel channel;
    private AtomicBoolean isStopping = new AtomicBoolean(false);
    private RabbitMQConnection connection;
    private ShutdownListener channelShutdownListener;

    public RabbitMQChannelWrapper(RabbitMQConnection connection) {
        this.connection = connection;
    }

    public RabbitMQConnection getConnection() {
        return connection;
    }

    protected void addShutdownListener(ShutdownListener channelShutdownListener) {
        this.channelShutdownListener = channelShutdownListener;
    }

    public final void start() {
        if (!this.isConnected()) {
            synchronized (this) {
                if (!this.isConnected()) {
                    this.connect();
                    this._start();
                }
            }
        }
    }

    public final boolean isConnected() {
        return this.isConnected.get();
    }

    public boolean isOpen() {
        if (this.channel != null) {
            return channel.isOpen();
        }
        return false;


    }

    private void connect() {
        try {

            this.channel = connection.createChannel();
            this.isConnected.set(true);

            if (channelShutdownListener != null) {
                this.channel.addShutdownListener(this.channelShutdownListener);
            }
            this.onChannelReady(this.channel);
        } catch (IOException e) {
            try {
                getLogger().error("Unable to create channel", e);
                close();
            } catch (Exception e1) {
                getLogger().error("Unable to close channel due an error occur: ", e1);
            }
            this.channel = null;
            this.isConnected.set(false);
        }
    }

    protected abstract void onChannelReady(Channel channel) throws IOException;

    protected abstract void _stop();

    protected abstract void _start();

    @Override
    public final void close() {
        if (!this.isConnected()) {
            return;
        }
        this.isStopping.set(true);
        this._stop();
        if (this.channel != null && this.channel.isOpen()) {
            try {
                this.getConnection().releaseChannel(channel);
                if (this.channelShutdownListener != null) {
                    this.channel.removeShutdownListener(channelShutdownListener);
                }
            } catch (Exception e) {
                getLogger().debug("Channel close error", e);
            }
            this.channel = null;
        }
        this.isStopping.set(false);
        this.isConnected.set(false);
    }

    protected Channel getChannel() {
        return this.channel;
    }

}
