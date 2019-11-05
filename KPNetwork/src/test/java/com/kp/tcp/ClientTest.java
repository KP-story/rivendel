package com.kp.tcp;


import com.kp.common.data.message.IMessageParser;
import com.kp.network.DefaultFutureManager;
import com.kp.network.FutureManager;
import com.kp.network.connection.IConnection;
import com.kp.network.event.impl.ConnectionListener;
import com.kp.network.event.impl.DefaultConnectionListenerManager;
import com.kp.network.event.impl.IConnectionListenerManager;
import com.kp.StringMesasgeParser;
import com.kp.StringMessage;
import io.netty.buffer.ByteBuf;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;


public class ClientTest {


    public static void createClient() throws Exception {

        FutureManager<String, StringMessage> futureManager = new DefaultFutureManager<>();
        futureManager.init();
        IMessageParser<ByteBuf, StringMessage> messageParser = new StringMesasgeParser();
        IConnectionListenerManager<StringMessage> connectionListenerManager = new DefaultConnectionListenerManager<>();
        StringSocketClient stringSocketClient = new StringSocketClient(connectionListenerManager, messageParser);
        stringSocketClient.setTimeout(20000);
        stringSocketClient.addRemoteAddress(new InetSocketAddress("localhost", 9090));
//        stringSocketClient.addLocalAddress(new InetSocketAddress("localhost",9022));
        connectionListenerManager.init();
        stringSocketClient.setNWorker(12);

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
        stringSocketClient.connect().get();

        while (true) {
            try {
                Future future = stringSocketClient.sendAsync(new StringMessage("khanhlv=ak,id=id+1"));
                CompletableFuture completableFuture = (CompletableFuture) future;


            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(500);
        }


    }

    public static void main(String[] args) throws Exception {

        createClient();
//client1();


    }


}
