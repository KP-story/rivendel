package com.kp.network.netty.client.tcp;

import com.kp.common.data.message.IMessage;
import com.kp.common.data.message.IMessageParser;
import com.kp.network.event.impl.IConnectionListenerManager;
import com.kp.network.netty.AbstractNettyConnection;
import com.kp.network.netty.NettyChannelInbound;
import com.kp.network.netty.client.NettyClientConnection;
import com.kp.network.netty.codec.NettyMessageDecoder;
import com.kp.network.netty.codec.NettyMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.Future;

public abstract class NettyTcpClient<T extends IMessage> extends NettyClientConnection<ByteBuf, T> {
    private SocketAddress localAddress;

    public NettyTcpClient(IConnectionListenerManager connectionListenerManager, IMessageParser<ByteBuf, T> messageParser) {
        super(connectionListenerManager, messageParser);
    }


    @Override
    public SocketAddress[] getLocalAddress() {
        SocketAddress[] a = {localAddress};
        return a;
    }

    @Override
    protected Future _connect() throws IOException, InterruptedException {
        workerGroup = new NioEventLoopGroup(nworker);
        Bootstrap b = new Bootstrap();

        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout);
        applyOptions(b);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                applyChannelHandler(ch);

                ch.pipeline().addLast(new NettyMessageDecoder<T>(messageParser));
                ch.pipeline().addLast(new NettyMessageEncoder<T>(messageParser));
                ch.pipeline().addLast(new NettyChannelInbound<T>(null) {
                    @Override
                    public AbstractNettyConnection<T> newConnection() {
                        return NettyTcpClient.this;
                    }
                });

            }
        });
        ChannelFuture channelFuture = null;
        if (localAddress == null) {
            channelFuture = b.connect(remoteAddress).sync();

        } else {
            channelFuture = b.connect(remoteAddress, localAddress).sync();

        }
        if (channelFuture.await().isSuccess()) {
            getLogger().info("Connect to server {} ", remoteAddress.toString());
            context = channelFuture.channel();
            createdTime = System.currentTimeMillis();
            return channelFuture;
        } else {
            throw new IOException(" netty  socket tcp client " + " error, unable to connect to "
                    + remoteAddress.toString());
        }


    }

    @Override
    public void addLocalAddress(SocketAddress inetAddress) {
        localAddress = inetAddress;
    }


}
