/*
 * Author:      Zak Nizam
 * Date:        October 9, 2026
 * Course:      CSD 420 - Advanced Java Programming
 * Assignment:  Module 5.2 Programming Assignment
 *
 * Purpose:
 *  Read collection_of_words.txt and display each different word once,
 *  first in ascending order and then in descending order.
 *
 * Explanation:
 *  A TreeSet removes duplicates and keeps words in alphabetical order.
 *  Words are changed to lowercase, so Java and java count as one word.
 *  Punctuation and whitespace separate words. This example treats a word
 *  as a group of letters. descendingSet() provides the reverse order.
 *  Run the program from the folder containing collection_of_words.txt.
 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Scanner;
import java.util.TreeSet;

public class NizamWordCollection {

    /** Reads the file and returns its unique lowercase words in order. */
    public static TreeSet<String> readWords(Path file) throws IOException {
        TreeSet<String> words = new TreeSet<>();

        try (Scanner input = new Scanner(file, StandardCharsets.UTF_8)) {
            // Anything other than a letter separates words.
            input.useDelimiter("[^\\p{L}]+");
            while (input.hasNext()) {
                String word = input.next().toLowerCase(Locale.ROOT);
                if (!word.isEmpty()) {
                    words.add(word);
                }
            }
        }
        return words;
    }

    public static void main(String[] args) throws IOException {
        Path file = Path.of("collection_of_words.txt");
        TreeSet<String> words = readWords(file);

        System.out.println("Zak Nizam | CSD 420 | Module 5.2 | October 9, 2026");
        System.out.println("File: " + file);
        System.out.println("Unique words: " + words.size());

        System.out.println("\nAscending order:");
        for (String word : words) {
            System.out.println(word);
        }

        System.out.println("\nDescending order:");
        for (String word : words.descendingSet()) {
            System.out.println(word);
        }
    }
}
