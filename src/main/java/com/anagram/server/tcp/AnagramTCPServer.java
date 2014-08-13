package com.anagram.server.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by rmanaloto on 8/12/14.
 */
public class AnagramTCPServer implements Runnable {

    private final Logger log = Logger.getLogger(getClass().getName());

    private volatile boolean active;
    private Selector selector;
    private int port = Integer.getInteger("anagram.server.tcp.port", 5555);

    private Map<SocketChannel, List<byte[]>> keepDataTrack = new HashMap<>();
    private ByteBuffer buffer = ByteBuffer.allocate(2 * 1024);
    private ServerSocketChannel serverSocketChannel;
    private NetworkInterface networkInterface;

    public void cancel() {
        this.active = false;
    }

    @Override
    public void run() {
        try {
            eventLoop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() throws IOException {
        selector = Selector.open();
        networkInterface = getNetworkInterface();
        serverSocketChannel = ServerSocketChannel.open();

        //check that both of them were successfully opened
        if ((serverSocketChannel.isOpen()) && (selector.isOpen())) {
            //configure non-blocking mode
            serverSocketChannel.configureBlocking(false);
            //set some options
            serverSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 256 * 1024);
            serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            //bind the server socket channel to port
            serverSocketChannel.bind(new InetSocketAddress(port));
            //register the current channel with the given selector
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            //display a waiting message while ... waiting!
            System.out.println("Waiting for connections ...");
        }

        serverSocketChannel.close();
    }

    private void eventLoop() throws IOException {
        try {
            init();
            while (!Thread.currentThread().isInterrupted() && active) {
                //wait for incoming events
                final int selectNow = selector.selectNow();
                //there is something to process on selected keys
                final Set<SelectionKey> selectionKeys = selector.selectedKeys();
                if(!selectionKeys.isEmpty()) {
                    Iterator<SelectionKey> keys = selectionKeys.iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = (SelectionKey) keys.next();
                        //prevent the same key from coming up again
                        keys.remove();
                        if (!key.isValid()) {
                            continue;
                        }
                        if (key.isAcceptable()) {
                            acceptOP(key, selector);
                        } else if (key.isReadable()) {
                            this.readOP(key);
                        } else if (key.isWritable()) {
                            this.writeOP(key);
                        }
                    }
                }
            }
        } finally {
            if(serverSocketChannel != null) {
                serverSocketChannel.close();
            }
            if(selector != null) {
                selector.close();
            }
        }

    }

    //isAcceptable returned true
    private void acceptOP(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverChannel.accept();
        socketChannel.configureBlocking(false);
        System.out.println("Incoming connection from: " + socketChannel.getRemoteAddress());
        //write a welcome message
        socketChannel.write(ByteBuffer.wrap("Hello!\n".getBytes("UTF-8")));
        //register channel with selector for further I/O
        keepDataTrack.put(socketChannel, new ArrayList<byte[]>());
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    //isReadable returned true
    private void readOP(SelectionKey key) {

        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            buffer.clear();
            int numRead = -1;
            try {
                numRead = socketChannel.read(buffer);
            } catch (IOException e) {
                System.err.println("Cannot read error!");
            }
            if (numRead == -1) {
                this.keepDataTrack.remove(socketChannel);
                System.out.println("Connection closed by: " + socketChannel.getRemoteAddress());
                socketChannel.close();
                key.cancel();
                return;
            }
            byte[] data = new byte[numRead];
            System.arraycopy(buffer.array(), 0, data, 0, numRead);
            System.out.println(new String(data, "UTF-8") + " from " +
                    socketChannel.getRemoteAddress());
            // write back to client
            doEchoJob(key, data);
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    //isWritable returned true
    private void writeOP(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        List<byte[]> channelData = keepDataTrack.get(socketChannel);
        Iterator<byte[]> its = channelData.iterator();
        while (its.hasNext()) {
            byte[] it = its.next();
            its.remove();
            socketChannel.write(ByteBuffer.wrap(it));
        }
        key.interestOps(SelectionKey.OP_READ);
    }

    private void doEchoJob(SelectionKey key, byte[] data) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        List<byte[]> channelData = keepDataTrack.get(socketChannel);
        channelData.add(data);
        key.interestOps(SelectionKey.OP_WRITE);
    }


    private NetworkInterface getNetworkInterface() throws SocketException {
        // Get the reference of a network interface
        NetworkInterface networkInterface = null;
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface nextNetworkInterface = networkInterfaces.nextElement();
            log.info(nextNetworkInterface + ": [supportsMulticast=" + nextNetworkInterface.supportsMulticast() + "][virtual=" + nextNetworkInterface.isVirtual() + "]");
            if (nextNetworkInterface.supportsMulticast()) {
                networkInterface = nextNetworkInterface;
//                break;
            }
        }
        return networkInterface;
    }

    public static void main(String[] args) throws IOException {
        AnagramTCPServer server = new AnagramTCPServer();
        Thread serverThread = new Thread(server);
        serverThread.start();
    }
}
