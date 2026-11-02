/*
 * Author:      Zak Nizam
 * Date:        November 1, 2026
 * Course:      CSD 420 - Advanced Java Programming
 * Assignment:  Module 8.2 Programming Assignment
 *
 * Purpose:
 *  Use three worker threads to generate 10,000 letters, 10,000 digits, and
 *  10,000 symbols. Display each character in a JavaFX text area as it is
 *  generated, allowing output from the three threads to mix together.
 *
 * Thread safety:
 *  Workers generate the characters, but only the JavaFX application thread
 *  changes the controls. Each worker waits for its current character to be
 *  appended before generating another, so output is never collected into
 *  a complete string for a thread or an unbounded queue of pending updates.
 */

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class ZakThreeThreads extends Application {
    public static final int CHARACTERS_PER_THREAD = 10_000;

    /** Each thread chooses random characters from its own set. */
    public enum CharacterType {
        LETTERS("abcdefghijklmnopqrstuvwxyz"),
        DIGITS("0123456789"),
        SYMBOLS("!@#$%&*");

        private final String characters;

        CharacterType(String characters) {
            this.characters = characters;
        }

        public String getCharacters() {
            return characters;
        }
    }

    private final int[] displayedCounts = new int[CharacterType.values().length];
    private Thread[] workers = new Thread[0];
    private TextArea outputArea;
    private Label statusLabel;
    private volatile boolean closing;

    /** Returns one random character from the requested set. */
    public static char randomCharacter(CharacterType type) {
        Objects.requireNonNull(type, "Character type must not be null.");
        String characters = type.getCharacters();
        return characters.charAt(ThreadLocalRandom.current().nextInt(characters.length()));
    }

    /**
     * Creates a task that sends one character at a time to the supplied output.
     * A count of zero does nothing; interruption ends the task early.
     * The output callback runs on the worker and must support concurrent calls
     * when shared by multiple workers. Negative counts and null arguments
     * are rejected before the task starts.
     */
    public static Runnable createTask(CharacterType type, int count, Consumer<Character> output) {
        Objects.requireNonNull(type, "Character type must not be null.");
        Objects.requireNonNull(output, "Output must not be null.");
        if (count < 0) {
            throw new IllegalArgumentException("Character count must not be negative.");
        }
        return () -> {
            for (int i = 0; i < count && !Thread.currentThread().isInterrupted(); i++) {
                output.accept(randomCharacter(type));
            }
        };
    }

    /** Starts three named workers together and returns them so they can be stopped. */
    public static Thread[] startThreads(int count, Consumer<Character> output) {
        // Validate before starting any worker, including the zero-count case.
        Objects.requireNonNull(output, "Output must not be null.");
        if (count < 0) {
            throw new IllegalArgumentException("Character count must not be negative.");
        }
        CountDownLatch startSignal = new CountDownLatch(1);
        CharacterType[] types = CharacterType.values();
        Thread[] threads = new Thread[types.length];
        for (int i = 0; i < types.length; i++) {
            Runnable task = createTask(types[i], count, output);
            threads[i] = new Thread(() -> {
                try {
                    startSignal.await();
                    task.run();
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
            }, types[i].name());
            threads[i].setDaemon(true);
            threads[i].start();
        }
        startSignal.countDown();
        return threads;
    }

    @Override
    public void start(Stage stage) {
        outputArea = new TextArea();
        outputArea.setId("outputArea");
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.setFont(Font.font("Consolas", 14));

        Label instructions = new Label("Three threads - 10,000 letters, digits, and symbols each");
        statusLabel = new Label("Generating characters...");
        statusLabel.setId("statusLabel");

        BorderPane layout = new BorderPane(outputArea);
        layout.setTop(instructions);
        layout.setBottom(statusLabel);
        layout.setPadding(new Insets(12));
        BorderPane.setMargin(outputArea, new Insets(10, 0, 10, 0));
        stage.setTitle("Zak Three Threads - Module 8.2");
        stage.setScene(new Scene(layout, 900, 520));
        stage.show();
        workers = startThreads(CHARACTERS_PER_THREAD, this::displayCharacter);
    }

    /** Adds one generated character on the JavaFX thread, then releases its worker. */
    private void displayCharacter(char character) {
        if (closing) {
            return;
        }
        CountDownLatch displayed = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                if (!closing) {
                    outputArea.appendText(String.valueOf(character));
                    CharacterType type = Character.isLetter(character) ? CharacterType.LETTERS
                            : Character.isDigit(character) ? CharacterType.DIGITS : CharacterType.SYMBOLS;
                    displayedCounts[type.ordinal()]++;
                    int total = outputArea.getLength();
                    if (total % 100 == 0) {
                        String counts = String.format("Letters: %,d | Digits: %,d | Symbols: %,d | Total: %,d",
                                displayedCounts[0], displayedCounts[1], displayedCounts[2], total);
                        boolean complete = total == 3 * CHARACTERS_PER_THREAD;
                        statusLabel.setText((complete ? "Complete - " : "Generating - ") + counts);
                        if (complete) {
                            System.out.println("Complete - " + counts);
                        }
                    }
                }
            } finally {
                displayed.countDown();
            }
        });
        try {
            displayed.await();
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    /** Interrupt waiting workers when the application closes. */
    @Override
    public void stop() {
        closing = true;
        for (Thread worker : workers) {
            worker.interrupt();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
