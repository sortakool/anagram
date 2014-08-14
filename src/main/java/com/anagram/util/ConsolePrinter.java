package com.anagram.util;

import java.io.PrintStream;

/**
 * Created by rmanaloto on 8/13/14.
 */
public abstract class ConsolePrinter {

    public static void print(PrintStream printStream, StringBuilder sb) {
        for (int i = 0; i < sb.length(); i++) {
            printStream.print(sb.charAt(i));
        }
    }

    public static void println(PrintStream printStream, StringBuilder sb) {
        print(printStream, sb);
        printStream.println();
    }
}
