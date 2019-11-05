package com.kp.network.netty.client.sctp;

import com.kp.common.data.message.IMessage;
import com.kp.common.data.message.IMessageParser;
import com.kp.network.event.impl.IConnectionListenerManager;
import com.kp.network.netty.AbstractNettyConnection;
import com.kp.network.netty.NettyChannelInbound;
import com.kp.network.netty.client.NettyClientConnection;
import com.kp.network.netty.codec.NettySctpMessageDecoder;
import com.kp.network.netty.codec.NettySctpMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.SctpChannel;
import io.netty.channel.sctp.SctpMessage;
import io.netty.channel.sctp.nio.NioSctpChannel;
import io.netty.handler.codec.sctp.SctpMessageCompletionHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public abstract class NettySctpClient<T extends IMessage> extends NettyClientConnection<SctpMessage, T> {
    private List<SocketAddress> localAddresses = new ArrayList<>();

    public NettySctpClient(IConnectionListenerManager connectionListenerManager, IMessageParser<SctpMessage, T> messageParser) {
        super(connectionListenerManager, messageParser);
    }


    @Override
    public SocketAddress[] getLocalAddress() {
        SocketAddress[] a = (SocketAddress[]) localAddresses.toArray();
        return a;
    }

    @Override
    protected Future _connect() throws IOException, InterruptedException {
        workerGroup = new NioEventLoopGroup(nworker);
        Bootstrap b = new Bootstrap();

        b.group(workerGroup);
        b.channel(NioSctpChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout);
        applyOptions(b);
        b.handler(new ChannelInitializer<SctpChannel>() {
            @Override
            public void initChannel(SctpChannel ch) throws Exception {
                ch.pipeline().addLast(new SctpMessageCompletionHandler());
                ch.pipeline().addLast(new NettySctpMessageDecoder<T>(messageParser));
                ch.pipeline().addLast(new NettySctpMessageEncoder<T>(messageParser));
                ch.pipeline().addLast(new NettyChannelInbound(null) {
                    @Override
                    public AbstractNettyConnection newConnection() {
                        return NettySctpClient.this;
                    }
                });
                applyChannelHandler(ch);

            }
        });
        ChannelFuture channelFuture = null;
        ChannelFuture bindFuture = b.bind(localAddresses.get(0)).sync();
        context = bindFuture.channel();
        // Get the underlying sctp channel
        SctpChannel sctpChannel = (SctpChannel) context;

        // Bind the secondary address.
        // Please note that, bindAddress in the client channel should be done before connecting if you have not
        // enable Dynamic Address Configuration. See net.sctp.addip_enable kernel param
        if (this.localAddresses.size() > 1) {
            for (int i = 1; i < localAddresses.size(); i++) {
                InetSocketAddress socketAddress = (InetSocketAddress) localAddresses.get(i);
                InetAddress localSecondaryInetAddress = socketAddress.getAddress();
                sctpChannel.bindAddress(localSecondaryInetAddress).sync();
            }
        }
        // Finish connect
        channelFuture = bindFuture.channel().connect(remoteAddress);
        if (channelFuture.await().isSuccess()) {
            getLogger().info("Connect to server {} ", remoteAddress.toString());
            context = channelFuture.channel();
            createdTime = System.currentTimeMillis();
            return channelFuture;

        } else {
            connectedFuture.cancel(true);
            throw new IOException(" netty  socket tcp client " + " error, unable to connect to "
                    + remoteAddress.toString());
        }


    }

    @Override
    public void addLocalAddress(SocketAddress inetAddress) {
        localAddresses.add(inetAddress);
    }


}
