package com.anagram.server.tcp;

import java.nio.channels.SelectionKey;

/**
 * Created by rmanaloto on 8/18/14.
 */
public abstract class SelectionKeyUtil {

    public static void clearBit(SelectionKey key, int value) {
        key.interestOps(key.interestOps() & ~value);
    }

    public static void setBit(SelectionKey key, int value) {
        key.interestOps(key.interestOps() | value);
    }
}
