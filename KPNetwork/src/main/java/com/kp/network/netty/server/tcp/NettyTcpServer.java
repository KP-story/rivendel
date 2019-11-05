package com.kp.network.netty.server.tcp;

import com.kp.common.data.message.IMessage;
import com.kp.common.data.message.IMessageParser;
import com.kp.network.FutureManager;
import com.kp.network.connection.IConnectionManager;
import com.kp.network.event.impl.IConnectionListenerManager;
import com.kp.network.netty.AbstractNettyConnection;
import com.kp.network.netty.NettyChannelInbound;
import com.kp.network.netty.codec.NettyMessageDecoder;
import com.kp.network.netty.codec.NettyMessageEncoder;
import com.kp.network.netty.server.AbstractNettySocketServer;
import com.kp.network.netty.server.NettyServerConnection;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.net.InetAddress;
import java.util.concurrent.Future;

import static io.netty.channel.ChannelOption.*;

public abstract class NettyTcpServer<T extends IMessage> extends AbstractNettySocketServer<T> {

    protected IMessageParser<ByteBuf, T> messageParser;
    protected InetAddress localAddress;

    public NettyTcpServer(IConnectionManager<T, Channel> connectionManager, IConnectionListenerManager<T> connectionListenerManager, IMessageParser<ByteBuf, T> messageParser) throws Exception {
        super(connectionManager, connectionListenerManager);
        this.messageParser = messageParser;
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
                ch.pipeline().addLast(new NettyMessageDecoder<T>(messageParser));
                ch.pipeline().addLast(new NettyMessageEncoder<T>(messageParser));
                ch.pipeline().addLast(new NettyChannelInbound<T>(connectionManager, connectionListenerManager) {
                    @Override
                    public AbstractNettyConnection<T> newConnection() {
                        NettyServerConnection nettyServerConnection = null;
                        try {
                            nettyServerConnection = new NettyServerConnection<T>(createEntryConnectionListenerManager()) {
                                @Override
                                protected FutureManager<String, T> createFutureManager() {
                                    return NettyTcpServer.this.newFutureManager();
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
    public void addLocalAddress(InetAddress address) {
        this.localAddress = address;
    }


    @Override
    public InetAddress[] getLocalAddress() {
        InetAddress[] a = {localAddress};
        return a;
    }
}
