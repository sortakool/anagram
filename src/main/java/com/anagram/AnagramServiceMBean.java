package com.anagram;

import java.util.Set;

/**
 * Created by rmanaloto on 8/12/14.
 */
public interface AnagramServiceMBean {

    boolean add(String word);

    boolean delete(String word);

    Set<String> getAnagrams(String word);
}
