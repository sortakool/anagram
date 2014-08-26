package com.anagram.server.tcp;

import com.anagram.AnagramServiceMBean;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by rmanaloto on 8/17/14.
 */
public class AnagramServiceTCPClient implements AnagramServiceMBean, Runnable {

    public static final int MAX_WAIT_ATTEMPTS = 1;

    private static class Command {
        private final EventType eventType;
        private final String word;

        private Command(EventType eventType, String word) {
            this.eventType = eventType;
            this.word = word;
        }

        public EventType getEventType() {
            return eventType;
        }

        public String getWord() {
            return word;
        }
    }

    private final Logger log = Logger.getLogger(getClass().getName());

    private final AnagramServiceTCPClientConfiguration configuration;


    private SocketChannel socketChannel;
    private InetSocketAddress socketAddress;
    private SelectionKey registeredSelectionKey;

    private volatile boolean active;
    private volatile boolean connected;
    private Selector selector;
    private final ByteBuffer readBuffer;
    private final ByteBuffer writeBuffer;

//    private final BlockingQueue<Command> commands = new LinkedBlockingDeque<>();
//    private final BlockingQueue<Boolean> addEvents = new LinkedBlockingDeque<>();
//    private final BlockingQueue<Boolean> deleteEvents = new LinkedBlockingDeque<>();
//    private final BlockingQueue<Set<String>> printEvents = new LinkedBlockingDeque<>();

    private final Object guardedObject = new Object();
    private Command command;
    private Boolean added;
    private Boolean deleted;
    private Set<String> anagrams;

    private final StringBuilder sb = new StringBuilder();

    private final Thread clientThread;

    public AnagramServiceTCPClient(AnagramServiceTCPClientConfiguration configuration) {
        this.configuration = configuration;
        Objects.nonNull(this.configuration);

        this.readBuffer = ByteBuffer.allocateDirect(this.configuration.getReadBufferSize());
        this.readBuffer.order(this.configuration.getByteOrder());
        this.writeBuffer = ByteBuffer.allocateDirect(this.configuration.getWriteBufferSize());
        this.writeBuffer.order(this.configuration.getByteOrder());

        clientThread = new Thread(this);
        clientThread.start();
    }

    public void cancel() {
        this.active = false;
        this.connected = false;

        try {
            clientThread.join();
            log.info("clientThread join completed");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isActive() {
        return active;
    }

    public boolean isConnected() {
        return connected;
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
        socketChannel = SocketChannel.open();
        socketAddress = new InetSocketAddress(configuration.getHost(), configuration.getPort());

        //check that both of them were successfully opened
        if ((socketChannel.isOpen()) && (selector.isOpen())) {
            //configure non-blocking mode
            socketChannel.configureBlocking(false);
            //set some options
            socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, configuration.getReceiveBufferSize());
            socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, configuration.getSendBufferSize());
            socketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, configuration.isKeepAlive());
            socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, configuration.isReuseAddress());
            socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, configuration.isTcpNoDelay());

            registeredSelectionKey = socketChannel.register(selector, SelectionKey.OP_CONNECT, readBuffer);
//            selectionKey.attach(readBuffer);

            final boolean connect = socketChannel.connect(socketAddress);

            this.active = true;

            //display a waiting message while ... waiting!
            System.out.println("active ...");
        }
    }

    private void eventLoop() throws IOException {
        try {
            init();
            Set<SelectionKey> selectionKeys = null;
            while (!Thread.currentThread().isInterrupted() && active) {
                Command theCommand = getCommand();
                processCommand(theCommand);
                //wait for incoming events
                final int selectNow = selector.selectNow();
                //there is something to process on selected keys
                if ((selectNow > 0) && !(selectionKeys = selector.selectedKeys()).isEmpty()) {
                    Iterator<SelectionKey> keys = selectionKeys.iterator();
                    while (keys.hasNext()) {
                        SelectionKey key = (SelectionKey) keys.next();
                        //prevent the same key from coming up again
                        keys.remove();
                        if (!key.isValid()) {
                            continue;
                        }
                        if (key.isConnectable()) {
                            connectOP(key, selector);
                        } else if (key.isAcceptable()) {
                            acceptOP(key, selector);
                        } else if (key.isReadable()) {
                            this.readOP(key, selector);
                        } else if (key.isWritable()) {
                            this.writeOP(key, selector);
                        }
                    }
                }
            }
        } finally {
            if (socketChannel != null) {
                socketChannel.close();
            }
            if (selector != null) {
                selector.close();
            }
//            cancel();
            this.active = false;
            this.connected = false;
            log.info("Exiting finally...");
        }

    }

    private Command getCommand() {
        Command command = null;
        synchronized (guardedObject) {
            command = this.command;
            if(command != null) {
                this.command = null;
            }
        }
        return command;
    }

    private void processCommand(Command command) {
        if(command != null) {
            writeBuffer.clear();
            writeBuffer.put(command.getEventType().getTypeCode());
            writeBuffer.putInt(command.getWord().getBytes().length);
            writeBuffer.put(command.getWord().getBytes());
            SelectionKeyUtil.setBit(registeredSelectionKey, SelectionKey.OP_WRITE);
        }
    }

    //isConnectable returned true
    private void connectOP(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel != null) {
            //signal connection success
            log.info("I am connected");

            if(channel.isConnectionPending()) {
                final boolean finishConnect = channel.finishConnect();
                log.info("finishConnect: " + finishConnect);
            }

            key.interestOps(0);

            this.connected = true;
            log.info("Waiting for commands...");
        }
    }

    //isAcceptable returned true
    private void acceptOP(SelectionKey key, Selector selector) throws IOException {
        log.severe("Accept is Unsupported");
    }

    //isWritable returned true
    private void writeOP(SelectionKey key, Selector selector) throws IOException {
        final SocketChannel socketChannel = (SocketChannel)key.channel();
        writeBuffer.flip();
        int writeCount = 0;
        while(writeBuffer.hasRemaining()) {
            writeCount += socketChannel.write(writeBuffer);
        }
        sb.setLength(0);
        sb.append("Wrote ").append(writeCount).append(" bytes");
        log.info(sb.toString());
        SelectionKeyUtil.clearBit(key, SelectionKey.OP_WRITE);
        SelectionKeyUtil.setBit(key, SelectionKey.OP_READ);
    }

    //isReadable returned true
    private void readOP(SelectionKey key, Selector selector) throws IOException {
        try {
            final SocketChannel socketChannel1 = (SocketChannel) key.channel();
            final ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
            byteBuffer.clear();
            int numRead = -1;
            try {
                numRead = socketChannel1.read(byteBuffer);
            } catch (IOException e) {
                log.severe("Read Error: " + e.getMessage());
            }
            if(numRead == -1) {
                log.info("Connection closed by: " + socketChannel1.getRemoteAddress());
                socketChannel1.close();
                key.cancel();
                return;
            } else if(numRead > 0){
                processResponse(key, socketChannel1, byteBuffer);
            }
        } catch(IOException e) {
            System.err.println(e);
        }
    }

    private void processResponse(SelectionKey selectionKey, SocketChannel socketChannel, ByteBuffer byteBuffer) {
        byteBuffer.flip();
        final byte eventTypeTypeCode = byteBuffer.get();
        final EventType eventType = EventType.fromTypeCode(eventTypeTypeCode);
        sb.setLength(0);

        switch (eventType) {
            case ADD:
                final byte addedByteValue = byteBuffer.get();
                boolean addedValue = (addedByteValue == 0) ? false : true;
                synchronized (guardedObject) {
                    while(added != null) {
                        try {
                            guardedObject.wait();
                        } catch (InterruptedException e) {
                            //restore interrupted status
                            Thread.currentThread().interrupt();
                        }
                    }
                    added = addedValue;
                    guardedObject.notify();
                }
                break;
            case DELETE:
                final byte deletedByteValue = byteBuffer.get();
                boolean deletedValue = (deletedByteValue == 0) ? false : true;
                synchronized (guardedObject) {
                    while(deleted != null) {
                        try {
                            guardedObject.wait();
                        } catch (InterruptedException e) {
                            //restore interrupted status
                            Thread.currentThread().interrupt();
                        }
                    }
                    deleted = deletedValue;
                    guardedObject.notify();
                }
                break;
            case PRINT:
                Set<String> tempAnagrams = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                final int count = byteBuffer.getInt();
                for(int i=0; i<count; i++) {
                    String word = getWord(byteBuffer);
                    tempAnagrams.add(word);
                }
                synchronized (guardedObject) {
                    while(anagrams != null) {
                        try {
                            guardedObject.wait();
                        } catch (InterruptedException e) {
                            //restore interrupted status
                            Thread.currentThread().interrupt();
                        }
                    }
                    anagrams = tempAnagrams;
                    guardedObject.notify();
                }
                break;
            default:
                break;
        }
    }

    private String getWord(ByteBuffer byteBuffer) {
        String word = null;
        sb.setLength(0);
        int wordLength = byteBuffer.getInt();
        for (int i = 0; i < wordLength; i++) {
            sb.append((char) byteBuffer.get());
        }
        word = sb.toString();
        return word;
    }


    @Override
    public boolean addWord(String word) {
        boolean wordAdded = false;
        //set commandWord
        synchronized (guardedObject) {
            while(command != null) {
                try {
                    guardedObject.wait();
                } catch (InterruptedException e) {
                    //restore interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            command = new Command(EventType.ADD, word);
            guardedObject.notify();
        }

        //check added Event value
        synchronized (guardedObject) {
            int attempts = 0;
            while((added == null) && (attempts < MAX_WAIT_ATTEMPTS)) {
                try {
                    guardedObject.wait(configuration.getWaitTimeoutMilliseconds());
                    attempts++;
                } catch (InterruptedException e) {
                    //restore interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            if(this.added == null) {
                throw new RuntimeException("Wait timeout " + configuration.getWaitTimeoutMilliseconds() + " milliseconds exceeded");
            }
            wordAdded = added;
            added = null;
            guardedObject.notify();
        }
        return wordAdded;
    }

    @Override
    public boolean deleteWord(String word) {
        boolean wordDeleted = false;
        //set commandWord
        synchronized (guardedObject) {
            while(command != null) {
                try {
                    guardedObject.wait();
                } catch (InterruptedException e) {
                    //restore interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            command = new Command(EventType.DELETE, word);
            guardedObject.notify();
        }

        //check added Event value
        synchronized (guardedObject) {
            int attempts = 0;
            while((deleted == null) && (attempts < MAX_WAIT_ATTEMPTS)) {
                try {
                    guardedObject.wait(configuration.getWaitTimeoutMilliseconds());
                    attempts++;
                } catch (InterruptedException e) {
                    //restore interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            if(this.deleted == null) {
                throw new RuntimeException("Wait timeout " + configuration.getWaitTimeoutMilliseconds() + " milliseconds exceeded");
            }
            wordDeleted = deleted;
            deleted = null;
            guardedObject.notify();
        }
        return wordDeleted;
    }

    @Override
    public Set<String> getAnagrams(String word) {
        Set<String> responseAnagrams = null;
        //set commandWord
        synchronized (guardedObject) {
            while(command != null) {
                try {
                    guardedObject.wait();
                } catch (InterruptedException e) {
                    //restore interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            command = new Command(EventType.PRINT, word);
            guardedObject.notify();
        }

        //check added Event value
        synchronized (guardedObject) {
            int attempts = 0;
            while((this.anagrams == null) && (attempts < MAX_WAIT_ATTEMPTS)) {
                try {
                    guardedObject.wait();
                } catch (InterruptedException e) {
                    //restore interrupted status
                    Thread.currentThread().interrupt();
                }
            }
            if(this.anagrams == null) {
                throw new RuntimeException("Wait timeout " + configuration.getWaitTimeoutMilliseconds() + " milliseconds exceeded");
            }
            responseAnagrams = this.anagrams;
            this.anagrams = null;
            guardedObject.notify();
        }
        return responseAnagrams;
    }

    @Override
    public void close() {
        cancel();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final AnagramServiceTCPClientConfiguration configuration = new AnagramServiceTCPClientConfiguration();
        AnagramServiceTCPClient client = new AnagramServiceTCPClient(configuration);
//        Thread clientThread = new Thread(client);
//        clientThread.start();

        while(!client.isActive() && !client.isConnected()) {
            TimeUnit.MILLISECONDS.sleep(100);
        }

        String word = "test";
        final Set<String> anagrams = client.getAnagrams(word);
        for (String anagram : anagrams) {
            System.out.println(anagram);
        }

        client.cancel();
//        clientThread.join();
        System.out.println("Done...");
    }
}
