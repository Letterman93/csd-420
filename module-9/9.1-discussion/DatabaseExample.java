import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseExample {

    public static void main(String[] args) {

        // Use the MySQL URL from the post unless a different JDBC URL is supplied.
        // The local screenshot uses an embedded H2 database and its JDBC driver.
        String url = args.length > 0
            ? args[0]
            : "jdbc:mysql://localhost:3306/application";
        String username = "root";
        String password = "password";

        String query =
            "SELECT id, username, balance FROM accounts";

        // These resources are closed automatically when the try block finishes.
        try (
            Connection connection =
                DriverManager.getConnection(url, username, password);

            Statement statement =
                connection.createStatement();

            ResultSet results =
                statement.executeQuery(query)
        ) {

            System.out.println("Connected to the database.\n");

            // next() moves the cursor from before the first row through each record.
            while (results.next()) {

                int id = results.getInt("id");
                String usernameValue =
                    results.getString("username");
                double balance =
                    results.getDouble("balance");

                System.out.println(
                    id + " | "
                    + usernameValue + " | $"
                    + balance
                );
            }

        } catch (Exception e) {
            System.out.println(
                "Database error: " + e.getMessage()
            );
        }
    }
}
