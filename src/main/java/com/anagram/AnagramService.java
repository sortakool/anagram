package com.anagram;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by rmanaloto on 8/12/14.
 */
public class AnagramService implements AnagramServiceMBean {

    private static final Locale usLocale = new Locale("en", "US");
    public static final String DEFAULT_DICTIONARY_FILENAME = "words.txt";
    public static final boolean DEFAULT_CASE_SENSITIVE = false;

    private final Map<String, Set<String>> sortedWord2Anagrams = new HashMap<>();
    private boolean caseSensitive;
    private final Comparator<String> stringComparator;
    private final int numberOfStripes;
    private final ReadWriteLock[] stripeLocks;

    public AnagramService(int numberOfStripes) {
        this(DEFAULT_CASE_SENSITIVE, numberOfStripes);
    }

    public AnagramService(boolean caseSensitive, int numberOfStripes) {
        this.caseSensitive = caseSensitive;
        this.stringComparator = this.caseSensitive ? null : String.CASE_INSENSITIVE_ORDER;
        this.numberOfStripes = numberOfStripes;
        this.stripeLocks = new ReadWriteLock[numberOfStripes];
        for(int i=0; i<numberOfStripes; i++) {
            this.stripeLocks[i] = new ReentrantReadWriteLock();
        }
    }

    public AnagramService(int numberOfStripes, String dictionaryFilename) throws FileNotFoundException {
        this(DEFAULT_CASE_SENSITIVE, numberOfStripes, dictionaryFilename);
    }

    public AnagramService(boolean caseSensitive, int numberOfStripes, String dictionaryFilename) throws FileNotFoundException {
        this(caseSensitive, numberOfStripes);
        int count = processFile(dictionaryFilename);
        System.out.println("Added " + count + " words");
    }

    public int processFile(String filename) throws FileNotFoundException {
        final FileSystem fileSystem = FileSystems.getDefault();
        final Path path = fileSystem.getPath(filename);
        final File localFile = path.toFile();
        Scanner scanner = null;
        if(localFile.exists()) {
            scanner = new Scanner(localFile, "UTF-8");
        } else {
            final InputStream localInputStream = getClass().getClassLoader().getResourceAsStream(filename);
            scanner = new Scanner(new BufferedInputStream(localInputStream), "UTF-8");
        }
        scanner.useLocale(usLocale);

        int count = 0;
        while(scanner.hasNext()) {
            String word = scanner.nextLine();
            add(word);
            count++;
        }
        return count;
    }



    @Override
    public boolean add(String word) {
        boolean added = false;
        if(word != null) {
            String sortedWord = sort(word);
            final ReadWriteLock lock = getLock(sortedWord);
            lock.writeLock().lock();
            try {
                Set<String> anagrams = sortedWord2Anagrams.get(sortedWord);
                if(anagrams == null) {
                    anagrams = caseSensitive ? new TreeSet<>() : new TreeSet<>(stringComparator);
                    sortedWord2Anagrams.put(sortedWord, anagrams);
                }
                added = anagrams.add(word);
            } finally {
               lock.writeLock().unlock();
            }
        }
        return added;
    }


    @Override
    public boolean delete(String word) {
        boolean deleted = false;
        if(word != null) {
            String sortedWord = sort(word);
            final ReadWriteLock lock = getLock(sortedWord);
            lock.writeLock().lock();
            try {
                Set<String> anagrams = sortedWord2Anagrams.get(sortedWord);
                if(anagrams != null) {
                    deleted = anagrams.remove(word);
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
        return deleted;
    }

    @Override
    public Set<String> getAnagrams(String word) {
        Set<String> anagramsCopy = null;
        Set<String> anagrams = null;
        if(word != null) {
            String sortedWord = sort(word);
            final ReadWriteLock lock = getLock(sortedWord);
            lock.readLock().lock();
            try {
                anagrams = sortedWord2Anagrams.get(sortedWord);
            } finally {
                lock.readLock().unlock();
            }
        }
        if( (anagrams == null) || anagrams.isEmpty()) {
            return Collections.emptySet();
        } else {
            anagramsCopy = new TreeSet<>(stringComparator);
            anagramsCopy.addAll(anagrams);
            anagramsCopy.remove(word);
        }
        return anagramsCopy;
    }

    private String sort(String word) {
        final char[] characters = new char[word.length()];
        for(int i=0; i<word.length(); i++) {
            characters[i] = caseSensitive ? word.charAt(i) : Character.toLowerCase(word.charAt(i));
        }
        Arrays.sort(characters);
        return new String(characters);
    }

    private ReadWriteLock getLock(String sortedWord) {
        int index = sortedWord.hashCode() % numberOfStripes;
        return stripeLocks[index];
    }
}
