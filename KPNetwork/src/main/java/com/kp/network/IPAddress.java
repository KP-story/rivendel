package com.kp.network;

public class IPAddress {
    private String ip;
    private int port;

    public IPAddress(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public IPAddress(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
