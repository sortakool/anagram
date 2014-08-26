package com.anagram;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class AnagramServiceTest {

    public static class AnagramServiceTestInputs {
        private int numberOfStripes;
        private Set<String> words;

        public AnagramServiceTestInputs(int numberOfStripes, Set<String> words) {
            this.numberOfStripes = numberOfStripes;
            this.words = words;
        }

        public int getNumberOfStripes() {
            return numberOfStripes;
        }

        public void setNumberOfStripes(int numberOfStripes) {
            this.numberOfStripes = numberOfStripes;
        }

        public Set<String> getWords() {
            return words;
        }

        public void setWords(Set<String> words) {
            this.words = words;
        }

        @Override
        public String toString() {
            return "AnagramServiceTestInputs{" +
                    "numberOfStripes=" + numberOfStripes +
                    ", words=" + words +
                    '}';
        }
    }

    private AnagramServiceTestInputs inputs;

    private static Collection<Set<String>> anagrams() {
        Collection<Set<String>> anagramsList = new ArrayList<>();
        anagramsList.add(Collections.emptySet()); //empty dictionary
        final String[] words = {"t", "te", "test", "test", "zzz", "0", "10", "test0", "test10"};
        for (String word : words) {
            final Set<String> anagrams = generateAnagrams(word);
            anagramsList.add(anagrams);
        }
        return anagramsList;
    }

    private static Set<String> generateAnagrams(String word) {
        Set<String> anagrams = new HashSet<>();
        for(int i=0; i<word.length(); i++) {
            char[] temp = new char[word.length()];
            word.getChars(0, word.length(), temp, 0);
            for(int j=0; j<word.length(); j++) {
                if(i != j) {
                    swap(temp, i, j);
                    String anagram = new String(temp);
                    anagrams.add(anagram);
                }
            }
        }
        return anagrams;
    }

    private static void swap(char[] values, int i, int j) {
        char temp = values[i];
        values[i] = values[j];
        values[j] =  temp;
    }

    @Parameterized.Parameters(name = "stripes = {0}, word = {1}")
    public static Collection<Object[]> data() {
//        final int[] numberOfStripesValues = {1, 2, 5, 10, 100};
        final int[] numberOfStripesValues = {1};
        List<Object[]> params = new ArrayList<>();
        for (Collection<String> anagrams : anagrams()) {
            for (int numberOfStripes : numberOfStripesValues) {
                params.add(new Object[]{numberOfStripes, anagrams});
            }
        }
        return params;
    }

    public AnagramServiceTest(int numberOfStripes, Set<String> words) {
        this.inputs = new AnagramServiceTestInputs(numberOfStripes, words);
    }

    @Test
    public void testAdd() throws Exception {
        AnagramService anagramService = new AnagramService(inputs.numberOfStripes);
        final Set<String> words = inputs.getWords();
        Set<String> processedWords = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        for (String word : words) {
            processedWords.add(word);

            Set<String> permutations = permutations(word);

            boolean added;
            int addCounter = 0;

            //addWord
            for (String permutation : permutations) {
                added = anagramService.addWord(permutation);

                boolean expectedAdd;
                if(addCounter == 0 ) {
                    expectedAdd = true;
                } else {
                    expectedAdd = false;
                }
                if(expectedAdd) {
                    processedWords.add(permutation);
                }
                assertEquals("[word=" + word + "][permutation=" + permutation + "][stripes=" + inputs.numberOfStripes + "]", expectedAdd, added);
                addCounter++;
            }

            Set<String> expectedAnagrams = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            expectedAnagrams.addAll(processedWords);
            expectedAnagrams.remove(word);

            for (String permutation : permutations) {
                final Set<String> anagrams = anagramService.getAnagrams(permutation);
                assertEquals("[word=" + word + "][permutation=" + permutation + "][stripes=" + inputs.numberOfStripes + "][expected=" + expectedAnagrams + "][actual=" + anagrams + "]", expectedAnagrams, anagrams);
            }
        }
    }

    @Test
    public void testDelete() throws Exception {
        AnagramService anagramService = new AnagramService(inputs.numberOfStripes);
        final Set<String> words = inputs.getWords();
        Set<String> processedWords = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        for (String word : words) {
            processedWords.add(word);



            Set<String> permutations = permutations(word);

            boolean added;
            boolean deleted;

            added = anagramService.addWord(word);
            assertTrue("[word=" + word + "][stripes=" + inputs.numberOfStripes + "]", added);
            deleted = anagramService.deleteWord(word);
            assertTrue("[word=" + word + "][stripes=" + inputs.numberOfStripes + "]", deleted);
            deleted = anagramService.deleteWord(word);
            assertFalse("[word=" + word + "][stripes=" + inputs.numberOfStripes + "]", deleted);
            added = anagramService.addWord(word);
            assertTrue("[word=" + word + "][stripes=" + inputs.numberOfStripes + "]", added);

            int permutationCount = 0;
            for (String permutation : permutations) {
                added = anagramService.addWord(permutation);

                boolean expectedAdd = (permutationCount == 0) ? false : true;

                assertEquals("[word=" + word + "][permutation=" + permutation + "][stripes=" + inputs.numberOfStripes + "]", expectedAdd, added);

                deleted = anagramService.deleteWord(permutation);
                assertTrue("[word=" + word + "][permutation=" + permutation + "][stripes=" + inputs.numberOfStripes + "]", deleted);

                deleted = anagramService.deleteWord(permutation);
                assertFalse("[word=" + word + "][permutation=" + permutation + "][stripes=" + inputs.numberOfStripes + "]", deleted);

                permutationCount++;
            }
        }
    }

    /**
     * Get all capitalization permutations of the word.
     * @param word
     * @return
     */
    private static Set<String> permutations(String word) {
        Set<String> permutations = new HashSet<>();
        char[] chars = word.toCharArray();
        for (int i = 0, n = (int) Math.pow(2, chars.length); i < n; i++) {
            char[] permutation = new char[chars.length];
            for (int j =0; j < chars.length; j++) {
                permutation[j] = (isBitSet(i, j)) ? Character.toUpperCase(chars[j]) : chars[j];
            }
            permutations.add(new String(permutation));
        }
        return permutations;
    }

    private static boolean isBitSet(int n, int offset) {
        return (n >> offset & 1) != 0;
    }

    private static void addWords(AnagramService anagramService, Set<String> words) {
        for (String word : words) {
            anagramService.addWord(word);
        }
    }

//    @Test
//    public void testGetAnagrams() throws Exception {
//
//    }
}