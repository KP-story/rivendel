package com.kp.taskmanager.network.server;

import com.kp.common.data.message.IMessageParser;
import com.kp.network.DefaultFutureManager;
import com.kp.network.FutureManager;
import com.kp.network.connection.IConnectionManager;
import com.kp.network.event.impl.DefaultConnectionListenerManager;
import com.kp.network.event.impl.IConnectionListenerManager;
import com.kp.network.netty.AbstractNettyConnection;
import com.kp.network.netty.NettyChannelInbound;
import com.kp.network.netty.codec.NettyMessageDecoder;
import com.kp.network.netty.codec.NettyMessageEncoder;
import com.kp.network.netty.server.NettyServerConnection;
import com.kp.network.netty.server.tcp.NettyTcpServer;
import com.kp.taskmanager.network.codec.VOMessage;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.concurrent.Future;

import static io.netty.channel.ChannelOption.*;

public class JsonNettyTcpServer extends NettyTcpServer<VOMessage> {
    public JsonNettyTcpServer(IConnectionManager<VOMessage, Channel> connectionManager, IConnectionListenerManager<VOMessage> connectionListenerManager, IMessageParser<ByteBuf, VOMessage> messageParser) throws Exception {
        super(connectionManager, connectionListenerManager, messageParser);
    }

    @Override
    protected IConnectionListenerManager<VOMessage> createEntryConnectionListenerManager() {
        return new DefaultConnectionListenerManager<>();
    }

    @Override
    protected Future _init() throws Exception {

        bossGroup = new NioEventLoopGroup(nboot);
        workerGroup = new NioEventLoopGroup(nworker);
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childOption(SO_RCVBUF, rcvbuf);
        bootstrap.option(SO_RCVBUF, rcvbuf);
        applyOptions(bootstrap);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new JsonObjectDecoder());
                ch.pipeline().addLast(new NettyMessageDecoder<VOMessage>(messageParser));
                ch.pipeline().addLast(new NettyMessageEncoder<VOMessage>(messageParser));
                ch.pipeline().addLast(new NettyChannelInbound<VOMessage>(connectionManager, connectionListenerManager) {
                    @Override
                    public AbstractNettyConnection<VOMessage> newConnection() {
                        NettyServerConnection nettyServerConnection = null;
                        try {
                            nettyServerConnection = new NettyServerConnection<VOMessage>(createEntryConnectionListenerManager()) {
                                @Override
                                protected FutureManager<String, VOMessage> createFutureManager() {
                                    return JsonNettyTcpServer.this.newFutureManager();
                                }
                            };
                        } catch (Exception e) {
                            return null;
                        }
                        nettyServerConnection.setTimeout(timeout);

                        return nettyServerConnection;

                    }
                });
                ch.pipeline().addLast(new ReadTimeoutHandler(timeout));


            }
        });

        bootstrap.option(CONNECT_TIMEOUT_MILLIS, timeout).option(SO_KEEPALIVE, true);

        // Bind and start to accept incoming connections.
        ChannelFuture channelFuture = bootstrap.bind(localAddress, getLocalPort());

        return channelFuture;
    }


    @Override
    public void applyOptions(ServerBootstrap b) {

    }

    @Override
    protected FutureManager<String, VOMessage> newFutureManager() {
        return new DefaultFutureManager<VOMessage>();
    }
}
