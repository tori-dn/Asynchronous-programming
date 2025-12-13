import java.util.*;
import java.util.concurrent.*;

public class Task2AsyncProductOfDifferences {

    private static final int SIZE = 20;
    private static final ExecutorService EXECUTOR =
            Executors.newFixedThreadPool(4);

    public static void main(String[] args) {

        System.out.println("--- Завдання 2: Добуток різниць ---\n");
        long totalStart = System.nanoTime();

        // Асинхронна генерація початкової послідовності
        CompletableFuture<double[]> generate =
                CompletableFuture.supplyAsync(() -> {
                    Random r = new Random();
                    double[] arr = new double[SIZE];
                    for (int i = 0; i < SIZE; i++) {
                        arr[i] = r.nextDouble() * 50;
                    }
                    System.out.println("Початкова послідовність: "
                            + Arrays.toString(arr));
                    return arr;
                }, EXECUTOR);

        // Асинхронне обчислення добутку різниць
        CompletableFuture<Double> calculate =
                generate.thenApplyAsync(arr -> {
                    long start = System.nanoTime();
                    double product = 1.0;

                    for (int i = 1; i < arr.length; i++) {
                        product *= (arr[i] - arr[i - 1]);
                    }

                    long end = System.nanoTime();
                    System.out.printf("[Обчислення] Час: %.3f мс%n",
                            (end - start) / 1_000_000.0);
                    return product;
                }, EXECUTOR);

        // Фінальний вивід результату
        calculate.thenAcceptAsync(result ->
                        System.out.printf("Результат обчислення: %.6f%n", result),
                EXECUTOR).join();

        long totalEnd = System.nanoTime();
        System.out.printf("%nЗагальний час: %.3f мс%n",
                (totalEnd - totalStart) / 1_000_000.0);

        shutdown();
        System.out.println("Головний потік: Завдання 2 завершено");
    }

    // Коректне завершення ExecutorService
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
