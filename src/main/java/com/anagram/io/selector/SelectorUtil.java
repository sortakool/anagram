package com.anagram.io.selector;


import java.lang.reflect.Field;
import java.nio.channels.Selector;
import java.util.logging.Logger;

/**
 * @see io.netty.channel.nio.NioEventLoop#openSelector()
 *
 * Created by rmanaloto on 4/3/14.
 */
public abstract class SelectorUtil {

//    private static final Logger log = Logger.getLogger(SelectorUtil.class.getName());
//
//    public static void optimizeSelector(Selector selector, SelectedSelectionKeySet selectedKeys) {
//        try {
//            Class<?> selectorImplClass =
//                    Class.forName("sun.nio.ch.SelectorImpl", false, ClassLoader.getSystemClassLoader());
//
//            // Ensure the current selector implementation is what we can instrument.
//            if (!selectorImplClass.isAssignableFrom(selector.getClass())) {
////                return selector;
//            }
//
//            Field selectedKeysField = selectorImplClass.getDeclaredField("selectedKeys");
//            Field publicSelectedKeysField = selectorImplClass.getDeclaredField("publicSelectedKeys");
//
//            selectedKeysField.setAccessible(true);
//            publicSelectedKeysField.setAccessible(true);
//
//            selectedKeysField.set(selector, selectedKeys);
//            publicSelectedKeysField.set(selector, selectedKeys);
//
////            selectedKeys = selectedKeySet;
//            log.info("Instrumented an optimized java.util.Set into: " + selector);
//        } catch (Throwable t) {
////            selectedKeys = null;
//            log.severe("Failed to instrument an optimized java.util.Set into: " + selector, t);
//        }
//    }
}
