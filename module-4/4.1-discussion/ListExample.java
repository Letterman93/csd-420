import java.util.ArrayList;
import java.util.Collections;

public class ListExample {

    public static void main(String[] args) {

        // Create an ArrayList and add several values.
        ArrayList<Integer> numbers = new ArrayList<>();

        numbers.add(42);
        numbers.add(15);
        numbers.add(8);
        numbers.add(27);
        numbers.add(63);

        System.out.println("Original: " + numbers);

        // Sort the list from smallest to largest.
        Collections.sort(numbers);
        System.out.println("Sorted: " + numbers);

        // Find the smallest and largest values.
        System.out.println("Minimum: " + Collections.min(numbers));
        System.out.println("Maximum: " + Collections.max(numbers));

        // Search for 27 in the sorted list.
        int index = Collections.binarySearch(numbers, 27);
        System.out.println("27 found at index: " + index);

        // Randomize the order of the list.
        Collections.shuffle(numbers);
        System.out.println("Shuffled: " + numbers);
    }
}
