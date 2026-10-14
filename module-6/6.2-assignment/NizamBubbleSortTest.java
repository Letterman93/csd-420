/*
 * Author:      Zak Nizam
 * Date:        October 14, 2026
 * Course:      CSD 420 - Advanced Java Programming
 * Assignment:  Module 6.2 Programming Assignment
 *
 * Purpose:
 *  Verify both generic bubble sort overloads with known expected results,
 *  boundary inputs, stable ordering, custom objects, and random arrays.
 *  Arrays.sort is used only as an independent reference in the tests.
 *  A failed test throws AssertionError; enabling -ea is not required.
 */

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class NizamBubbleSortTest {
    private static int passed;

    public static void main(String[] args) {
        System.out.println("Zak Nizam | CSD 420 | Module 6.2 | October 14, 2026");
        System.out.println("Generic bubble sort correctness tests");

        checkNatural("Integers: negatives and duplicates",
                new Integer[]{4, -2, 0, 4, 1}, new Integer[]{-2, 0, 1, 4, 4});
        checkNatural("Strings: natural ordering", new String[]{"pear", "apple", "banana"},
                new String[]{"apple", "banana", "pear"});
        checkNatural("Doubles", new Double[]{2.5, -1.2, 0.0}, new Double[]{-1.2, 0.0, 2.5});
        checkNatural("Comparable: empty array", new Integer[]{}, new Integer[]{});
        checkNatural("Comparable: single element", new Integer[]{8}, new Integer[]{8});
        checkNatural("Comparable: already sorted", new Integer[]{1, 2, 3}, new Integer[]{1, 2, 3});
        checkNatural("Comparable: reverse sorted", new Integer[]{3, 2, 1}, new Integer[]{1, 2, 3});
        checkNatural("Comparable: all equal", new Integer[]{2, 2, 2}, new Integer[]{2, 2, 2});

        checkCustom("Comparator: descending", new Integer[]{2, 1, 3, 2},
                new Integer[]{3, 2, 2, 1}, Comparator.reverseOrder());
        checkCustom("Comparator: case insensitive", new String[]{"pear", "APPLE", "Banana"},
                new String[]{"APPLE", "Banana", "pear"}, String.CASE_INSENSITIVE_ORDER);
        checkCustom("Comparator: length and tie breaker", new String[]{"pear", "fig", "kiwi", "apple"},
                new String[]{"fig", "kiwi", "pear", "apple"},
                Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()));
        checkCustom("Comparator: empty array", new Integer[]{}, new Integer[]{}, Comparator.naturalOrder());
        checkCustom("Comparator: single element", new Integer[]{8}, new Integer[]{8}, Comparator.reverseOrder());
        checkCustom("Comparator: already sorted", new Integer[]{3, 2, 1},
                new Integer[]{3, 2, 1}, Comparator.reverseOrder());
        checkCustom("Comparator: all equal", new Integer[]{2, 2, 2},
                new Integer[]{2, 2, 2}, Comparator.naturalOrder());
        checkCustom("Comparator: null-aware rule", new Integer[]{2, null, 1},
                new Integer[]{1, 2, null}, Comparator.nullsLast(Comparator.naturalOrder()));

        // Comparable items have equal keys but different identities to test stability.
        Item first = new Item("first", 2);
        Item second = new Item("second", 2);
        Item low = new Item("low", 1);
        checkNatural("Comparable: stable equal keys", new Item[]{first, low, second},
                new Item[]{low, first, second});

        // Ticket does not implement Comparable, so ordering comes from the comparator.
        Ticket a = new Ticket("first", 2);
        Ticket b = new Ticket("second", 2);
        Ticket c = new Ticket("urgent", 1);
        checkCustom("Comparator: custom objects and stable ties", new Ticket[]{a, c, b},
                new Ticket[]{c, a, b}, Comparator.comparingInt(ticket -> ticket.priority));

        ChildItem childHigh = new ChildItem("high", 3);
        ChildItem childLow = new ChildItem("low", 1);
        checkNatural("Inherited Comparable implementation", new ChildItem[]{childHigh, childLow},
                new ChildItem[]{childLow, childHigh});

        expectNull("Comparable: null array rejected",
                () -> NizamBubbleSort.bubbleSort((Integer[]) null));
        expectNull("Comparator: null array rejected",
                () -> NizamBubbleSort.bubbleSort((Integer[]) null, Comparator.naturalOrder()));
        expectNull("Comparator: null rule rejected",
                () -> NizamBubbleSort.bubbleSort(new Integer[]{}, null));

        checkRandomArrays();
        System.out.println("All " + passed + " test cases passed.");
    }

    /** Checks an exact expected order after sorting the supplied array in place. */
    private static <E extends Comparable<? super E>> void checkNatural(
            String name, E[] input, E[] expected) {
        NizamBubbleSort.bubbleSort(input);
        require(Arrays.equals(input, expected), name + ": " + Arrays.toString(input));
        pass(name);
    }

    private static <E> void checkCustom(String name, E[] input, E[] expected,
                                       Comparator<? super E> comparator) {
        NizamBubbleSort.bubbleSort(input, comparator);
        require(Arrays.equals(input, expected), name + ": " + Arrays.toString(input));
        pass(name);
    }

    /** Uses a fixed seed so failures can be reproduced; checks both overloads. */
    private static void checkRandomArrays() {
        Random random = new Random(42062);
        for (int trial = 0; trial < 100; trial++) {
            Integer[] input = new Integer[random.nextInt(41)];
            for (int i = 0; i < input.length; i++) {
                input[i] = random.nextInt(21) - 10;
            }
            Integer[] expected = input.clone();
            Arrays.sort(expected);
            Integer[] actual = input.clone();
            NizamBubbleSort.bubbleSort(actual);
            require(Arrays.equals(actual, expected), "Random natural ordering trial " + trial);

            Arrays.sort(expected, Comparator.reverseOrder());
            actual = input.clone();
            NizamBubbleSort.bubbleSort(actual, Comparator.reverseOrder());
            require(Arrays.equals(actual, expected), "Random comparator trial " + trial);
        }
        pass("100 random arrays checked with both overloads");
    }

    private static void expectNull(String name, Runnable operation) {
        boolean rejected = false;
        try {
            operation.run();
        } catch (NullPointerException expected) {
            rejected = true;
        }
        require(rejected, name);
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

    /** Natural ordering uses only the key; identity distinguishes equal-key items. */
    private static class Item implements Comparable<Item> {
        private final String name;
        private final int key;
        Item(String name, int key) { this.name = name; this.key = key; }
        @Override public int compareTo(Item other) { return Integer.compare(key, other.key); }
        @Override public String toString() { return name + "(" + key + ")"; }
    }

    private static class ChildItem extends Item {
        ChildItem(String name, int key) { super(name, key); }
    }

    private static class Ticket {
        private final String name;
        private final int priority;
        Ticket(String name, int priority) { this.name = name; this.priority = priority; }
        @Override public String toString() { return name + "(" + priority + ")"; }
    }
}
