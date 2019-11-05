package com.kp.sctp;

import com.kp.common.data.message.IMessageParser;
import com.kp.network.DefaultFutureManager;
import com.kp.network.FutureManager;
import com.kp.network.connection.IConnection;
import com.kp.network.event.impl.ConnectionListener;
import com.kp.network.event.impl.DefaultConnectionListenerManager;
import com.kp.network.event.impl.IConnectionListenerManager;
import com.kp.StringMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.SctpMessage;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;


public class SctpClientTest {


    public static void createClient() throws Exception {

        FutureManager<String, StringMessage> futureManager = new DefaultFutureManager<>();
        IMessageParser<SctpMessage, StringMessage> messageParser = new StringSctpMesasgeParser();
        IConnectionListenerManager<StringMessage> connectionListenerManager = new DefaultConnectionListenerManager<>();
        SctpStringSocketClient stringSocketClient = new SctpStringSocketClient(connectionListenerManager, messageParser);
        stringSocketClient.setTimeout(20000);
        stringSocketClient.addRemoteAddress(new InetSocketAddress("localhost", 9090));
        stringSocketClient.addLocalAddress(new InetSocketAddress("localhost", 9022));
        stringSocketClient.setNWorker(12);
        connectionListenerManager.init();
        stringSocketClient.connect();

        stringSocketClient.addConnectionListener("khanhlv", new ConnectionListener<StringMessage>() {
            @Override
            public void connectionOpened(IConnection connection) {
                System.out.println("open");
            }

            @Override
            public void connectionClosed(IConnection connection) {
                System.out.println("close");

            }

            @Override
            public void messageReceived(IConnection connection, StringMessage message) {
                System.out.println("message den " + message.content);

            }

            @Override
            public void internalError(IConnection connection, StringMessage message, Throwable cause) {

            }

        });
        while (true) {
            stringSocketClient.send(new StringMessage("hi khanhlv"));
            Thread.sleep(1000);
        }

    }

    public static void main(String[] args) throws Exception {

        createClient();
//client1();


    }

    public static void client1() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap clientBootstrap = new Bootstrap();

            clientBootstrap.group(group);
            clientBootstrap.channel(NioSocketChannel.class);
            clientBootstrap.remoteAddress(new InetSocketAddress("localhost", 9090));
            clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new ClientHandler());
                }
            });
            ChannelFuture channelFuture = clientBootstrap.connect().sync();
            try {
                channelFuture.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static class ClientHandler extends SimpleChannelInboundHandler {

        @Override
        public void channelActive(ChannelHandlerContext channelHandlerContext) {
            channelHandlerContext.writeAndFlush(Unpooled.copiedBuffer("Netty Rocks!", CharsetUtil.UTF_8));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) {
            cause.printStackTrace();
            channelHandlerContext.close();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        }
    }

}
