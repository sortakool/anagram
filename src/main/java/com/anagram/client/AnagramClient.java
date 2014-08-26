package com.anagram.client;

import com.anagram.AnagramServiceFactory;
import com.anagram.AnagramServiceMBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.Set;

/**
 * Created by rmanaloto on 8/13/14.
 *
 * Console application for a {@link com.anagram.AnagramServiceMBean} which can run in several modes:
 * <ol>
 *     <li>{@link com.anagram.AnagramServiceFactory.Mode#LOCAL}</li>
 *     <li>{@link com.anagram.AnagramServiceFactory.Mode#JMX}</li>
 * </ol>
 *
 */
public class AnagramClient {

    private final AnagramServiceFactory.Mode mode;
    private final AnagramServiceMBean anagramService;
    private final AnagramServiceFactory anagramServiceFactory;

    public AnagramClient(AnagramServiceFactory.Mode mode) {
        this.mode = mode;
        this.anagramServiceFactory = new AnagramServiceFactory();
        this.anagramService = this.anagramServiceFactory.createAnagramService(this.mode);
    }

    public void close() {
        this.anagramService.close();
    }

//    private void readConsoleInput() {
//        Scanner scanner = new Scanner(System.in);
//        final StringBuilder sb = new StringBuilder(1024);
//        String word;
//        Set<String> anagrams;
//        while(true) {
//            sb.setLength(0);
//            sb.append("Enter any command below:\n");
//            sb.append("[A]\tAdd a word\n");
//            sb.append("[D]\tDelete a word\n");
//            sb.append("[P]\tPrint anagrams\n");
//            sb.append("[X]\tExit");
//            System.out.println(sb);
//            sb.setLength(0);
//            String command = scanner.nextLine();
//            switch(command) {
//                case "A":
//                    sb.append("Enter word: ");
//                    System.out.println(sb);
//                    word = scanner.nextLine();
//                    boolean added = anagramService.addWord(word);
//                    sb.setLength(0);
//                    if(added) {
//                        sb.append("Successfully added word '").append(word).append("'");
//                    } else {
//                        sb.append("'").append(word).append("' already exists.");
//                    }
//                    System.out.println(sb);
//                    break;
//                case "D":
//                    sb.append("Enter word: ");
//                    System.out.println(sb);
//                    word = scanner.nextLine();
//                    boolean deleted = anagramService.deleteWord(word);
//                    sb.setLength(0);
//                    if(deleted) {
//                        sb.append("Successfully deleted word '").append(word).append("'");
//                    } else {
//                        sb.append("'").append(word).append("' does not exist in dictionary.");
//                    }
//                    System.out.println(sb);
//                    break;
//                case "P":
//                    sb.append("Enter word: ");
//                    System.out.println(sb);
//                    word = scanner.nextLine();
//                    anagrams = anagramService.getAnagrams(word);
//                    sb.setLength(0);
//                    if(anagrams.isEmpty()) {
//                        sb.append("No anagrams for word '").append(word).append("'");
//                    } else {
//                        sb.append(anagrams.size()).append(" anagrams for word '").append(word).append("': ");
//                        int counter = 0;
//                        for (String anagram : anagrams) {
//                            if(counter++ > 0) {
//                                sb.append(", ");
//                            }
//                            sb.append(anagram);
//                        }
//                    }
//                    System.out.println(sb);
//                    break;
//                case "X":
//                    close();
//                    sb.append("Exiting...");
//                    System.out.println(sb);
//                    return;
//                default:
//                    sb.append("Invalid command '").append(command).append("'");
//                    System.out.println(sb);
//                    break;
//            }
//        }
//    }

    private void readConsoleInput() throws IOException {
        final StringBuilder sb = new StringBuilder(1024);
        String word;
        Set<String> anagrams;
        try (BufferedReader is = new BufferedReader(new InputStreamReader(System.in))) {
            while(true) {
                sb.setLength(0);
                sb.append("Enter any command below:\n");
                sb.append("[A]\tAdd a word\n");
                sb.append("[D]\tDelete a word\n");
                sb.append("[P]\tPrint anagrams\n");
                sb.append("[X]\tExit");
                System.out.println(sb);
                sb.setLength(0);
                String command = is.readLine();
                switch(command) {
                    case "A":
                        sb.append("Enter word: ");
                        System.out.println(sb);
                        word = is.readLine();
                        boolean added = anagramService.addWord(word);
                        sb.setLength(0);
                        if(added) {
                            sb.append("Successfully added word '").append(word).append("'");
                        } else {
                            sb.append("'").append(word).append("' already exists.");
                        }
                        System.out.println(sb);
                        break;
                    case "D":
                        sb.append("Enter word: ");
                        System.out.println(sb);
                        word = is.readLine();
                        boolean deleted = anagramService.deleteWord(word);
                        sb.setLength(0);
                        if(deleted) {
                            sb.append("Successfully deleted word '").append(word).append("'");
                        } else {
                            sb.append("'").append(word).append("' does not exist in dictionary.");
                        }
                        System.out.println(sb);
                        break;
                    case "P":
                        sb.append("Enter word: ");
                        System.out.println(sb);
                        word = is.readLine();
                        anagrams = anagramService.getAnagrams(word);
                        sb.setLength(0);
                        if(anagrams.isEmpty()) {
                            sb.append("No anagrams for word '").append(word).append("'");
                        } else {
                            sb.append(anagrams.size()).append(" anagrams for word '").append(word).append("': ");
                            int counter = 0;
                            for (String anagram : anagrams) {
                                if(counter++ > 0) {
                                    sb.append(", ");
                                }
                                sb.append(anagram);
                            }
                        }
                        System.out.println(sb);
                        break;
                    case "X":
                        close();
                        sb.append("Exiting...");
                        System.out.println(sb);
                        return;
                    default:
                        sb.append("Invalid command '").append(command).append("'");
                        System.out.println(sb);
                        break;
                }
            }
        }


    }

    public static void main(String[] args) throws Exception {
        String modeString = System.getProperty("anagram.service.factory.mode", AnagramServiceFactory.Mode.LOCAL.name());
        AnagramServiceFactory.Mode mode = AnagramServiceFactory.Mode.valueOf(modeString.toUpperCase());
        if(mode == null) {
            throw new IllegalArgumentException("Invalid mode '" + modeString + "'");
        }
        final AnagramClient client = new AnagramClient(mode);
        client.readConsoleInput();
    }
}
