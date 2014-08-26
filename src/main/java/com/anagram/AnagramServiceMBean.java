package com.anagram;

import java.util.Set;

/**
 * Created by rmanaloto on 8/12/14.
 *
 * Service that will store a dictionary of words and will find anagrams for a specified word.
 */
public interface AnagramServiceMBean {

    /**
     * Add a word into the service's dictionary.
     *
     * @param word word to add
     * @return True if the word was successfully added. False if the word already exists.
     */
    boolean addWord(String word);

    /**
     * Deletes a word from the service's dictionary.
     *
     * @param word the word to delete.
     * @return True if the word was successfully added. False if the word is not in the dictionary.
     */
    boolean deleteWord(String word);

    /**
     * Returns a {@link java.util.Set} of all anagrams in the service's dictionary for the specified word.
     *
     * @param word The word to find anagrams for.
     * @return a {@link java.util.Set} of anagrams.
     *         An empty {@link java.util.Set} is returned if there are not any anagrams for the word.
     */
    Set<String> getAnagrams(String word);

    void close();
}
