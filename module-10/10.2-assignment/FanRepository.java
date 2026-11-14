/*
 * Author:      Zak Nizam
 * Date:        November 14, 2026
 * Course:      CSD 420 - Advanced Java Programming
 * Assignment:  Module 10.2 Programming Assignment
 * Purpose:     Read and update existing fan records using JDBC. This class
 *              does not create or delete tables or insert or delete records.
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FanRepository {
    private final Connection connection;

    /** A fan record uses the same four fields as the existing fans table. */
    public record Fan(int id, String firstName, String lastName, String favoriteTeam) { }

    /** Accept a connection so the application and tests can share the same queries. */
    public FanRepository(Connection connection) {
        this.connection = connection;
    }

    /** Connect to the assignment database with the required student account. */
    public static Connection connect() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException failure) {
            throw new SQLException("MySQL Connector/J was not found. Add its JAR to the classpath.", failure);
        }
        return DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/databasedb"
                + "?useSSL=false&serverTimezone=UTC&connectTimeout=5000&socketTimeout=5000",
                "student1", "pass");
    }

    /** Return the requested fan, or null when that ID is not in the table. */
    public Fan find(int id) throws SQLException {
        String sql = "SELECT ID, firstname, lastname, favoriteteam FROM fans WHERE ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? new Fan(result.getInt("ID"),
                        result.getString("firstname"), result.getString("lastname"),
                        result.getString("favoriteteam")) : null;
            }
        }
    }

    /** Update only the three text fields of an existing fan, keeping the ID intact. */
    public boolean update(Fan fan) throws SQLException {
        String sql = "UPDATE fans SET firstname = ?, lastname = ?, favoriteteam = ? WHERE ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            // Parameters keep entered names and team names separate from SQL commands.
            statement.setString(1, fan.firstName());
            statement.setString(2, fan.lastName());
            statement.setString(3, fan.favoriteTeam());
            statement.setInt(4, fan.id());
            return statement.executeUpdate() == 1;
        }
    }
}
