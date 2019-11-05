package com.kp.network.netty.server.sctp;

import com.kp.common.data.message.IMessage;
import com.kp.common.data.message.IMessageParser;
import com.kp.network.FutureManager;
import com.kp.network.connection.IConnectionManager;
import com.kp.network.event.impl.IConnectionListenerManager;
import com.kp.network.netty.AbstractNettyConnection;
import com.kp.network.netty.NettyChannelInbound;
import com.kp.network.netty.codec.NettySctpMessageDecoder;
import com.kp.network.netty.codec.NettySctpMessageEncoder;
import com.kp.network.netty.server.AbstractNettySocketServer;
import com.kp.network.netty.server.NettyServerConnection;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.SctpChannel;
import io.netty.channel.sctp.SctpMessage;
import io.netty.channel.sctp.SctpServerChannel;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.handler.codec.sctp.SctpMessageCompletionHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public abstract class NettySctpServer<T extends IMessage> extends AbstractNettySocketServer<T> {

    private IMessageParser<SctpMessage, T> messageParser;

    private List<InetAddress> localAddresses = new ArrayList<>();


    public NettySctpServer(IConnectionManager<T, Channel> connectionManager, IConnectionListenerManager<T> connectionListenerManager, IMessageParser<SctpMessage, T> messageParser) throws Exception {
        super(connectionManager, connectionListenerManager);
        this.messageParser = messageParser;
    }

    @Override
    protected Future _init() throws Exception {

        ServerBootstrap b = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(nboot);
        workerGroup = new NioEventLoopGroup(nworker);
        b.group(bossGroup, workerGroup);
        b.channel(NioSctpServerChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout);
        applyOptions(b);
        b.childHandler(new ChannelInitializer<SctpChannel>() {
            @Override
            public void initChannel(SctpChannel ch) throws Exception {
                ch.pipeline().addLast(new SctpMessageCompletionHandler());
                ch.pipeline().addLast(new NettySctpMessageDecoder<T>(messageParser));
                ch.pipeline().addLast(new NettySctpMessageEncoder<T>(messageParser));
                ch.pipeline().addLast(new NettyChannelInbound(connectionManager, connectionListenerManager) {
                    @Override
                    public AbstractNettyConnection newConnection() {
                        NettyServerConnection nettyServerConnection = null;
                        try {
                            nettyServerConnection = new NettyServerConnection<T>(createEntryConnectionListenerManager()) {
                                @Override
                                protected FutureManager<String, T> createFutureManager() {
                                    return NettySctpServer.this.newFutureManager();
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

        InetAddress primaryLocalAddress = localAddresses.get(0);
        // Bind the server to primary address.
        ChannelFuture channelFuture = b.bind(primaryLocalAddress, localPort).sync();

        // Get the underlying sctp channel
        SctpServerChannel serverChannelSctp = (SctpServerChannel) channelFuture.channel();

        // Bind the secondary address.
        // Please note that, bindAddress in the client channel should be done before connecting if you have not
        // enable Dynamic Address Configuration. See net.sctp.addip_enable kernel param
        if (this.localAddresses.size() > 1) {
            for (int i = 1; i < localAddresses.size(); i++) {
                serverChannelSctp.bindAddress(localAddresses.get(i)).sync();
            }
        }
        this.serverChannel = channelFuture.channel();
        return channelFuture;
    }

    @Override
    public void addLocalAddress(InetAddress address) {
        this.localAddresses.add(address);
    }


    @Override
    public InetAddress[] getLocalAddress() {
        InetAddress[] a = (InetAddress[]) localAddresses.toArray();
        return a;
    }
}
