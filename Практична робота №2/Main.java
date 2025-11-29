import java.util.*;
import java.util.concurrent.*;

class AsyncArrayMultiplier {

    private static final int MIN_SIZE = 40;
    private static final int MAX_SIZE = 60;
    private static final int PARTITION_SIZE = 10; // просте та зрозуміле розбиття

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("=== АСИНХРОННА ОБРОБКА МАСИВІВ ===");
        System.out.print("Мінімальне значення діапазону: ");
        int min = scanner.nextInt();

        System.out.print("Максимальне значення діапазону: ");
        int max = scanner.nextInt();

        scanner.close();

        long start = System.currentTimeMillis();

        // --- Генерація масиву ---
        int size = ThreadLocalRandom.current().nextInt(MIN_SIZE, MAX_SIZE + 1);
        int[] numbers = ThreadLocalRandom.current().ints(size, min, max + 1).toArray();

        System.out.println("\nРозмір масиву: " + size);
        System.out.println("Масив: " + Arrays.toString(numbers));

        // --- Розбиття на частини ---
        List<int[]> parts = partitionArray(numbers, PARTITION_SIZE);

        System.out.println("\nЧастини масиву:");
        for (int i = 0; i < parts.size(); i++) {
            System.out.println("Частина " + i + ": " + Arrays.toString(parts.get(i)));
        }

        // --- Executor ---
        ExecutorService executor = Executors.newFixedThreadPool(parts.size());
        List<Future<int[]>> futures = new ArrayList<>();

        // --- Запуск задач ---
        System.out.println("\n=== ЗАПУСК ЗАВДАНЬ ===");

        for (int i = 0; i < parts.size(); i++) {
            futures.add(executor.submit(new PairwiseMultiplierCallable(parts.get(i), i)));
        }

        // --- Збір результатів у правильному порядку ---
        int[][] partialResults = new int[parts.size()][];
        CopyOnWriteArraySet<Integer> uniqueResults = new CopyOnWriteArraySet<>();

        System.out.println("\n=== ОЧІКУВАННЯ РЕЗУЛЬТАТІВ ===");

        for (int i = 0; i < futures.size(); i++) {
            Future<int[]> future = futures.get(i);

            if (future.isCancelled()) {
                System.out.println("Завдання " + i + " скасоване!");
                continue;
            }

            while (!future.isDone()) {
                System.out.println("Завдання " + i + " ще виконується...");
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ignored) {}
            }

            try {
                partialResults[i] = future.get(); // <-- ПРАВИЛЬНИЙ ПОРЯДОК
                System.out.println("Завдання " + i + " завершено.");

                // додаємо унікальні значення для вимоги варіанту
                for (int v : partialResults[i]) {
                    uniqueResults.add(v);
                }

            } catch (Exception e) {
                System.out.println("Помилка завдання " + i + ": " + e.getMessage());
            }
        }

        executor.shutdown();

        // --- Об'єднання у фінальний масив ---
        int totalLength = Arrays.stream(partialResults)
                .filter(Objects::nonNull)
                .mapToInt(arr -> arr.length)
                .sum();

        int[] finalArray = new int[totalLength];
        int index = 0;

        for (int[] arr : partialResults) {
            if (arr == null) continue;
            for (int v : arr) finalArray[index++] = v;
        }

        long end = System.currentTimeMillis();

        // --- Вивід результатів ---
        System.out.println("\n=== ФІНАЛЬНИЙ РЕЗУЛЬТАТ ===");
        System.out.println("Попарні добутки у правильному порядку:");
        System.out.println(Arrays.toString(finalArray));

        System.out.println("\nУнікальні значення (CopyOnWriteArraySet):");
        System.out.println(uniqueResults);

        System.out.println("\n=== СТАТИСТИКА ===");
        System.out.println("Кількість потоків: " + parts.size());
        System.out.println("Час виконання: " + (end - start) + " мс");
    }

    // ------------------- Допоміжні методи -------------------

    private static List<int[]> partitionArray(int[] array, int size) {
        List<int[]> parts = new ArrayList<>();
        for (int i = 0; i < array.length; i += size) {
            int end = Math.min(array.length, i + size);
            parts.add(Arrays.copyOfRange(array, i, end));
        }
        return parts;
    }

    // ------------------- Callable -------------------

    static class PairwiseMultiplierCallable implements Callable<int[]> {

        private final int[] arr;
        private final int id;

        public PairwiseMultiplierCallable(int[] arr, int id) {
            this.arr = arr;
            this.id = id;
        }

        @Override
        public int[] call() throws Exception {
            System.out.println("Потік " + Thread.currentThread().getName() +
                    " обробляє частину " + id);

            List<Integer> results = new ArrayList<>();

            for (int i = 0; i < arr.length - 1; i += 2) {
                results.add(arr[i] * arr[i + 1]);
            }

            if (arr.length % 2 != 0) {
                results.add(arr[arr.length - 1]);
            }

            return results.stream().mapToInt(Integer::intValue).toArray();
        }
    }
}
