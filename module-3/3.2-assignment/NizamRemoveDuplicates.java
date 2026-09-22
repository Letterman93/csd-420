/*
 * Author:      Zak Nizam
 * Date:        September 22, 2026
 * Course:      CSD 420 - Advanced Java Programming
 * Assignment:  Module 3.2 Programming Assignment
 *
 * Purpose:
 *  Fill an ArrayList with 50 random integers from 1 through 20 and use a
 *  generic method to return a new ArrayList containing no duplicate values.
 */

import java.util.ArrayList;
import java.util.Random;

public class NizamRemoveDuplicates {

    public static <E> ArrayList<E> removeDuplicates(ArrayList<E> list) {
        ArrayList<E> uniqueValues = new ArrayList<>();

        for (E value : list) {
            if (!uniqueValues.contains(value)) {
                uniqueValues.add(value);
            }
        }

        return uniqueValues;
    }

    public static void main(String[] args) {
        ArrayList<Integer> originalValues = new ArrayList<>();
        Random random = new Random();

        // Add 50 random integer values ranging from 1 through 20.
        for (int count = 0; count < 50; count++) {
            originalValues.add(random.nextInt(20) + 1);
        }

        ArrayList<Integer> uniqueValues = removeDuplicates(originalValues);

        System.out.println("Original ArrayList (50 values):");
        System.out.println(originalValues);
        System.out.println();
        System.out.println("New ArrayList with duplicates removed:");
        System.out.println(uniqueValues);
        System.out.println();
        System.out.println("Original size: " + originalValues.size());
        System.out.println("New size: " + uniqueValues.size());
    }
}
