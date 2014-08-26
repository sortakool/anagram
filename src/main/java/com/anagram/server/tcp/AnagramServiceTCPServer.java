package com.anagram.server.tcp;

import com.anagram.AnagramServiceFactory;
import com.anagram.AnagramServiceMBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by rmanaloto on 8/12/14.
 *
 * TODO handle issue where there are multiple different clients send requests
 * and how to handle the buffers for each (ie, make sure they are not intermixed)
 */
public class AnagramServiceTCPServer implements Runnable {

    private final Logger log = Logger.getLogger(getClass().getName());

    public static final String EXIT = "X";

    private final StringBuilder sb = new StringBuilder();

    private final AnagramTCPServerConfiguration configuration;

    private volatile boolean active;
    private Selector selector;
    private final ByteBuffer readBuffer;
    private final ByteBuffer writeBuffer;
    private ServerSocketChannel serverSocketChannel;
    private NetworkInterface networkInterface;

    private final AnagramServiceFactory anagramServiceFactory;
    private final AnagramServiceMBean anagramService;
    private SelectionKey registeredSelectionKey;

    private boolean writeImmediately = true;

    public AnagramServiceTCPServer(AnagramTCPServerConfiguration configuration) {
        this.configuration = configuration;
        Objects.nonNull(this.configuration);

        this.readBuffer = ByteBuffer.allocateDirect(this.configuration.getReadBufferSize());
        this.readBuffer.order(this.configuration.getByteOrder());
        this.writeBuffer = ByteBuffer.allocateDirect(this.configuration.getWriteBufferSize());
        this.writeBuffer.order(this.configuration.getByteOrder());

        this.anagramServiceFactory = new AnagramServiceFactory();
        this.anagramService = this.anagramServiceFactory.createAnagramService(AnagramServiceFactory.Mode.LOCAL);
    }

    public void cancel() {
        this.active = false;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isWriteImmediately() {
        return writeImmediately;
    }

    public void setWriteImmediately(boolean writeImmediately) {
        this.writeImmediately = writeImmediately;
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
//        networkInterface = getNetworkInterface();
        serverSocketChannel = ServerSocketChannel.open();

        //check that both of them were successfully opened
        if ((serverSocketChannel.isOpen()) && (selector.isOpen())) {
            //configure non-blocking mode
            serverSocketChannel.configureBlocking(false);
            //set some options
            serverSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF, configuration.getReceiveBufferSize());
            serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, configuration.isReuseAddress());
            //bind the server socket channel to port
            serverSocketChannel.bind(new InetSocketAddress(configuration.getPort()));
            //register the current channel with the given selector
            registeredSelectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            this.active = true;

            //display a waiting message while ... waiting!
            System.out.println("Waiting for connections ...");
        }
    }

    private void eventLoop() throws IOException {
        try {
            init();
            Set<SelectionKey> selectionKeys = null;
            while (!Thread.currentThread().isInterrupted() && active) {
                //wait for incoming events
                final int selectNow = selector.selectNow();
                //there is something to process on selected keys
                if ((selectNow > 0) && !(selectionKeys = selector.selectedKeys()).isEmpty()) {
                    Iterator<SelectionKey> keys = selectionKeys.iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        //prevent the same key from coming up again
                        keys.remove();
                        if (!key.isValid()) {
                            continue;
                        }
                        if(!serverSocketChannel.isOpen()) {
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
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
            }
            if (selector != null) {
                selector.close();
            }
            cancel();

            log.info("Exiting finally...");
        }
    }

    //isAcceptable returned true
    private void acceptOP(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        if (serverChannel != null) {
            SocketChannel socketChannel = serverChannel.accept();
            socketChannel.configureBlocking(false);

            socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, configuration.isClientKeepAlive());
            socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, configuration.isClientTCPNoDelay());
            socketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, configuration.isClientReuseAddress());
            socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, configuration.getClientReceiveBufferSize());
            socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, configuration.getClientSendBufferSize());

            sb.setLength(0);
            sb.append("Incoming connection from: ").append(socketChannel.getRemoteAddress());
            log.info(sb.toString());

            final SelectionKey readSelectionKey = socketChannel.register(selector, SelectionKey.OP_READ, readBuffer);
        }
    }

    //isReadable returned true
    private void readOP(SelectionKey key) {

        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            final ByteBuffer buffer = (ByteBuffer) key.attachment();
            buffer.clear();
            int numRead = -1;
            try {
                numRead = socketChannel.read(buffer);
            } catch (IOException e) {
                log.severe("Cannot read error!");
            }
            if (numRead == -1) {
                log.info("Connection closed by: " + socketChannel.getRemoteAddress());
                socketChannel.close();
                key.cancel();
                return;
            } else if (numRead > 0) {
                sendRequestToAnagramService(key, socketChannel, buffer);
                if(writeImmediately) {
                    writeOP(key);
                }
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    private void sendRequestToAnagramService(SelectionKey selectionKey, SocketChannel socketChannel, ByteBuffer byteBuffer) {
        byteBuffer.flip();
        byte eventTypeTypeCode = byteBuffer.get();
        final EventType eventType = EventType.fromTypeCode(eventTypeTypeCode);
        sb.setLength(0);

        String word = getWord(byteBuffer);
        sb.setLength(0);
        sb.append("[eventType=").append(eventType).append("][word=").append(word).append("]");
        log.info(sb.toString());

        switch (eventType) {
            case ADD:
                writeBuffer.put(EventType.ADD.getTypeCode());
                boolean added = anagramService.addWord(word);
                if (added) {
                    writeBuffer.put((byte) 0);
                } else {
                    writeBuffer.put((byte) 1);
                }
                SelectionKeyUtil.clearBit(selectionKey, SelectionKey.OP_READ);
                SelectionKeyUtil.setBit(selectionKey, SelectionKey.OP_WRITE);
                break;
            case DELETE:
                writeBuffer.put(EventType.DELETE.getTypeCode());
                boolean deleted = anagramService.deleteWord(word);
                if (deleted) {
                    writeBuffer.put((byte) 0);
                } else {
                    writeBuffer.put((byte) 1);
                }
                SelectionKeyUtil.clearBit(selectionKey, SelectionKey.OP_READ);
                SelectionKeyUtil.setBit(selectionKey, SelectionKey.OP_WRITE);
                break;
            case PRINT:
                writeBuffer.put(EventType.PRINT.getTypeCode());
                Set<String> anagrams = anagramService.getAnagrams(word);
                writeBuffer.putInt(anagrams.size());
                for (String anagram : anagrams) {
                    writeBuffer.putInt(anagram.getBytes().length);
                    writeBuffer.put(anagram.getBytes());
                }
                SelectionKeyUtil.clearBit(selectionKey, SelectionKey.OP_READ);
                SelectionKeyUtil.setBit(selectionKey, SelectionKey.OP_WRITE);
                break;
            default:
                log.severe("Invalid eventType: " + eventTypeTypeCode);
                break;
        }
    }

    private String getWord(ByteBuffer byteBuffer) {
        String word = null;
        int wordLength = byteBuffer.getInt();
        for (int i = 0; i < wordLength; i++) {
            sb.append((char) byteBuffer.get());
        }
        word = sb.toString();
        return word;
    }

    //isWritable returned true
    private void writeOP(SelectionKey key) throws IOException {
        writeBuffer.flip();
        SocketChannel socketChannel = (SocketChannel) key.channel();
        int writeCount  = 0;
        while (writeBuffer.hasRemaining()) {
            writeCount += socketChannel.write(writeBuffer);
        }
        sb.setLength(0);
        sb.append("Wrote ").append(writeCount).append(" bytes");
        log.info(sb.toString());
        SelectionKeyUtil.clearBit(key, SelectionKey.OP_WRITE);
        SelectionKeyUtil.setBit(key, SelectionKey.OP_READ);
//        final boolean writable = key.isWritable();
//        log.info("writable="+writable);
        writeBuffer.clear();
    }

    private NetworkInterface getNetworkInterface() throws SocketException {
        // Get the reference of a network interface
        NetworkInterface networkInterface = null;
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        int maxMTU = Integer.MIN_VALUE;
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface nextNetworkInterface = networkInterfaces.nextElement();
//            log.info(nextNetworkInterface + ": [supportsMulticast=" + nextNetworkInterface.supportsMulticast() + "][virtual=" + nextNetworkInterface.isVirtual() + "]");
            if (!nextNetworkInterface.isVirtual() &&
                    !nextNetworkInterface.isLoopback() &&
                    nextNetworkInterface.isUp()
                    ) {
                if (nextNetworkInterface.getMTU() > maxMTU) {
                    networkInterface = nextNetworkInterface;
                }
            }
        }
        return networkInterface;
    }

//    private void readConsoleInput() {
//        Scanner scanner = new Scanner(System.in);
//        final StringBuilder sb = new StringBuilder(1024);
//        while (true) {
//            sb.setLength(0);
//            sb.append("Enter any command below:\n");
//            sb.append("[X]\tExit");
//            System.out.println(sb);
//            sb.setLength(0);
//            String command = scanner.nextLine();
//            switch (command) {
//                case EXIT:
//                    sb.append("Exiting...");
//                    System.out.println(sb);
//                    return;
//                default:
//                    sb.append("Invalid command '").append(command).append("'");
//                    System.out.println(sb);
//                    break;
//            }
//        }
//    }

    private void readConsoleInput() throws IOException {
        final StringBuilder sb = new StringBuilder(1024);
        try (BufferedReader is = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                sb.setLength(0);
                sb.append("Enter any command below:\n");
                sb.append("[X]\tExit");
                System.out.println(sb);
                sb.setLength(0);
                String command = is.readLine();
                switch (command) {
                    case EXIT:
                        sb.append("Exiting...");
                        System.out.println(sb);
                        return;
                    default:
                        sb.append("Invalid command '").append(command).append("'");
                        System.out.println(sb);
                        break;
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final AnagramTCPServerConfiguration configuration = new AnagramTCPServerConfiguration();
        AnagramServiceTCPServer server = new AnagramServiceTCPServer(configuration);
        Thread serverThread = new Thread(server);
        serverThread.start();
        server.readConsoleInput();
        server.cancel();
        serverThread.join();
        System.out.println("Done...");
    }
}
