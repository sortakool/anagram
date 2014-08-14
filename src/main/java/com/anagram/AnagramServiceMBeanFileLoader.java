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

    private static final Locale usLocale = new Locale("en", "US");

    public static int processFile(AnagramServiceMBean anagramService, String filename) throws FileNotFoundException {
        Objects.nonNull(anagramService);
        Objects.nonNull(filename);
        final FileSystem fileSystem = FileSystems.getDefault();
        final Path path = fileSystem.getPath(filename);
        final File localFile = path.toFile();
        Scanner scanner = null;
        if(localFile.exists()) {
            scanner = new Scanner(localFile, StandardCharsets.UTF_8.name());
        } else {
            final InputStream localInputStream = anagramService.getClass().getClassLoader().getResourceAsStream(filename);
            scanner = new Scanner(new BufferedInputStream(localInputStream), StandardCharsets.UTF_8.name());
        }
        scanner.useLocale(usLocale);

        int count = 0;
        while(scanner.hasNext()) {
            String word = scanner.nextLine();
            anagramService.add(word);
            count++;
        }
        System.out.println("Added " + count + " words");
        return count;
    }
}
