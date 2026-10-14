import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EnumComparatorExample {

    enum Priority {
        HIGH(1),
        MEDIUM(2),
        LOW(3);

        private final int rank;

        Priority(int rank) {
            this.rank = rank;
        }

        public int getRank() {
            return rank;
        }
    }

    static class Task {
        private String name;
        private Priority priority;

        public Task(String name, Priority priority) {
            this.name = name;
            this.priority = priority;
        }

        public Priority getPriority() {
            return priority;
        }

        @Override
        public String toString() {
            return name + " - " + priority;
        }
    }

    public static void main(String[] args) {
        List<Task> tasks = new ArrayList<>();

        tasks.add(new Task("Finish homework", Priority.HIGH));
        tasks.add(new Task("Clean desk", Priority.LOW));
        tasks.add(new Task("Answer email", Priority.MEDIUM));

        // Sort tasks using the numeric rank assigned to each enum value.
        tasks.sort(
            Comparator.comparingInt(
                task -> task.getPriority().getRank()
            )
        );

        for (Task task : tasks) {
            System.out.println(task);
        }
    }
}
