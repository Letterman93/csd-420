/*
 * Author:      Zak Nizam
 * Date:        October 4, 2026
 * Course:      CSD 420 - Advanced Java Programming
 * Assignment:  Module 4.2 Programming Assignment
 *
 * Purpose:
 *  Store 50,000 and then 500,000 integers in a LinkedList and compare the
 *  time needed to traverse each list using an iterator and get(index).
 *
 * Results and discussion:
 *  An iterator moves directly from one node to the next, so a complete
 *  traversal takes O(n) time. LinkedList.get(index) starts at the nearer
 *  end and walks to the requested node. Repeating that lookup for every
 *  index takes O(n^2) time, even though the list is doubly linked.
 *  Increasing the list size tenfold suggests about ten times
 *  the iterator work and about 100 times the indexed traversal work.
 *  Actual times also depend on JVM warm-up, caching, and other running
 *  programs. Each size is measured once, with the iterator timed first.
 *
 *  Measured on this Windows computer with Temurin JDK 25.0.4.1:
 *  50,000 integers: iterator 2.221 ms; get(index) 1,263.671 ms.
 *  500,000 integers: iterator 5.881 ms; get(index) 156,301.472 ms.
 *  The larger indexed traversal took about 156.301 seconds instead of
 *  1.264 seconds, a 123.69-fold increase. The iterator time grew 2.65-fold
 *  in this run. Small timings are affected by JVM optimization
 *  and measurement noise, so these ratios will not be exactly 10 or 100.
 *  Both methods produced the correct sums at both sizes. An iterator is
 *  the practical choice for sequential LinkedList traversal.
 */

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

public class NizamLinkedListTraversal {

    // Keep warm-up results observable so their calculations are used.
    private static volatile long warmUpSum;

    /** Creates a list containing the integers 0 through size - 1. */
    public static LinkedList<Integer> createList(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("List size cannot be negative.");
        }

        LinkedList<Integer> numbers = new LinkedList<>();
        for (int value = 0; value < size; value++) {
            numbers.add(value);
        }
        return numbers;
    }

    /** Visits each value once using an explicit iterator. */
    public static long sumUsingIterator(LinkedList<Integer> numbers) {
        long sum = 0;
        Iterator<Integer> iterator = numbers.iterator();
        while (iterator.hasNext()) {
            sum += iterator.next();
        }
        return sum;
    }

    /** Visits every index, requiring a fresh node lookup for each get. */
    public static long sumUsingGet(LinkedList<Integer> numbers) {
        long sum = 0;
        int size = numbers.size();
        for (int index = 0; index < size; index++) {
            sum += numbers.get(index);
        }
        return sum;
    }

    public static void main(String[] args) {
        System.out.println("Zak Nizam | CSD 420 | Module 4.2 | October 4, 2026");
        System.out.println("LinkedList traversal: iterator versus get(index)");
        System.out.println("List creation, validation, and printing are not timed.");
        System.out.println();

        // Use a small list to warm up both methods before measuring.
        LinkedList<Integer> warmUpList = createList(5_000);
        for (int repeat = 0; repeat < 5; repeat++) {
            warmUpSum = sumUsingIterator(warmUpList);
            warmUpSum = sumUsingGet(warmUpList);
        }

        long[] smallerTimes = runComparison(50_000);
        long[] largerTimes = runComparison(500_000);

        System.out.printf(Locale.US,
                "Time growth (500,000 / 50,000): iterator %.2fx; get(index) %.2fx%n",
                (double) largerTimes[0] / smallerTimes[0],
                (double) largerTimes[1] / smallerTimes[1]);
        System.out.println("Both sizes passed validation. Times vary between runs.");
    }

    /** Returns iterator and indexed traversal durations in nanoseconds. */
    private static long[] runComparison(int size) {
        LinkedList<Integer> numbers = createList(size);
        validateStoredValues(numbers, size);
        long expectedSum = (long) size * (size - 1) / 2;

        System.out.printf(Locale.US, "List size: %,d integers%n", size);
        long start = System.nanoTime();
        long iteratorSum = sumUsingIterator(numbers);
        long iteratorTime = System.nanoTime() - start;

        System.out.println("Running get(index) traversal; please wait...");
        start = System.nanoTime();
        long indexedSum = sumUsingGet(numbers);
        long indexedTime = System.nanoTime() - start;

        if (iteratorSum != expectedSum || indexedSum != expectedSum
                || numbers.size() != size) {
            throw new AssertionError("Traversal validation failed for size " + size);
        }

        System.out.printf(Locale.US, "Iterator:   %,.3f ms | sum: %,d%n",
                iteratorTime / 1_000_000.0, iteratorSum);
        System.out.printf(Locale.US, "get(index): %,.3f ms | sum: %,d%n",
                indexedTime / 1_000_000.0, indexedSum);
        System.out.printf(Locale.US, "get(index) / iterator: %,.2fx%n",
                (double) indexedTime / iteratorTime);
        System.out.println("PASS: stored values, list size, and both sums are correct.");
        System.out.println();
        return new long[] {iteratorTime, indexedTime};
    }

    /** Checks the full stored sequence without expensive indexed access. */
    private static void validateStoredValues(LinkedList<Integer> numbers, int size) {
        int expectedValue = 0;
        for (int value : numbers) {
            if (value != expectedValue) {
                throw new AssertionError("Incorrect stored value at " + expectedValue);
            }
            expectedValue++;
        }
        if (numbers.size() != size || expectedValue != size) {
            throw new AssertionError("Incorrect number of stored integers.");
        }
    }
}
