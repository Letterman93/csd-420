public class RecursiveBinarySearch {
    public static int binarySearch(int[] numbers, int target,
                                   int low, int high) {

        // Base case: no values are left to search.
        if (low > high) {
            return -1;
        }

        // Find the middle position of the current search range.
        int mid = low + (high - low) / 2;

        System.out.println("Searching indexes " + low +
                           " through " + high);

        // Base case: the target was found.
        if (numbers[mid] == target) {
            return mid;
        }

        // Search the left half.
        if (target < numbers[mid]) {
            return binarySearch(numbers, target, low, mid - 1);
        }

        // Search the right half.
        return binarySearch(numbers, target, mid + 1, high);
    }

    public static void main(String[] args) {

        int[] numbers = {5, 12, 18, 27, 34, 42, 56, 63, 71};
        int target = 42;

        int result = binarySearch(
                numbers, target, 0, numbers.length - 1);

        if (result == -1) {
            System.out.println("Value not found.");
        } else {
            System.out.println(target +
                               " was found at index " + result);
        }
    }
}
