import java.util.concurrent.*;

public class CompletableFutureDemo {

    // Пул потоків для виконання асинхронних задач
    private static final ExecutorService EXECUTOR =
            Executors.newFixedThreadPool(4);

    public static void main(String[] args) {

        System.out.println("--- Демонстрація методів CompletableFuture ---\n");

        // runAsync — асинхронне виконання Runnable (без результату)
        CompletableFuture<Void> future = CompletableFuture
                .runAsync(() ->
                        log("runAsync: виконання Runnable без результату"))

                // supplyAsync — асинхронно повертає значення
                .thenCompose(v -> CompletableFuture.supplyAsync(() -> {
                    log("supplyAsync: повертаємо число 100");
                    return 100;
                }, EXECUTOR))

                // thenApplyAsync — обробка (трансформація) результату
                .thenApplyAsync(value -> {
                    log("thenApplyAsync: ділення " + value + " / 3");
                    return value / 3.0;
                }, EXECUTOR)

                // thenAcceptAsync — приймає результат, але нічого не повертає
                .thenAcceptAsync(result ->
                                log(String.format("thenAcceptAsync: результат = %.2f", result)),
                        EXECUTOR)

                // thenRunAsync — виконується після завершення всіх етапів
                .thenRunAsync(() ->
                                log("thenRunAsync: всі попередні етапи завершені"),
                        EXECUTOR);

        // Очікуємо завершення всього ланцюжка
        future.join();

        shutdown();
        System.out.println("\nГоловний потік: демонстрацію завершено");
    }

    // Допоміжний метод для виводу потоку виконання
    private static void log(String message) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + message);
    }

    // Коректне завершення пулу потоків
    private static void shutdown() {
        EXECUTOR.shutdown();
        try {
            if (!EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
