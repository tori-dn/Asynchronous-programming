import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class Task1AsyncArrayModification {

    private static final int SIZE = 10;

    // Пул потоків для асинхронних операцій
    private static final ExecutorService EXECUTOR =
            Executors.newFixedThreadPool(4);

    public static void main(String[] args) {

        System.out.println("--- Завдання 1: Асинхронна робота з масивом ---\n");

        // Асинхронна генерація початкового масиву
        CompletableFuture<int[]> generate =
                timedSupply("Генерація масиву", () -> {
                    Random r = new Random();
                    int[] arr = new int[SIZE];
                    for (int i = 0; i < SIZE; i++) {
                        arr[i] = r.nextInt(100) + 1;
                    }
                    System.out.println("Початковий масив: " + Arrays.toString(arr));
                    return arr;
                });

        // Асинхронне додавання +10 до кожного елемента
        CompletableFuture<int[]> plusTen =
                generate.thenApplyAsync(arr ->
                        timed("Додавання +10", () -> {
                            int[] res = Arrays.copyOf(arr, arr.length);
                            for (int i = 0; i < res.length; i++) {
                                res[i] += 10;
                            }
                            System.out.println("Після +10: " + Arrays.toString(res));
                            return res;
                        }), EXECUTOR);

        // Асинхронне ділення на 2 з переходом до double[]
        CompletableFuture<double[]> divide =
                plusTen.thenApplyAsync(arr ->
                        timed("Ділення на 2", () -> {
                            double[] res = new double[arr.length];
                            for (int i = 0; i < arr.length; i++) {
                                res[i] = arr[i] / 2.0;
                            }
                            return res;
                        }), EXECUTOR);

        // Фінальний вивід результату
        divide.thenAcceptAsync(result ->
                        System.out.println("Результат ділення: " + Arrays.toString(result)),
                EXECUTOR).join();

        shutdown();
        System.out.println("\nГоловний потік: Завдання 1 завершено");
    }

    // Запуск асинхронної операції з вимірюванням часу
    private static <T> CompletableFuture<T> timedSupply(
            String title, Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(() ->
                timed(title, supplier), EXECUTOR);
    }

    // Вимірювання часу виконання операції
    private static <T> T timed(String title, Supplier<T> supplier) {
        long start = System.nanoTime();
        T result = supplier.get();
        long end = System.nanoTime();
        System.out.printf("[%s] Час виконання: %.3f мс%n",
                title, (end - start) / 1_000_000.0);
        return result;
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
