import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BatchExample {

    public static void main(String[] args) {

        // Use the post's MySQL connection unless a local JDBC URL is supplied.
        // The screenshot uses an embedded H2 database with its JDBC driver.
        String url = args.length > 0
            ? args[0]
            : "jdbc:mysql://localhost:3306/application";
        String user = "root";
        String password = "password";

        String sql =
            "INSERT INTO accounts (username, balance) VALUES (?, ?)";

        // Close the connection and statement automatically after use.
        try (
            Connection connection =
                DriverManager.getConnection(url, user, password);

            PreparedStatement statement =
                connection.prepareStatement(sql)
        ) {

            // Each addBatch() saves the current values for the INSERT statement.
            statement.setString(1, "Alex");
            statement.setDouble(2, 250.00);
            statement.addBatch();

            statement.setString(1, "Jordan");
            statement.setDouble(2, 475.50);
            statement.addBatch();

            statement.setString(1, "Taylor");
            statement.setDouble(2, 125.75);
            statement.addBatch();

            // Execute the three queued commands and receive their update counts.
            int[] results = statement.executeBatch();

            System.out.println(
                results.length + " records processed."
            );

        } catch (SQLException e) {
            System.out.println(
                "Database error: " + e.getMessage()
            );
        }
    }
}
