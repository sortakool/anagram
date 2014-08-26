package com.anagram;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by rmanaloto on 8/12/14.
 *
 * Implementation of {@link com.anagram.AnagramServiceMBean} which supports thread-safe access.
 * Internal data structures are striped to provide supporting multi-threaded access from different clients.
 */
public class AnagramService implements AnagramServiceMBean {

    private final Map<String, Set<String>> sortedWord2Anagrams = new HashMap<>();
    private final int numberOfStripes;
    private final ReadWriteLock[] stripeLocks;

    /**
     * Creates a new, empty {@link com.anagram.AnagramServiceMBean} instance that supports multiple updating threads via
     * the numberOfStripes parameter.
     *
     * @param numberOfStripes number of stripes which correlates to the estimated number of concurrently updating threads.
     */
    public AnagramService(int numberOfStripes) {
        this.numberOfStripes = numberOfStripes;
        if (this.numberOfStripes <= 0) {
            throw new IllegalArgumentException("Invalid number of stripes " + numberOfStripes);
        }
        this.stripeLocks = new ReadWriteLock[numberOfStripes];
        for (int i = 0; i < numberOfStripes; i++) {
            this.stripeLocks[i] = new ReentrantReadWriteLock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addWord(String word) {
        boolean added = false;
        if (word != null) {
            String sortedWord = sort(word);
            final ReadWriteLock lock = getLock(sortedWord);
            lock.writeLock().lock();
            try {
                Set<String> anagrams = sortedWord2Anagrams.get(sortedWord);
                if (anagrams == null) {
                    anagrams = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                    sortedWord2Anagrams.put(sortedWord, anagrams);
                }
                added = anagrams.add(word);
            } finally {
                lock.writeLock().unlock();
            }
        }
        return added;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteWord(String word) {
        boolean deleted = false;
        if (word != null) {
            String sortedWord = sort(word);
            final ReadWriteLock lock = getLock(sortedWord);
            lock.writeLock().lock();
            try {
                Set<String> anagrams = sortedWord2Anagrams.get(sortedWord);
                if (anagrams != null) {
                    deleted = anagrams.remove(word);
                    if (anagrams.isEmpty()) {
                        sortedWord2Anagrams.remove(sortedWord);
                    }
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
        return deleted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getAnagrams(String word) {
        Set<String> anagramsCopy = null;
        Set<String> anagrams = null;
        if (word != null) {
            String sortedWord = sort(word);
            final ReadWriteLock lock = getLock(sortedWord);
            lock.readLock().lock();
            try {
                anagrams = sortedWord2Anagrams.get(sortedWord);
            } finally {
                lock.readLock().unlock();
            }
        }
        if ((anagrams == null) || (anagrams.size() <= 1)) {
            return Collections.emptySet();
        } else {
            anagramsCopy = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            anagramsCopy.addAll(anagrams);
            anagramsCopy.remove(word);
        }
        return anagramsCopy;
    }

    @Override
    public void close() {

    }

    /**
     * Sorts the word to its uppercase form.
     *
     * @param word word to sort
     * @return word uppercase sorted.
     */
    private String sort(String word) {
        final char[] characters = word.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            characters[i] = Character.toUpperCase(characters[i]);
        }
        Arrays.sort(characters);
        return new String(characters);
    }

    /**
     * Gets a lock for the sorted word.
     * Internally, it uses the same logic found in {@link java.util.concurrent.ConcurrentHashMap}
     * to stripe access into the internal data structures.
     *
     * @see java.util.concurrent.ConcurrentHashMap
     *
     * @param sortedWord the sorted word to find a lock for
     * @return {@link java.util.concurrent.locks.ReadWriteLock} for the sortedWord.
     */
    private ReadWriteLock getLock(String sortedWord) {
        int index = spread(sortedWord.hashCode()) % numberOfStripes;
        return stripeLocks[index];
    }

    /**
     * @see java.util.concurrent.ConcurrentHashMap#HASH_BITS
     */
    private static final int HASH_BITS = 0x7fffffff; // usable bits of normal node hash

    /**
     * @see java.util.concurrent.ConcurrentHashMap#spread(int)
     *
     * Spreads (XORs) higher bits of hash to lower and also forces top
     * bit to 0. Because the table uses power-of-two masking, sets of
     * hashes that vary only in bits above the current mask will
     * always collide. (Among known examples are sets of Float keys
     * holding consecutive whole numbers in small tables.)  So we
     * apply a transform that spreads the impact of higher bits
     * downward. There is a tradeoff between speed, utility, and
     * quality of bit-spreading. Because many common sets of hashes
     * are already reasonably distributed (so don't benefit from
     * spreading), and because we use trees to handle large sets of
     * collisions in bins, we just XOR some shifted bits in the
     * cheapest possible way to reduce systematic lossage, as well as
     * to incorporate impact of the highest bits that would otherwise
     * never be used in index calculations because of table bounds.
     */
    private static final int spread(int h) {
        return (h ^ (h >>> 16)) & HASH_BITS;
    }
}
