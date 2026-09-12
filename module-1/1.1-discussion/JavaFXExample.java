import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class JavaFXExample extends Application {

    @Override
    public void start(Stage stage) {
        Pane root = new Pane();

        // Creates a blue gradient for the background.
        LinearGradient backgroundGradient = new LinearGradient(
                0, 0, 1, 1,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.DARKBLUE),
                new Stop(1, Color.LIGHTBLUE)
        );

        Rectangle background = new Rectangle(500, 350);
        background.setFill(backgroundGradient);

        // Creates a radial gradient for the circle.
        RadialGradient circleGradient = new RadialGradient(
                0, 0,
                0.5, 0.5,
                0.5,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.GOLD),
                new Stop(1, Color.DARKORANGE)
        );

        // Creates a circle in the center of the window.
        Circle circle = new Circle(250, 175, 80);
        circle.setFill(circleGradient);

        // Creates an arc around the circle.
        Arc arc = new Arc(250, 175, 110, 110, 45, 270);
        arc.setType(ArcType.OPEN);
        arc.setFill(Color.TRANSPARENT);
        arc.setStroke(Color.WHITE);
        arc.setStrokeWidth(8);

        // Adds the shapes to the window.
        root.getChildren().addAll(background, circle, arc);

        Scene scene = new Scene(root, 500, 350);
        stage.setTitle("JavaFX Example");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
