package com.example.forkjoin;

import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class FindInMatrixDeal {

    public static int[][] generateMatrix(int rows, int cols, int min, int max, long seed) {
        Random rnd = (seed == Long.MIN_VALUE) ? new Random() : new Random(seed);
        int[][] m = new int[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                m[i][j] = rnd.nextInt(max - min + 1) + min;
        return m;
    }

    public static void printMatrix(int[][] m) {
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                System.out.print(m[i][j] + "\t");
            }
            System.out.println();
        }
    }

    public static int[] findWithExecutor(int[][] matrix, int threadCount) throws InterruptedException {
        int rows = matrix.length;
        int chunk = (rows + threadCount - 1) / threadCount;
        ExecutorService ex = Executors.newFixedThreadPool(threadCount);
        AtomicBoolean found = new AtomicBoolean(false);
        AtomicReference<int[]> result = new AtomicReference<>(null);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            final int rs = t * chunk;
            final int re = Math.min(rows, rs + chunk);
            ex.submit(() -> {
                try {
                    outerLoop:
                    for (int i = rs; i < re && !found.get(); i++) {
                        for (int j = 0; j < matrix[0].length && !found.get(); j++) {
                            int v = matrix[i][j];
                            if (v == i + j) {
                                if (found.compareAndSet(false, true)) {
                                    result.set(new int[]{i, j, v});
                                }
                                break outerLoop;
                            }
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        ex.shutdownNow();
        return result.get();
    }

    public static void interactiveRun() {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== Work-dealing (ExecutorService) версія ===");
        int rows = readInt(sc, "Введіть кількість рядків:", 1, 10000);
        int cols = readInt(sc, "Введіть кількість стовпців:", 1, 10000);
        int min = readInt(sc, "Мінімальне значення елементу (ціле):", Integer.MIN_VALUE, Integer.MAX_VALUE);
        int max = readInt(sc, "Максимальне значення елементу (ціле):", min, Integer.MAX_VALUE);
        int threadCount = readInt(sc, "Кількість потоків для пулу:", 1, Runtime.getRuntime().availableProcessors() * 2);
        sc.nextLine();
        System.out.println("Введіть seed (або залиште пустим для випадкового):");
        String seedLine = sc.nextLine().trim();
        long seed = seedLine.isEmpty() ? Long.MIN_VALUE : Long.parseLong(seedLine);

        int[][] matrix = generateMatrix(rows, cols, min, max, seed);
        System.out.println("Згенерований масив:");
        if (rows <= 20 && cols <= 20) {
            printMatrix(matrix);
        } else {
            System.out.println("(Масив занадто великий для відображення)");
        }

        long t0 = System.nanoTime();
        int[] res = null;
        try {
            res = findWithExecutor(matrix, threadCount);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Пошук перервано.");
        }
        long t1 = System.nanoTime();

        if (res != null) {
            System.out.printf("Знайдено елемент: matrix[%d][%d] = %d%n", res[0], res[1], res[2]);
        } else {
            System.out.println("Елементу, що дорівнює сумі індексів, не знайдено.");
        }
        System.out.printf("Час (ms): %.3f%n", (t1 - t0) / 1_000_000.0);
    }

    private static int readInt(Scanner sc, String prompt, int min, int max) {
        while (true) {
            System.out.println(prompt);
            String line = sc.nextLine().trim();
            try {
                int v = Integer.parseInt(line);
                if (v < min || v > max) {
                    System.out.printf("Значення має бути в діапазоні [%d, %d]%n", min, max);
                } else return v;
            } catch (Exception e) {
                System.out.println("Невірний ввід. Спробуйте ще раз.");
            }
        }
    }
}