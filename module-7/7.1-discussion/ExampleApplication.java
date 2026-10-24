import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Starts the JavaFX application; the layout and event handling are in separate files.
public class ExampleApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Find Example.fxml beside this class and load its layout and controller.
        Parent root = FXMLLoader.load(
                ExampleApplication.class.getResource("Example.fxml")
        );

        // Place the loaded layout in a scene and display it in the application window.
        stage.setTitle("FXML Controller Example");
        stage.setScene(new Scene(root, 360, 140));
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        // Start JavaFX, which calls the start method above.
        launch(args);
    }
}
