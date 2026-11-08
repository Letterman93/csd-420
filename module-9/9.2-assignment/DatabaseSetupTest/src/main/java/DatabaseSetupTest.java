/*
 * Author:      Zak Nizam
 * Date:        November 8, 2026
 * Course:      CSD 420 - Advanced Java Programming
 * Assignment:  Module 9.2 Programming Assignment
 *
 * Purpose:
 *  Test the local MySQL 5 setup by connecting to databasedb as student1.
 *  Check the server version, database, account, and ability to write and read
 *  a record. The test table is temporary and disappears when the connection
 *  closes. Run this Maven project in NetBeans using the installed JDK.
 */

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetupTest {
    private static final String URL =
            "jdbc:mysql://127.0.0.1:3306/databasedb?useSSL=false&serverTimezone=UTC";
    private static final String USERNAME = "student1";
    private static final String PASSWORD = "pass";

    public static void main(String[] args) {
        System.out.println("Zak Nizam | CSD 420 | Module 9.2 | November 8, 2026");
        System.out.println("Java: " + System.getProperty("java.version"));

        // JDBC loads the MySQL driver supplied by the Maven dependency.
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            DatabaseMetaData metadata = connection.getMetaData();
            System.out.println("MySQL server: " + metadata.getDatabaseProductVersion());
            System.out.println("JDBC driver: " + metadata.getDriverVersion());
            require(connection.isValid(5), "The JDBC connection is valid");
            require(metadata.getDatabaseProductName().equals("MySQL")
                    && metadata.getDatabaseProductVersion().startsWith("5."), "MySQL 5 is running");

            // Ask the server which database and account are actually in use.
            try (Statement statement = connection.createStatement();
                 ResultSet results = statement.executeQuery("SELECT DATABASE(), CURRENT_USER()")) {
                if (!results.next()) {
                    throw new SQLException("The server returned no connection details.");
                }
                String database = results.getString(1);
                String account = results.getString(2);
                System.out.println("Database: " + database);
                System.out.println("Connected account: " + account);
                require("databasedb".equals(database), "The database is databasedb");
                require(account.startsWith("student1@"), "The authenticated account is student1");
            }

            // Exercise this account's database permissions without leaving a test table.
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TEMPORARY TABLE connection_check "
                        + "(id INT PRIMARY KEY, message VARCHAR(60) NOT NULL)");
            }
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO connection_check (id, message) VALUES (?, ?)")) {
                insert.setInt(1, 1);
                insert.setString(2, "Java connected successfully");
                require(insert.executeUpdate() == 1, "The account can create and insert data");
            }
            try (Statement statement = connection.createStatement();
                 ResultSet results = statement.executeQuery("SELECT id, message FROM connection_check")) {
                boolean correct = results.next() && results.getInt("id") == 1
                        && "Java connected successfully".equals(results.getString("message"));
                require(correct && !results.next(), "The inserted record can be read back correctly");
            }
            System.out.println("All 6 setup checks passed.");
        } catch (SQLException failure) {
            System.err.println("Setup test failed: " + failure.getMessage());
            System.exit(1);
        }
    }

    /** Prints a passed check or fails the program if the setup does not match. */
    private static void require(boolean condition, String description) throws SQLException {
        if (!condition) {
            throw new SQLException(description);
        }
        System.out.println("PASS: " + description);
    }
}
