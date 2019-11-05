package com.kp.network.netty;

import com.kp.common.data.message.IMessage;
import com.kp.common.log.Loggable;
import com.kp.network.connection.IConnectionManager;
import com.kp.network.event.impl.IConnectionListenerManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.timeout.ReadTimeoutException;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;


public abstract class NettyChannelInbound<T extends IMessage> extends ChannelInboundHandlerAdapter implements Loggable {

    protected IConnectionManager<T, Channel> connectionManager;
    protected IConnectionListenerManager<T> connectionListenerManager;
    private AbstractNettyConnection<T> connection;

    public NettyChannelInbound(IConnectionManager<T, Channel> connectionManager) {
        this.connectionManager = connectionManager;
    }

    public NettyChannelInbound(IConnectionManager<T, Channel> connectionManager, IConnectionListenerManager<T> connectionListenerManager) {
        this.connectionManager = connectionManager;
        this.connectionListenerManager = connectionListenerManager;
    }

    public abstract AbstractNettyConnection<T> newConnection();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        getLogger().debug("socket active:  client {}", ctx.channel().remoteAddress().toString());
        try {

            connection = newConnection();
            connection.onConnected(ctx.channel());
            if (connectionManager != null) {
                connectionManager.add(connection.getId(), connection);
            }
            try {
                if (connectionListenerManager != null) {
                    connectionListenerManager.fireConnectionOpened(connection);
                }
            } catch (Exception e) {
                getLogger().error("fireConnectionOpened  client {} error ", ctx.channel().remoteAddress().toString(), e);

            }
            try {
                connection.fireConnectionOpened(connection);
            } catch (Exception e) {
                getLogger().error("connection.fireConnectionOpened  client {} error", ctx.channel().remoteAddress().toString(), e);

            }
        } catch (Exception e) {
            ctx.close();
            getLogger().error("init futureManager  client {} error ", ctx.channel().remoteAddress().toString(), e);

        }


    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        if (connection != null) {
            if (!connection.isActive()) {
                getLogger().debug("socket inactive:  client {}", ctx.channel().remoteAddress().toString());

            }
            if (connectionManager != null) {
                connectionManager.remove(connection.getId());
            }
            try {
                if (connectionListenerManager != null) {
                    connectionListenerManager.fireConnectionClosed(connection);
                }

            } catch (Exception e) {
                getLogger().error("connectionListenerManager.fireConnectionClosed {} ", ctx.channel().remoteAddress().toString(), e);

            }
            try {
                connection.fireConnectionClosed(connection);

            } catch (Exception e) {
                getLogger().error("connection. fireConnectionClosed {} error ", ctx.channel().remoteAddress().toString(), e);

            }
            connection.disconnect();
            connection = null;

            getLogger().debug("socket inactive:  client {}", ctx.channel().remoteAddress().toString());
        } else {
            getLogger().debug("not found iConnection   {}  ", ctx.channel().remoteAddress().toString());

        }

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);

        if (connection != null && connection.isConnected()) {


            T message = (T) msg;
            if (message.getId() != null) {
                try {
                    CompletableFuture<T> future = (CompletableFuture<T>) connection.getFutureManager().remove(message.getId());

                    if (future != null) {
                        future.complete(message);
                    } else {
                        getLogger().debug("not found Future {}", message);

                    }
                } catch (Exception e) {
                    getLogger().error("complete future has error {} {} ", message, connection, e);

                }
                try {
                    if (connectionListenerManager != null) {
                        connectionListenerManager.fireMessageReceived(connection, message);
                    }
                } catch (Exception e) {
                    getLogger().error("fireMessageReceived error {}  ", message, connection, e);

                }
                try {

                    connection.fireMessageReceived(connection, message);
                } catch (Exception e) {
                    getLogger().error("connection.fireMessageReceived error {}  ", message, connection, e);

                }
            } else {
                getLogger().debug("not found iConnection   {}  ", ctx.channel().remoteAddress().toString());

            }

        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);

        if (connection != null) {
            try {
                connection.fireInternalError(connection, null, cause);
            } catch (Exception e) {
                getLogger().debug("connection.fireInternalError   {}  ", ctx.channel().remoteAddress().toString());
            }
            try {
                if (connectionListenerManager != null) {
                    connectionListenerManager.fireInternalError(connection, null, cause);

                }
            } catch (Exception e) {
                getLogger().debug("fireInternalError   {}  ", ctx.channel().remoteAddress().toString());
            }
        } else {
            getLogger().debug("not found iConnection   {}  ", ctx.channel().remoteAddress().toString());

        }


        getLogger().error("error on socket session " + ctx.channel().remoteAddress().toString(), cause);
        if (cause instanceof ReadTimeoutException || cause instanceof DecoderException || (cause instanceof IOException && cause.getMessage().contains("Connection reset by peer"))) {
            try {
                ctx.close();
            } catch (Exception e) {
                getLogger().error("Cannot call channel inactive...", e);
            }

            try {

                if (connection != null) {
                    if (!connection.isActive()) {
                        getLogger().debug("socket inactive:  client {}", ctx.channel().remoteAddress().toString());

                    }
                    if (connectionManager != null) {
                        connectionManager.remove(connection.getId());
                    }
                    connection.disconnect();

                    connection = null;
                } else {
                    getLogger().debug("not found iConnection   {}  ", ctx.channel().remoteAddress().toString());

                }


            } catch (Exception e) {
                getLogger().error("Close future manager", e);

            }
        }

    }
}