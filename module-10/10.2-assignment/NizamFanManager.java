/*
 * Author:      Zak Nizam
 * Date:        November 14, 2026
 * Course:      CSD 420 - Advanced Java Programming
 * Assignment:  Module 10.2 Programming Assignment
 * Purpose:     Display a fan by ID and save changes to the existing record.
 *              The fans table must already exist in databasedb.
 */

import java.sql.Connection;
import java.sql.SQLException;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class NizamFanManager extends Application {
    private Connection connection;
    private FanRepository repository;
    private Integer loadedId;
    final TextField idField = new TextField();
    final TextField firstNameField = new TextField();
    final TextField lastNameField = new TextField();
    final TextField teamField = new TextField();
    final Button displayButton = new Button("Display");
    final Button updateButton = new Button("Update");
    final Label status = new Label("Enter a fan ID, then click Display.");

    @Override
    public void start(Stage stage) {
        Scene scene = createScene(null);
        try {
            connection = FanRepository.connect();
            repository = new FanRepository(connection);
        } catch (SQLException failure) {
            status.setText("Could not connect: " + failure.getMessage());
            displayButton.setDisable(true);
        }
        stage.setTitle("Module 10.2 - Fan Information");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        idField.requestFocus();
    }

    /** Build the same interface for the application and the button tests. */
    public Scene createScene(FanRepository repository) {
        this.repository = repository;
        Label title = new Label("Fan Information");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        GridPane fields = new GridPane();
        fields.setHgap(14);
        fields.setVgap(12);
        fields.addRow(0, new Label("ID:"), idField);
        fields.addRow(1, new Label("First name:"), firstNameField);
        fields.addRow(2, new Label("Last name:"), lastNameField);
        fields.addRow(3, new Label("Favorite team:"), teamField);
        idField.setPromptText("Enter an existing ID");
        for (TextField field : new TextField[] {idField, firstNameField, lastNameField, teamField}) {
            field.setPrefColumnCount(22);
        }
        clearRecord();
        // Changing the search ID requires another Display before an update is allowed.
        idField.textProperty().addListener((observable, before, after) -> {
            clearRecord();
            status.setText("Enter a fan ID, then click Display.");
        });
        displayButton.setOnAction(event -> displayFan());
        updateButton.setOnAction(event -> updateFan());
        HBox buttons = new HBox(12, displayButton, updateButton);
        displayButton.setPrefWidth(95);
        updateButton.setPrefWidth(95);
        status.setWrapText(true);
        status.setMinHeight(40);
        VBox root = new VBox(16, title, fields, buttons, status);
        root.setPadding(new Insets(24));
        return new Scene(root, 460, 365);
    }

    /** Validate the search ID, then fill the form from the database. */
    public void displayFan() {
        final int id;
        try {
            id = Integer.parseInt(idField.getText().trim());
        } catch (NumberFormatException failure) {
            clearRecord();
            status.setText("Enter a valid integer ID.");
            return;
        }
        try {
            FanRepository.Fan fan = repository.find(id);
            clearRecord();
            if (fan == null) {
                status.setText("No fan found with ID " + id + ".");
                return;
            }
            firstNameField.setText(fan.firstName());
            lastNameField.setText(fan.lastName());
            teamField.setText(fan.favoriteTeam());
            loadedId = id;
            setEditing(true);
            status.setText("Fan " + id + " displayed. Edit the fields, then click Update.");
        } catch (SQLException failure) {
            clearRecord();
            status.setText("Could not display the fan. Check the database connection and fans table.");
        }
    }

    /** Save the edited names and team for the fan that was last displayed. */
    public void updateFan() {
        if (loadedId == null) {
            status.setText("Display a fan before updating.");
            return;
        }
        String first = firstNameField.getText().trim();
        String last = lastNameField.getText().trim();
        String team = teamField.getText().trim();
        if (first.isEmpty() || last.isEmpty() || team.isEmpty()) {
            status.setText("Enter a first name, last name, and favorite team.");
            return;
        }
        if (tooLong(first) || tooLong(last) || tooLong(team)) {
            status.setText("Each name and team must be 25 characters or fewer.");
            return;
        }
        try {
            if (repository.update(new FanRepository.Fan(loadedId, first, last, team))) {
                firstNameField.setText(first);
                lastNameField.setText(last);
                teamField.setText(team);
                status.setText("Fan " + loadedId + " updated successfully.");
            } else {
                clearRecord();
                status.setText("This fan no longer exists. Display a record again.");
            }
        } catch (SQLException failure) {
            status.setText("Could not update the fan. Check the database connection and try again.");
        }
    }

    private static boolean tooLong(String value) {
        return value.codePointCount(0, value.length()) > 25;
    }

    private void clearRecord() {
        loadedId = null;
        firstNameField.clear();
        lastNameField.clear();
        teamField.clear();
        setEditing(false);
    }

    private void setEditing(boolean enabled) {
        firstNameField.setEditable(enabled);
        lastNameField.setEditable(enabled);
        teamField.setEditable(enabled);
        updateButton.setDisable(!enabled);
    }

    /** Release the JDBC connection when the window closes. */
    @Override
    public void stop() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
