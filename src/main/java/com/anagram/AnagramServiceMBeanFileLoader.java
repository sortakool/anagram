package com.anagram;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;

/**
 * Created by rmanaloto on 8/13/14.
 */
public abstract class AnagramServiceMBeanFileLoader {

    public static final Locale DEFAULT_LOCALE = new Locale("en", "US");
    public static final String DEFAULT_CHARSET = StandardCharsets.UTF_8.name();

    /**
     * Processes a dictionary file with specified filename and adds all words into the {@link com.anagram.AnagramServiceMBean}.
     * Assumes file specifies a word for each line and will trim it to be safe.
     *
     * Uses DEFAULT_LOCALE and DEFAULT_CHARSET
     *
     * @param anagramService The {@link com.anagram.AnagramServiceMBean} to add words to from dictionary file
     * @param filename The file name to process
     * @return number of words processed and added.
     * @throws FileNotFoundException If file is not found in the filesystem or classpath.
     */
    public static int processFile(AnagramServiceMBean anagramService, String filename) throws FileNotFoundException {
        return processFile(anagramService, filename, DEFAULT_LOCALE, DEFAULT_CHARSET);
    }

    /**
     * Processes a dictionary file with specified filename and adds all words into the {@link com.anagram.AnagramServiceMBean}.
     * Assumes file specifies a word for each line and will trim it to be safe.
     *
     * @param anagramService The {@link com.anagram.AnagramServiceMBean} to add words to from dictionary file
     * @param filename The file name to process
     * @param locale {@link java.util.Locale} to use for {@link java.util.Scanner} used to process the dictionary file.
     * @param charset the {@link java.nio.charset.Charset} for the dictionary file.
     * @return number of words processed and added.
     * @throws FileNotFoundException If file is not found in the filesystem or classpath.
     */
    public static int processFile(AnagramServiceMBean anagramService, String filename, Locale locale, String charset) throws FileNotFoundException {
        Objects.nonNull(anagramService);
        Objects.nonNull(filename);
        final FileSystem fileSystem = FileSystems.getDefault();
        final Path path = fileSystem.getPath(filename);
        final File localFile = path.toFile();
        Scanner scanner = null;
        if (localFile.exists()) {
            scanner = new Scanner(localFile, charset);
        } else {
            final InputStream localInputStream = anagramService.getClass().getClassLoader().getResourceAsStream(filename);
            scanner = new Scanner(new BufferedInputStream(localInputStream), charset);
        }
        scanner.useLocale(locale);

        int count = 0;
        String trimmedWord = null;
        while (scanner.hasNext()) {
            String word = scanner.nextLine();
            if ( (word != null) && ((trimmedWord = word.trim()).length() > 0)) {
                anagramService.addWord(trimmedWord);
                count++;
            }
        }
        scanner.close();
        System.out.println("Added " + count + " words");
        return count;
    }
}
