import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ExampleController {

    // FXMLLoader connects this field to the label with fx:id="messageLabel".
    // @FXML allows the loader to access this private field.
    @FXML
    private Label messageLabel;

    // The FXML button's onAction="#handleButtonClick" calls this private method.
    @FXML
    private void handleButtonClick() {
        // Update the existing label when the user clicks the button.
        messageLabel.setText("The button was clicked!");
    }
}
