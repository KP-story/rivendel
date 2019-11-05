package com.kp.tcp;


import com.kp.common.data.message.IMessageParser;
import com.kp.network.connection.DefaultConnectionManager;
import com.kp.network.connection.IConnection;
import com.kp.network.connection.IConnectionManager;
import com.kp.network.event.impl.ConnectionListener;
import com.kp.network.event.impl.DefaultConnectionListenerManager;
import com.kp.network.event.impl.IConnectionListenerManager;
import com.kp.StringMesasgeParser;
import com.kp.StringMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.concurrent.CountDownLatch;

public class ServerTest {
    static boolean i = true;

    public static void main(String[] args) throws Exception {

        createServer();
    }

    public static void createServer() throws Exception {


        IConnectionManager<StringMessage, Channel> connectionManager = new DefaultConnectionManager<>();
        IConnectionListenerManager<StringMessage> connectionListenerManager = new DefaultConnectionListenerManager<>();
        IMessageParser<ByteBuf, StringMessage> messageParser = new StringMesasgeParser();


        StringSocketServer stringSocketServer = new StringSocketServer(connectionManager, connectionListenerManager, messageParser);
        InetAddress inetAddress = InetAddressByIPv4("0.0.0.0");
        stringSocketServer.addLocalAddress(inetAddress);
        stringSocketServer.addLocalPort(9090);
        stringSocketServer.setNboot(11);
        stringSocketServer.setNworker(122);
        stringSocketServer.setRcvbuf(10);
        stringSocketServer.setTimeout(12000);
        CountDownLatch lock = new CountDownLatch(1);
        stringSocketServer.addConnectionListener("khanhlv", new ConnectionListener<StringMessage>() {
            @Override
            public void connectionOpened(IConnection connection) {
                System.out.println("connect" + connection);


            }

            @Override
            public void connectionClosed(IConnection connection) {
                System.out.println("disconnect" + connection);


            }

            @Override
            public void messageReceived(IConnection connection, StringMessage message) {
                try {
                    if (i) {
                        i = false;

                        lock.await();


                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("tinhan den" + connection.getId());

            }

            @Override
            public void internalError(IConnection connection, StringMessage message, Throwable cause) {


            }
        });

        stringSocketServer.init();
        while (true) {
            stringSocketServer.broadcastMessage(new StringMessage("dau dit kinh di,id=12"));
            Thread.sleep(1000);

        }

    }

    public static InetAddress InetAddressByIPv4(String address) {
        StringTokenizer addressTokens = new StringTokenizer(address, ".");
        if (addressTokens.countTokens() == 4) {
            byte[] bytes = new byte[]{getByBytes(addressTokens), getByBytes(addressTokens), getByBytes(addressTokens), getByBytes(addressTokens)};

            try {
                return InetAddress.getByAddress(bytes);
            } catch (UnknownHostException var4) {
                return null;
            }
        } else {
            return null;
        }
    }

    private static byte getByBytes(StringTokenizer addressTokens) {
        int word = Integer.parseInt(addressTokens.nextToken());
        return (byte) (word & 255);
    }

    public static InetAddress InetAddressByIPv6(String address) {
        StringTokenizer addressTokens = new StringTokenizer(address, ":");
        byte[] bytes = new byte[16];
        if (addressTokens.countTokens() != 8) {
            return null;
        } else {
            for (int count = 0; addressTokens.hasMoreTokens(); ++count) {
                int word = Integer.parseInt(addressTokens.nextToken(), 16);
                bytes[count * 2] = (byte) (word >> 8 & 255);
                bytes[count * 2 + 1] = (byte) (word & 255);
            }

            try {
                return InetAddress.getByAddress(bytes);
            } catch (UnknownHostException var5) {
                return null;
            }
        }
    }
}
