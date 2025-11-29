package com.example.forkjoin;

import java.util.Random;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;
import java.util.Scanner;

public class FindInMatrixForkJoin {
    static class FindTask extends RecursiveTask<int[]> {
        private final int[][] matrix;
        private final int rowStart, rowEnd;
        private final int colStart, colEnd;
        private static final int ROW_THRESHOLD = 20;
        private volatile boolean cancelled = false;

        FindTask(int[][] matrix, int rs, int re, int cs, int ce) {
            this.matrix = matrix;
            this.rowStart = rs;
            this.rowEnd = re;
            this.colStart = cs;
            this.colEnd = ce;
        }

        public void cancel() {
            this.cancelled = true;
        }

        @Override
        protected int[] compute() {
            if (cancelled) return null;

            int rows = rowEnd - rowStart;
            if (rows <= ROW_THRESHOLD) {
                for (int i = rowStart; i < rowEnd && !cancelled; i++) {
                    for (int j = colStart; j < colEnd && !cancelled; j++) {
                        int val = matrix[i][j];
                        if (val == i + j) {
                            return new int[]{i, j, val};
                        }
                    }
                }
                return null;
            } else {
                int mid = rowStart + rows / 2;
                FindTask top = new FindTask(matrix, rowStart, mid, colStart, colEnd);
                FindTask bottom = new FindTask(matrix, mid, rowEnd, colStart, colEnd);

                bottom.fork();
                int[] topResult = top.compute();
                if (topResult != null) {
                    bottom.cancel();
                    return topResult;
                }
                int[] bottomResult = bottom.join();
                return bottomResult;
            }
        }
    }

    public static int[] findWithForkJoin(int[][] matrix) {
        ForkJoinPool pool = ForkJoinPool.commonPool();
        FindTask root = new FindTask(matrix, 0, matrix.length, 0, matrix[0].length);
        return pool.invoke(root);
    }

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

    public static void interactiveRun() {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== Work-stealing (ForkJoin) версія ===");
        int rows = readInt(sc, "Введіть кількість рядків:", 1, 10000);
        int cols = readInt(sc, "Введіть кількість стовпців:", 1, 10000);
        int min = readInt(sc, "Мінімальне значення елементу (ціле):", Integer.MIN_VALUE, Integer.MAX_VALUE);
        int max = readInt(sc, "Максимальне значення елементу (ціле):", min, Integer.MAX_VALUE);
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
        int[] res = findWithForkJoin(matrix);
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