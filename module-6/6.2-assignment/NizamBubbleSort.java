/*
 * Author:      Zak Nizam
 * Date:        October 14, 2026
 * Course:      CSD 420 - Advanced Java Programming
 * Assignment:  Module 6.2 Programming Assignment
 *
 * Purpose:
 *  Demonstrate two generic bubble sort methods: natural ordering through
 *  Comparable and a separate ordering rule through Comparator.
 *
 * Explanation:
 *  Each pass compares neighboring elements and swaps an out-of-order pair.
 *  The last unsorted element reaches its final position after each pass.
 *  A pass without swaps ends the sort early. Both methods change the supplied
 *  array in place and preserve the original order of equal elements.
 *  Worst-case time is O(n^2), best-case time is O(n), and extra space is O(1).
 */

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class NizamBubbleSort {

    /**
     * Sorts an array in ascending natural order using bubble sort.
     * Elements must be non-null and mutually comparable.
     *
     * @param <E> element type with a natural ordering
     * @param list array to sort in place
     * @throws NullPointerException if the array is null
     */
    public static <E extends Comparable<? super E>> void bubbleSort(E[] list) {
        Objects.requireNonNull(list, "The array must not be null.");
        for (int end = list.length - 1; end > 0; end--) {
            boolean swapped = false;
            for (int index = 0; index < end; index++) {
                if (list[index].compareTo(list[index + 1]) > 0) {
                    E temporary = list[index];
                    list[index] = list[index + 1];
                    list[index + 1] = temporary;
                    swapped = true;
                }
            }
            // No swaps means every remaining neighboring pair is ordered.
            if (!swapped) {
                break;
            }
        }
    }

    /**
     * Sorts an array using bubble sort and the supplied comparison rule.
     * Elements must be supported by the comparator, including any null values.
     *
     * @param <E> element type
     * @param list array to sort in place
     * @param comparator rule that determines the desired ordering
     * @throws NullPointerException if the array or comparator is null
     */
    public static <E> void bubbleSort(E[] list, Comparator<? super E> comparator) {
        Objects.requireNonNull(list, "The array must not be null.");
        Objects.requireNonNull(comparator, "The comparator must not be null.");
        for (int end = list.length - 1; end > 0; end--) {
            boolean swapped = false;
            for (int index = 0; index < end; index++) {
                if (comparator.compare(list[index], list[index + 1]) > 0) {
                    E temporary = list[index];
                    list[index] = list[index + 1];
                    list[index + 1] = temporary;
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }
    }

    /** Displays examples of natural ordering and custom ordering. */
    public static void main(String[] args) {
        System.out.println("Zak Nizam | CSD 420 | Module 6.2 | October 14, 2026");
        System.out.println("Generic bubble sort examples");

        Integer[] numbers = {42, 7, 19, 7, -3, 0};
        System.out.println("\nComparable - integers ascending");
        System.out.println("Before: " + Arrays.toString(numbers));
        bubbleSort(numbers);
        System.out.println("After:  " + Arrays.toString(numbers));

        String[] words = {"pear", "apple", "orange", "banana"};
        System.out.println("\nComparable - strings alphabetical");
        System.out.println("Before: " + Arrays.toString(words));
        bubbleSort(words);
        System.out.println("After:  " + Arrays.toString(words));

        System.out.println("\nComparator - integers descending");
        System.out.println("Before: " + Arrays.toString(numbers));
        bubbleSort(numbers, Comparator.reverseOrder());
        System.out.println("After:  " + Arrays.toString(numbers));

        // Length is independent of the natural alphabetical ordering.
        System.out.println("\nComparator - strings by length, then alphabetical");
        System.out.println("Before: " + Arrays.toString(words));
        bubbleSort(words, Comparator.comparingInt(String::length)
                .thenComparing(Comparator.naturalOrder()));
        System.out.println("After:  " + Arrays.toString(words));
    }
}
