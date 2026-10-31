public class RunnableExample {

    public static void main(String[] args) {

        // Create a task that prints the thread name and a count.
        Runnable task = () -> {
            for (int i = 1; i <= 5; i++) {
                System.out.println(
                    Thread.currentThread().getName()
                    + " - Count: " + i
                );
            }
        };

        // Create two threads that use the same task.
        Thread thread1 = new Thread(task, "Thread 1");
        Thread thread2 = new Thread(task, "Thread 2");

        // Start both threads. Their output order can change between runs.
        thread1.start();
        thread2.start();
    }
}
