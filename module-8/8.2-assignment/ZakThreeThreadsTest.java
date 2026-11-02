/*
 * Author:      Zak Nizam
 * Date:        November 1, 2026
 * Course:      CSD 420 - Advanced Java Programming
 * Assignment:  Module 8.2 Programming Assignment
 *
 * Purpose:
 *  Test character generation, task counts, invalid arguments, three-worker
 *  execution, and interruption. Then run the real application and check all
 *  30,000 displayed characters, one-character updates, and mixed output.
 *  Failed checks produce exit status 1; no -ea or test framework is needed.
 */

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ZakThreeThreadsTest extends Application {
    private static int exitCode;
    private int passed;
    private int updates;
    private boolean singleCharacterUpdates = true;
    private boolean fxThreadUpdates = true;
    private boolean finalCheckScheduled;
    private ZakThreeThreads application;
    private PauseTransition timeout;

    @Override
    public void start(Stage stage) {
        System.out.println("Zak Nizam | CSD 420 | Module 8.2 | November 1, 2026");
        System.out.println("Three-thread correctness tests");
        try {
            testGenerators();
            testTasks();
            testWorkerThreads();
            testInterruption();

            application = new ZakThreeThreads();
            application.start(stage);
            TextArea output = (TextArea) stage.getScene().lookup("#outputArea");
            Label status = (Label) stage.getScene().lookup("#statusLabel");
            check("Text area is read-only and wraps text", !output.isEditable() && output.isWrapText());
            System.out.println("Running the full 30,000-character display test...");

            // start() has not returned, so no queued character updates have run yet.
            output.textProperty().addListener((observable, oldText, newText) -> {
                updates++;
                singleCharacterUpdates &= newText.length() == oldText.length() + 1
                        && newText.startsWith(oldText);
                fxThreadUpdates &= Platform.isFxApplicationThread();
                if (newText.length() == 3 * ZakThreeThreads.CHARACTERS_PER_THREAD && !finalCheckScheduled) {
                    finalCheckScheduled = true;
                    Platform.runLater(() -> verifyDisplay(stage, output, status));
                }
            });
            timeout = new PauseTransition(Duration.minutes(3));
            timeout.setOnFinished(event -> fail(stage,
                    new AssertionError("Timed out before displaying all characters.")));
            timeout.play();
        } catch (Exception | AssertionError failure) {
            fail(stage, failure);
        }
    }

    private void testGenerators() {
        for (ZakThreeThreads.CharacterType type : ZakThreeThreads.CharacterType.values()) {
            Set<Character> observed = ConcurrentHashMap.newKeySet();
            for (int i = 0; i < 1_000; i++) {
                char value = ZakThreeThreads.randomCharacter(type);
                require(type.getCharacters().indexOf(value) >= 0, "Invalid character for " + type);
                observed.add(value);
            }
            check("Random " + type.name().toLowerCase() + " stay in their allowed set", observed.size() > 1);
        }
    }

    private void testTasks() {
        for (ZakThreeThreads.CharacterType type : ZakThreeThreads.CharacterType.values()) {
            StringBuilder text = new StringBuilder();
            ZakThreeThreads.createTask(type, 25, value -> text.append(value.charValue())).run();
            require(text.length() == 25, "Wrong task count for " + type);
            for (int i = 0; i < text.length(); i++) {
                require(type.getCharacters().indexOf(text.charAt(i)) >= 0, "Wrong task character type");
            }
        }
        check("Each task emits the exact requested count", true);

        AtomicInteger emitted = new AtomicInteger();
        ZakThreeThreads.createTask(ZakThreeThreads.CharacterType.LETTERS, 0,
                value -> emitted.incrementAndGet()).run();
        check("Zero-count task emits nothing", emitted.get() == 0);

        expectThrows(NullPointerException.class, () -> ZakThreeThreads.randomCharacter(null));
        expectThrows(NullPointerException.class, () -> ZakThreeThreads.createTask(null, 1, value -> {}));
        expectThrows(NullPointerException.class, () ->
                ZakThreeThreads.createTask(ZakThreeThreads.CharacterType.LETTERS, 1, null));
        expectThrows(NullPointerException.class, () -> ZakThreeThreads.startThreads(1, null));
        check("Null arguments are rejected", true);

        expectThrows(IllegalArgumentException.class, () ->
                ZakThreeThreads.createTask(ZakThreeThreads.CharacterType.LETTERS, -1, value -> {}));
        expectThrows(IllegalArgumentException.class, () -> ZakThreeThreads.startThreads(-1, value -> {}));
        check("Negative counts are rejected", true);
    }

    /** Uses a thread-safe output because all three workers call it directly. */
    private void testWorkerThreads() throws InterruptedException {
        StringBuffer text = new StringBuffer();
        Set<String> names = ConcurrentHashMap.newKeySet();
        Thread[] threads = ZakThreeThreads.startThreads(100, value -> {
            names.add(Thread.currentThread().getName());
            text.append(value.charValue());
        });
        joinAll(threads);
        require(threads.length == 3 && names.equals(Set.of("LETTERS", "DIGITS", "SYMBOLS")),
                "Expected three distinct named workers.");
        check("Three workers generate 100 characters each", correctCounts(text.toString(), 100));
        AtomicInteger emptyOutput = new AtomicInteger();
        Thread[] emptyWorkers = ZakThreeThreads.startThreads(0, value -> emptyOutput.incrementAndGet());
        joinAll(emptyWorkers);
        check("Zero-count workers finish without output", emptyOutput.get() == 0);
    }

    private void testInterruption() throws InterruptedException {
        AtomicInteger emitted = new AtomicInteger();
        Thread worker = new Thread(ZakThreeThreads.createTask(
                ZakThreeThreads.CharacterType.DIGITS, 100, value -> {
                    if (emitted.incrementAndGet() == 5) {
                        Thread.currentThread().interrupt();
                    }
                }));
        worker.start();
        joinAll(new Thread[]{worker});
        check("Interruption stops a task before its full count", emitted.get() == 5);
    }

    private void verifyDisplay(Stage stage, TextArea output, Label status) {
        try {
            timeout.stop();
            int count = ZakThreeThreads.CHARACTERS_PER_THREAD;
            check("At least 10,000 characters from each set", count >= 10_000 && correctCounts(output.getText(), count));
            check("Exactly 30,000 characters displayed", output.getLength() == 30_000);
            check("Every text update adds exactly one character", singleCharacterUpdates && updates == 30_000);
            check("All text updates use the JavaFX thread", fxThreadUpdates);
            int transitions = 0;
            String text = output.getText();
            for (int i = 1; i < text.length(); i++) {
                if (category(text.charAt(i)) != category(text.charAt(i - 1))) {
                    transitions++;
                }
            }
            check("Output is mixed rather than three complete blocks", transitions > 2);
            check("Status reports completion", status.getText().startsWith("Complete - "));
            application.stop();
            check("Application cleanup can be called more than once", stopAgain());
            System.out.println("All " + passed + " test cases passed.");
            stage.close();
            Platform.exit();
        } catch (Exception | AssertionError failure) {
            fail(stage, failure);
        }
    }

    private boolean stopAgain() {
        application.stop();
        return true;
    }

    private static boolean correctCounts(String text, int expected) {
        int[] counts = new int[3];
        for (int i = 0; i < text.length(); i++) {
            int type = category(text.charAt(i));
            if (type < 0) {
                return false;
            }
            counts[type]++;
        }
        return counts[0] == expected && counts[1] == expected && counts[2] == expected;
    }

    private static int category(char value) {
        for (ZakThreeThreads.CharacterType type : ZakThreeThreads.CharacterType.values()) {
            if (type.getCharacters().indexOf(value) >= 0) {
                return type.ordinal();
            }
        }
        return -1;
    }

    private static void joinAll(Thread[] threads) throws InterruptedException {
        for (Thread thread : threads) {
            thread.join(5_000);
            require(!thread.isAlive(), "Worker did not finish.");
        }
    }

    private static void expectThrows(Class<? extends RuntimeException> type, Runnable operation) {
        try {
            operation.run();
        } catch (RuntimeException failure) {
            require(type.isInstance(failure), "Unexpected exception type: " + failure);
            return;
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    private void check(String name, boolean condition) {
        require(condition, name);
        passed++;
        System.out.println("PASS: " + name);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private void fail(Stage stage, Throwable failure) {
        exitCode = 1;
        if (timeout != null) {
            timeout.stop();
        }
        if (application != null) {
            application.stop();
        }
        System.err.println("FAIL: " + failure.getMessage());
        failure.printStackTrace();
        stage.close();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }
}
