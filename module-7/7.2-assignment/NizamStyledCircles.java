/*
 * Author:      Zak Nizam
 * Date:        October 24, 2026
 * Course:      CSD 420 - Advanced Java Programming
 * Assignment:  Module 7.2 Programming Assignment
 *
 * Purpose:
 *  Display four circles styled by the external mystyle.css stylesheet.
 *  A shared class gives the circles white fills and black strokes. IDs
 *  override those colors for the red and green circles. A bordered pane
 *  surrounds the first circle, matching the assignment's sample layout.
 */

import java.net.URL;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class NizamStyledCircles extends Application {

    /**
     * Builds the four-circle scene and attaches the external stylesheet.
     * Keep mystyle.css beside this class on the runtime classpath.
     *
     * @return a scene with two white circles, a red circle, and a green circle
     * @throws IllegalStateException if the stylesheet cannot be found
     */
    public static Scene createScene() {
        Circle first = createCircle();
        Circle second = createCircle();
        Circle red = createCircle();
        Circle green = createCircle();

        // IDs select the two color rules; the shared class remains on all four.
        red.setId("redcircle");
        green.setId("greencircle");

        // The first circle is centered inside a tall, narrow bordered pane.
        StackPane frame = new StackPane(first);
        frame.getStyleClass().add("border");
        frame.setMinWidth(70);
        frame.setPrefWidth(70);
        frame.setMaxWidth(70);

        HBox row = new HBox(5, frame, second, red, green);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(0, 15, 0, 15));
        row.getStyleClass().add("circle-row");

        Scene scene = new Scene(row, 300, 250);
        URL stylesheet = NizamStyledCircles.class.getResource("mystyle.css");
        if (stylesheet == null) {
            throw new IllegalStateException("Keep mystyle.css beside the compiled classes.");
        }
        scene.getStylesheets().add(stylesheet.toExternalForm());
        return scene;
    }

    /** Gives every circle the same radius and external CSS style class. */
    private static Circle createCircle() {
        Circle circle = new Circle(30);
        circle.getStyleClass().add("plaincircle");
        return circle;
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Module 7.2 - Styled Circles");
        stage.setScene(createScene());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
