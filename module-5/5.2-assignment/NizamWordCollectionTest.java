/*
 * Author:      Zak Nizam
 * Date:        October 9, 2026
 * Course:      CSD 420 - Advanced Java Programming
 * Assignment:  Module 5.2 Programming Assignment
 *
 * Purpose:
 *  Check duplicate removal, ascending and descending order, mixed case,
 *  punctuation, empty files, whitespace, and repeated words. Also check
 *  the submitted word file and a missing file. Temporary files are removed.
 *  A failed check throws AssertionError; the -ea option is not required.
 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class NizamWordCollectionTest {

    private static int passed;

    public static void main(String[] args) throws IOException {
        System.out.println("Zak Nizam | CSD 420 | Module 5.2 | October 9, 2026");
        System.out.println("Word collection correctness tests");

        checkFile("Duplicates and ordering", "pear apple pear banana apple",
                Arrays.asList("apple", "banana", "pear"));
        checkFile("Mixed capitalization", "JAVA java Java Python PYTHON",
                Arrays.asList("java", "python"));
        checkFile("Punctuation", ",pear! apple; banana. pear?",
                Arrays.asList("apple", "banana", "pear"));
        checkFile("Empty file", "", Collections.emptyList());
        checkFile("Whitespace only", " \n\t  \r\n", Collections.emptyList());
        checkFile("One repeated word", "map map map", Arrays.asList("map"));

        checkWords("Submitted word file",
                NizamWordCollection.readWords(Path.of("collection_of_words.txt")),
                Arrays.asList("array", "hashmap", "hashset", "java", "linkedlist",
                        "list", "map", "python", "queue", "set", "stack", "tree"));

        Path missing = Files.createTempFile("csd420-missing-", ".txt");
        Files.delete(missing);
        boolean rejected = false;
        try {
            NizamWordCollection.readWords(missing);
        } catch (IOException expected) {
            rejected = true;
        }
        require(rejected, "Missing file must report an error");
        pass("Missing file");

        System.out.println("All " + passed + " test cases passed.");
    }

    /** Uses a temporary input file and checks that reading does not change it. */
    private static void checkFile(String name, String text, List<String> expected)
            throws IOException {
        Path file = Files.createTempFile("csd420-words-", ".txt");
        try {
            Files.writeString(file, text, StandardCharsets.UTF_8);
            TreeSet<String> words = NizamWordCollection.readWords(file);
            require(Files.readString(file, StandardCharsets.UTF_8).equals(text),
                    name + ": input file was changed");
            checkWords(name, words, expected);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private static void checkWords(String name, TreeSet<String> words,
                                   List<String> expectedAscending) {
        require(new ArrayList<>(words).equals(expectedAscending),
                name + ": incorrect ascending words or duplicates");

        List<String> expectedDescending = new ArrayList<>(expectedAscending);
        Collections.reverse(expectedDescending);
        require(new ArrayList<>(words.descendingSet()).equals(expectedDescending),
                name + ": incorrect descending words");
        pass(name);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void pass(String name) {
        passed++;
        System.out.println("PASS: " + name);
    }
}
