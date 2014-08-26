package com.anagram.server.tcp;

import java.nio.ByteOrder;

/**
 * Created by rmanaloto on 8/13/14.
 */
public class AnagramTCPServerConfiguration {

    private int receiveBufferSize = Integer.getInteger("anagram.tcp.server.configuration.receive.buffer.size", 256 * 1024);
    private int sendBufferSize = Integer.getInteger("anagram.tcp.server.configuration.send.buffer.size", 256 * 1024);
    private int readBufferSize = Integer.getInteger("anagram.tcp.server.configuration.read.buffer.size", 256 * 1024);
    private int writeBufferSize = Integer.getInteger("anagram.tcp.server.configuration.write.buffer.size", 256 * 1024);
    private boolean reuseAddress = Boolean.valueOf(System.getProperty("anagram.tcp.server.configuration.reuse.address", "true"));
    private boolean keepAlive = Boolean.valueOf(System.getProperty("anagram.tcp.server.configuration.keepalive", "true"));
    private boolean tcpNoDelay = Boolean.valueOf(System.getProperty("anagram.tcp.server.configuration.tcp_no_delay", "true"));
    private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
    private int port = Integer.getInteger("anagram.tcp.server.configuration.port", 5555);

    private boolean clientReuseAddress = Boolean.valueOf(System.getProperty("anagram.tcp.server.configuration.client.reuse.address", "true"));
    private boolean clientKeepAlive = Boolean.valueOf(System.getProperty("anagram.tcp.server.configuration.client.keepalive", "true"));
    private boolean clientTCPNoDelay = Boolean.valueOf(System.getProperty("anagram.tcp.server.configuration.client.tcp_no_delay", "true"));
    private int clientReceiveBufferSize = Integer.getInteger("anagram.tcp.server.configuration.receive.buffer.size", 256 * 1024);
    private int clientSendBufferSize = Integer.getInteger("anagram.tcp.server.configuration.send.buffer.size", 256 * 1024);
    private int clientReadBufferSize = Integer.getInteger("anagram.tcp.server.configuration.client.read.buffer.size", 256 * 1024);
    private int cientWriteBufferSize = Integer.getInteger("anagram.tcp.server.configuration.client.write.buffer.size", 256 * 1024);

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public int getSendBufferSize() {
        return sendBufferSize;
    }

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public boolean isReuseAddress() {
        return reuseAddress;
    }

    public void setReuseAddress(boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }

    public int getReadBufferSize() {
        return readBufferSize;
    }

    public void setReadBufferSize(int readBufferSize) {
        this.readBufferSize = readBufferSize;
    }

    public int getWriteBufferSize() {
        return writeBufferSize;
    }

    public void setWriteBufferSize(int writeBufferSize) {
        this.writeBufferSize = writeBufferSize;
    }

    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    public void setByteOrder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public boolean isClientReuseAddress() {
        return clientReuseAddress;
    }

    public void setClientReuseAddress(boolean clientReuseAddress) {
        this.clientReuseAddress = clientReuseAddress;
    }

    public boolean isClientKeepAlive() {
        return clientKeepAlive;
    }

    public void setClientKeepAlive(boolean clientKeepAlive) {
        this.clientKeepAlive = clientKeepAlive;
    }

    public boolean isClientTCPNoDelay() {
        return clientTCPNoDelay;
    }

    public void setClientTCPNoDelay(boolean clientTCPNoDelay) {
        this.clientTCPNoDelay = clientTCPNoDelay;
    }

    public int getClientReadBufferSize() {
        return clientReadBufferSize;
    }

    public void setClientReadBufferSize(int clientReadBufferSize) {
        this.clientReadBufferSize = clientReadBufferSize;
    }

    public int getCientWriteBufferSize() {
        return cientWriteBufferSize;
    }

    public void setCientWriteBufferSize(int cientWriteBufferSize) {
        this.cientWriteBufferSize = cientWriteBufferSize;
    }

    public int getClientReceiveBufferSize() {
        return clientReceiveBufferSize;
    }

    public void setClientReceiveBufferSize(int clientReceiveBufferSize) {
        this.clientReceiveBufferSize = clientReceiveBufferSize;
    }

    public int getClientSendBufferSize() {
        return clientSendBufferSize;
    }

    public void setClientSendBufferSize(int clientSendBufferSize) {
        this.clientSendBufferSize = clientSendBufferSize;
    }
}
