/*
 * Author:      Zak Nizam
 * Date:        November 14, 2026
 * Course:      CSD 420 - Advanced Java Programming
 * Assignment:  Module 10.2 Programming Assignment
 * Purpose:     Test real MySQL reads and updates and the JavaFX controls.
 *              Insert a temporary test record in a transaction, then roll back
 *              all test changes. Never create or delete a table. A failed check
 *              exits with status 1; no external test framework is needed.
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NizamFanManagerTest extends Application {
    private static int exitCode;
    private int passed;

    @Override
    public void start(Stage stage) {
        System.out.println("Zak Nizam | CSD 420 | Module 10.2 | November 14, 2026");
        System.out.println("Java " + System.getProperty("java.version")
                + " | JavaFX " + System.getProperty("javafx.runtime.version"));
        try (Connection connection = FanRepository.connect()) {
            System.out.println("MySQL " + connection.getMetaData().getDatabaseProductVersion()
                    + " | databasedb | student1");
            connection.setAutoCommit(false);
            try {
                runChecks(stage, connection);
            } finally {
                // Roll back even if an assertion or database call fails.
                connection.rollback();
            }
            System.out.println("All " + passed + " checks passed. Test changes rolled back.");
        } catch (Exception | AssertionError failure) {
            exitCode = 1;
            System.err.println("FAIL: " + failure.getMessage());
        } finally {
            stage.close();
            Platform.exit();
        }
    }

    private void runChecks(Stage stage, Connection connection) throws SQLException {
        FanRepository repository = new FanRepository(connection);
        // Find unused IDs rather than overwriting any existing fan.
        int id = Integer.MIN_VALUE;
        while (repository.find(id) != null || repository.find(id + 1) != null) {
            id += 2;
            if (id >= -1) {
                throw new SQLException("No available test IDs.");
            }
        }
        final int missingId = id + 1;
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO fans (ID, firstname, lastname, favoriteteam) VALUES (?, ?, ?, ?)")) {
            insert.setInt(1, id);
            insert.setString(2, "Test");
            insert.setString(3, "Fan");
            insert.setString(4, "Original Team");
            insert.executeUpdate();
        }
        check("Repository reads all four fields", repository.find(id).equals(
                new FanRepository.Fan(id, "Test", "Fan", "Original Team")));
        check("Missing ID returns no record", repository.find(missingId) == null);
        check("Missing ID cannot be updated", !repository.update(
                new FanRepository.Fan(missingId, "No", "Fan", "No Team")));

        NizamFanManager app = new NizamFanManager();
        Scene scene = app.createScene(repository);
        stage.setScene(scene);
        stage.show();
        check("Interface has exactly Display and Update buttons",
                scene.getRoot().lookupAll(".button").size() == 2
                && "Display".equals(app.displayButton.getText())
                && "Update".equals(app.updateButton.getText()));
        check("Editing starts disabled", app.updateButton.isDisabled()
                && !app.firstNameField.isEditable());
        app.updateFan();
        check("Update without Display is rejected", app.status.getText().contains("Display a fan"));
        app.displayButton.fire();
        check("Empty ID is rejected", app.status.getText().contains("valid integer"));
        app.idField.setText("abc");
        app.displayButton.fire();
        check("Nonnumeric ID is rejected", app.status.getText().contains("valid integer"));
        app.idField.setText("2147483648");
        app.displayButton.fire();
        check("ID outside integer range is rejected", app.status.getText().contains("valid integer"));
        app.idField.setText("  " + id + "  ");
        app.displayButton.fire();
        check("Display loads the requested record", "Test".equals(app.firstNameField.getText())
                && "Fan".equals(app.lastNameField.getText())
                && "Original Team".equals(app.teamField.getText()) && !app.updateButton.isDisabled());
        app.firstNameField.setText(" ");
        app.updateButton.fire();
        check("Blank fields do not save", app.status.getText().contains("Enter a first name")
                && "Test".equals(repository.find(id).firstName()));
        app.firstNameField.setText("A".repeat(26));
        app.updateButton.fire();
        check("Overlong first name is rejected", app.status.getText().contains("25 characters"));
        app.firstNameField.setText("Test");
        app.lastNameField.setText("B".repeat(26));
        app.updateButton.fire();
        check("Overlong last name is rejected", app.status.getText().contains("25 characters"));
        app.lastNameField.setText("Fan");
        app.teamField.setText("C".repeat(26));
        app.updateButton.fire();
        check("Overlong team is rejected", app.status.getText().contains("25 characters"));
        app.firstNameField.setText("  Alex  ");
        app.lastNameField.setText(" O'Neil ");
        app.teamField.setText(" Seattle Seahawks ");
        app.updateButton.fire();
        check("Update saves all edited fields and trims spaces", repository.find(id).equals(
                new FanRepository.Fan(id, "Alex", "O'Neil", "Seattle Seahawks"))
                && app.status.getText().contains("updated successfully"));
        app.displayButton.fire();
        check("Display reads saved values, preserving apostrophes",
                "O'Neil".equals(app.lastNameField.getText()));
        app.teamField.setText("T".repeat(25));
        app.updateButton.fire();
        check("Exactly 25 characters can be saved", repository.find(id).favoriteTeam().length() == 25);
        app.updateButton.fire();
        check("Saving unchanged values succeeds", app.status.getText().contains("updated successfully"));
        NizamFanManager failedUpdate = new NizamFanManager();
        failedUpdate.createScene(new FanRepository(connection) {
            @Override
            public boolean update(Fan fan) throws SQLException {
                throw new SQLException("Simulated connection loss before saving.");
            }
        });
        failedUpdate.idField.setText(String.valueOf(id));
        failedUpdate.displayButton.fire();
        failedUpdate.teamField.setText("Unsaved Team");
        failedUpdate.updateButton.fire();
        check("Update reports a database failure without saving",
                failedUpdate.status.getText().contains("Could not update")
                && !"Unsaved Team".equals(repository.find(id).favoriteTeam()));

        // Change only this connection's catalog to exercise an absent fans table.
        String catalog = connection.getCatalog();
        try {
            connection.setCatalog("information_schema");
            app.displayButton.fire();
            check("Missing table produces a clear message", app.status.getText().contains("fans table")
                    && app.updateButton.isDisabled());
        } finally {
            connection.setCatalog(catalog);
        }
        app.displayButton.fire();
        app.idField.setText(String.valueOf(missingId));
        check("Changing ID clears old fields and disables Update", app.firstNameField.getText().isEmpty()
                && app.updateButton.isDisabled());
        app.displayButton.fire();
        check("Display handles a nonexistent fan", app.status.getText().contains("No fan found")
                && app.teamField.getText().isEmpty());
        app.idField.setText(String.valueOf(id));
        app.displayButton.fire();
        // Remove only this uncommitted test fixture to simulate another user's deletion.
        try (PreparedStatement delete = connection.prepareStatement("DELETE FROM fans WHERE ID = ?")) {
            delete.setInt(1, id);
            delete.executeUpdate();
        }
        app.updateButton.fire();
        check("Update handles a record removed after Display", app.status.getText().contains("no longer exists")
                && app.updateButton.isDisabled());

        // A closed connection exercises the actual JDBC error paths without stopping MySQL.
        Connection closed = FanRepository.connect();
        FanRepository unavailable = new FanRepository(closed);
        NizamFanManager offline = new NizamFanManager();
        offline.createScene(unavailable);
        offline.idField.setText(String.valueOf(id));
        closed.close();
        offline.displayButton.fire();
        check("Display reports a database failure", offline.status.getText().contains("Could not display")
                && offline.updateButton.isDisabled());
        try {
            unavailable.update(new FanRepository.Fan(id, "A", "B", "C"));
            throw new AssertionError("Closed connection update should fail.");
        } catch (SQLException expected) {
            check("Repository propagates update failures", true);
        }
    }

    private void check(String description, boolean success) {
        if (!success) {
            throw new AssertionError(description);
        }
        passed++;
        System.out.println("PASS: " + description);
    }

    public static void main(String[] args) {
        launch(args);
        System.exit(exitCode);
    }
}
