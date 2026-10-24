/*
 * Author:      Zak Nizam
 * Date:        October 24, 2026
 * Course:      CSD 420 - Advanced Java Programming
 * Assignment:  Module 7.2 Programming Assignment
 *
 * Purpose:
 *  Load the real application scene, apply its external CSS, and verify the
 *  circle count, colors, style selectors, border, and sample arrangement.
 *  Tests run on the JavaFX application thread after showing the scene.
 *  A failed check prints an error and exits with status 1. No -ea is needed.
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.css.StyleOrigin;
import javafx.css.StyleableProperty;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class NizamStyledCirclesTest extends Application {
    private static int exitCode;
    private int passed;

    @Override
    public void start(Stage stage) {
        System.out.println("Zak Nizam | CSD 420 | Module 7.2 | October 24, 2026");
        System.out.println("External CSS and four-circle correctness tests");
        try {
            Scene scene = NizamStyledCircles.createScene();
            check("Sample scene size is 300 by 250", close(scene.getWidth(), 300)
                    && close(scene.getHeight(), 250));
            stage.setScene(scene);
            stage.show();
            // Resolve CSS and layout before reading visual properties.
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            check("External mystyle.css attached", scene.getStylesheets().size() == 1
                    && scene.getStylesheets().get(0).endsWith("mystyle.css"));

            List<Circle> circles = new ArrayList<>();
            for (Node node : scene.getRoot().lookupAll(".plaincircle")) {
                if (node instanceof Circle) {
                    circles.add((Circle) node);
                }
            }
            circles.sort(Comparator.comparingDouble(circle -> sceneBounds(circle).getMinX()));
            check("Exactly four circles", countCircles(scene.getRoot()) == 4 && circles.size() == 4);
            check("Shared class and 30-pixel radius", circles.stream().allMatch(circle ->
                    circle.getStyleClass().contains("plaincircle") && close(circle.getRadius(), 30)));
            check("First circle: white fill and black stroke", colors(circles.get(0), Color.WHITE, Color.BLACK));
            check("Second circle: white fill and black stroke", colors(circles.get(1), Color.WHITE, Color.BLACK));
            check("Red ID overrides shared class", colors(circles.get(2), Color.RED, Color.RED)
                    && scene.lookup("#redcircle") == circles.get(2));
            check("Green ID overrides shared class", colors(circles.get(3), Color.GREEN, Color.GREEN)
                    && scene.lookup("#greencircle") == circles.get(3));
            check("Fill and stroke originate from stylesheet", circles.stream().allMatch(circle ->
                    ((StyleableProperty<?>) circle.fillProperty()).getStyleOrigin() == StyleOrigin.AUTHOR
                    && ((StyleableProperty<?>) circle.strokeProperty()).getStyleOrigin() == StyleOrigin.AUTHOR));

            Region frame = (Region) scene.lookup(".border");
            BorderStroke border = frame.getBorder().getStrokes().get(0);
            check("Frame: black border on all sides", Color.BLACK.equals(border.getTopStroke())
                    && Color.BLACK.equals(border.getRightStroke())
                    && Color.BLACK.equals(border.getBottomStroke())
                    && Color.BLACK.equals(border.getLeftStroke()));
            check("Frame: five-pixel border on all sides", close(border.getWidths().getTop(), 5)
                    && close(border.getWidths().getRight(), 5)
                    && close(border.getWidths().getBottom(), 5)
                    && close(border.getWidths().getLeft(), 5));
            check("Only first circle is inside the frame", circles.get(0).getParent() == frame
                    && circles.subList(1, 4).stream().noneMatch(circle -> circle.getParent() == frame));
            // Pixel snapping can shift a center slightly on a scaled display.
            check("Circles share a horizontal center line", circles.stream().allMatch(circle ->
                    Math.abs(sceneBounds(circle).getCenterY() - scene.getHeight() / 2) <= 1));
            boolean separated = true;
            for (int i = 1; i < circles.size(); i++) {
                separated &= sceneBounds(circles.get(i - 1)).getMaxX()
                        < sceneBounds(circles.get(i)).getMinX();
            }
            check("Circles appear left to right without overlap", separated);
            check("Tall frame spans the scene height", close(frame.getHeight(), scene.getHeight()));
            System.out.println("All " + passed + " test cases passed.");
        } catch (AssertionError | RuntimeException failure) {
            exitCode = 1;
            System.err.println("FAIL: " + failure.getMessage());
            failure.printStackTrace();
        } finally {
            stage.close();
            Platform.exit();
        }
    }

    /** Counts every Circle, including any that accidentally lack the CSS class. */
    private static int countCircles(Node node) {
        int count = node instanceof Circle ? 1 : 0;
        if (node instanceof javafx.scene.Parent) {
            for (Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                count += countCircles(child);
            }
        }
        return count;
    }

    private static Bounds sceneBounds(Circle circle) {
        return circle.localToScene(circle.getBoundsInLocal());
    }

    private static boolean colors(Circle circle, Color fill, Color stroke) {
        return fill.equals(circle.getFill()) && stroke.equals(circle.getStroke());
    }

    private static boolean close(double actual, double expected) {
        return Math.abs(actual - expected) < 0.01;
    }

    private void check(String name, boolean condition) {
        if (!condition) {
            throw new AssertionError(name);
        }
        passed++;
        System.out.println("PASS: " + name);
    }

    public static void main(String[] args) {
        launch(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
