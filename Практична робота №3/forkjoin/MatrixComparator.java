package com.example.forkjoin;

public class MatrixComparator {

    public static void compareApproaches(int[][] matrix) {
        System.out.println("\n=== Порівняння підходів ===");

        // Work-stealing
        long start = System.nanoTime();
        int[] result1 = FindInMatrixForkJoin.findWithForkJoin(matrix);
        long time1 = System.nanoTime() - start;

        // Work-dealing
        start = System.nanoTime();
        int[] result2 = null;
        try {
            result2 = FindInMatrixDeal.findWithExecutor(matrix,
                    Runtime.getRuntime().availableProcessors());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long time2 = System.nanoTime() - start;

        System.out.printf("Work-stealing: %.3f ms%n", time1 / 1_000_000.0);
        System.out.printf("Work-dealing:  %.3f ms%n", time2 / 1_000_000.0);

        double difference = Math.abs(time1 - time2) / 1_000_000.0;
        String faster = time1 < time2 ? "Work-stealing" : "Work-dealing";
        System.out.printf("Різниця: %.3f ms (%s швидший)%n", difference, faster);

        // Перевірка коректності результатів
        boolean resultsMatch = (result1 == null && result2 == null) ||
                (result1 != null && result2 != null &&
                        result1[0] == result2[0] && result1[1] == result2[1]);
        System.out.println("Результати збігаються: " + resultsMatch);
    }

    public static void interactiveComparison() {
        java.util.Scanner sc = new java.util.Scanner(System.in);
        System.out.println("=== Порівняння Work-stealing vs Work-dealing ===");

        int rows = 1000;
        int cols = 1000;
        int min = 0;
        int max = 2000;

        System.out.println("Генерується тестова матриця 1000x1000...");
        int[][] matrix = FindInMatrixForkJoin.generateMatrix(rows, cols, min, max, System.currentTimeMillis());

        // Запускаємо порівняння 5 разів для усереднення
        for (int i = 1; i <= 5; i++) {
            System.out.println("\n--- Запуск " + i + " ---");
            compareApproaches(matrix);
        }
    }
}