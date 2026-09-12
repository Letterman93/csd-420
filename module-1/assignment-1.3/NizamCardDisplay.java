/*
 * Author:      Zak Nizam
 * Date:        September 12, 2026
 * Course:      CSD 420 - Advanced Java Programming
 * Assignment:  Module 1.3 Programming Assignment
 *
 * Purpose:
 *  This JavaFX program randomly displays four unique playing cards from a
 *  standard 52-card deck. Selecting the Refresh Cards button displays four
 *  new cards. A lambda expression handles the button's action event.
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class NizamCardDisplay extends Application {
    private static final int DECK_SIZE = 52;
    private static final int DISPLAY_COUNT = 4;
    private static final Path CARDS_DIRECTORY = Path.of("cards");

    private final Random random = new Random();
    private final List<ImageView> cardViews = new ArrayList<>();
    private List<Integer> displayedCards = List.of();

    @Override
    public void start(Stage stage) {
        Label titleLabel = new Label("Four Random Playing Cards");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        HBox cardPane = new HBox(15);
        cardPane.setAlignment(Pos.CENTER);

        for (int index = 0; index < DISPLAY_COUNT; index++) {
            ImageView cardView = createCardView();
            cardViews.add(cardView);
            cardPane.getChildren().add(cardView);
        }

        Button refreshButton = new Button("Refresh Cards");
        refreshButton.setStyle("-fx-font-size: 14px; -fx-padding: 8px 18px;");

        // Lambda expression refreshes the display when the button is selected.
        refreshButton.setOnAction(event -> displayRandomCards());

        VBox root = new VBox(20, titleLabel, cardPane, refreshButton);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #eef5ff, #c9dcf5);");

        displayRandomCards();

        Scene scene = new Scene(root, 560, 330);
        stage.setTitle("CSD 420 - Random Card Display");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Creates an ImageView with consistent dimensions for displaying a card.
     *
     * @return a configured ImageView
     */
    private ImageView createCardView() {
        ImageView cardView = new ImageView();
        cardView.setFitWidth(100);
        cardView.setFitHeight(145);
        cardView.setPreserveRatio(true);
        cardView.setSmooth(true);
        return cardView;
    }

    /**
     * Selects and displays four unique cards. On refresh, cards currently on
     * screen are excluded so that all four displayed images change.
     */
    private void displayRandomCards() {
        List<Integer> availableCards = new ArrayList<>();

        for (int cardNumber = 1; cardNumber <= DECK_SIZE; cardNumber++) {
            if (!displayedCards.contains(cardNumber)) {
                availableCards.add(cardNumber);
            }
        }

        Collections.shuffle(availableCards, random);
        displayedCards = new ArrayList<>(availableCards.subList(0, DISPLAY_COUNT));

        for (int index = 0; index < DISPLAY_COUNT; index++) {
            int cardNumber = displayedCards.get(index);
            Path imagePath = CARDS_DIRECTORY.resolve(cardNumber + ".png");

            if (!Files.isRegularFile(imagePath)) {
                throw new IllegalStateException("Missing card image: " + imagePath.toAbsolutePath());
            }

            Image cardImage = new Image(imagePath.toUri().toString());
            cardViews.get(index).setImage(cardImage);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
