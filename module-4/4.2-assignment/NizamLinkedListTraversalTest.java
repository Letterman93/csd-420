/*
 * Author:      Zak Nizam
 * Date:        October 4, 2026
 * Course:      CSD 420 - Advanced Java Programming
 * Assignment:  Module 4.2 Programming Assignment
 *
 * Purpose:
 *  Test list creation and both traversal methods using known expected
 *  results, edge cases, overflow protection, and unchanged input lists.
 *  These checks throw AssertionError on failure and do not require -ea.
 */

import java.util.Arrays;
import java.util.LinkedList;

public class NizamLinkedListTraversalTest {

    private static int passed;

    public static void main(String[] args) {
        System.out.println("Zak Nizam | CSD 420 | Module 4.2 | October 4, 2026");
        System.out.println("LinkedList traversal correctness tests");

        checkList("Empty list", new LinkedList<>(), 0);
        checkList("Single value", new LinkedList<>(Arrays.asList(42)), 42);
        checkList("Duplicates and negative values",
                new LinkedList<>(Arrays.asList(7, -3, 7, 0, -11)), 0);
        checkList("Sum exceeds Integer.MAX_VALUE",
                new LinkedList<>(Arrays.asList(Integer.MAX_VALUE,
                        Integer.MAX_VALUE, Integer.MAX_VALUE)), 6_442_450_941L);

        LinkedList<Integer> generated = NizamLinkedListTraversal.createList(100);
        require(generated.size() == 100, "Generated list size");
        int expected = 0;
        for (int value : generated) {
            require(value == expected++, "Generated list order");
        }
        checkList("Generated 0 through 99", generated, 4_950);

        require(NizamLinkedListTraversal.createList(0).isEmpty(),
                "Zero-size list must be empty");
        pass("Zero-size creation");

        boolean rejected = false;
        try {
            NizamLinkedListTraversal.createList(-1);
        } catch (IllegalArgumentException expectedException) {
            rejected = true;
        }
        require(rejected, "Negative size must be rejected");
        pass("Negative-size rejection");

        System.out.println("All " + passed + " test cases passed.");
        System.out.println("The timing program also validates 50,000 and 500,000 values.");
    }

    private static void checkList(String name, LinkedList<Integer> list,
                                  long expectedSum) {
        LinkedList<Integer> original = new LinkedList<>(list);
        require(NizamLinkedListTraversal.sumUsingIterator(list) == expectedSum,
                name + ": incorrect iterator sum");
        require(list.equals(original), name + ": iterator changed the list");
        require(NizamLinkedListTraversal.sumUsingGet(list) == expectedSum,
                name + ": incorrect indexed sum");
        require(list.equals(original), name + ": indexed traversal changed the list");
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
