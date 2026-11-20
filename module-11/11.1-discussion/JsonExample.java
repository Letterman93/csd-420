import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonExample {

    public static void main(String[] args) throws Exception {

        // A text block keeps the JSON formatted the same way it appears in a file.
        String json = """
            {
              "username": "Alex",
              "active": true,
              "score": 92
            }
            """;

        // Jackson's ObjectMapper parses the JSON text into a tree of nodes.
        ObjectMapper mapper = new ObjectMapper();
        JsonNode data = mapper.readTree(json);

        // Find each field by name and convert its value to the Java type needed.
        String username = data.get("username").asText();
        boolean active = data.get("active").asBoolean();
        int score = data.get("score").asInt();

        System.out.println("Username: " + username);
        System.out.println("Active: " + active);
        System.out.println("Score: " + score);
    }
}
