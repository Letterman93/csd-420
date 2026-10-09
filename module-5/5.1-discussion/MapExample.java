import java.util.HashMap;
import java.util.Map;

public class MapExample {

    public static void main(String[] args) {

        // Create a map where names are keys and scores are values.
        Map<String, Integer> scores = new HashMap<>();

        // Add several key-value pairs.
        scores.put("Emma", 92);
        scores.put("Leo", 85);
        scores.put("Sam", 97);

        // Show the original key-value pairs.
        System.out.println("Original scores:");
        System.out.println(scores);

        // Retrieve a value by using its key.
        System.out.println("\nRetrieve Leo's score using get(\"Leo\"):");
        System.out.println(scores.get("Leo"));

        // Update an existing value.
        System.out.println("\nChange Leo's score using put(\"Leo\", 90):");
        scores.put("Leo", 90);

        // Show the map again after updating Leo's score.
        System.out.println("\nUpdated scores:");
        System.out.println(scores);
    }
}
