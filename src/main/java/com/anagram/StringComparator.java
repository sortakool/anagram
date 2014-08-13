package com.anagram;

import java.util.Comparator;

/**
 * Created by rmanaloto on 8/12/14.
 */
public class StringComparator implements Comparator<String> {

    private volatile boolean caseSensitive;

    public StringComparator(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    @Override
    public int compare(String o1, String o2) {
        return caseSensitive ? o1.compareTo(o2) : o1.compareToIgnoreCase(o2);
    }
}
