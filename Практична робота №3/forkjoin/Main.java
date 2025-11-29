package com.example.forkjoin;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ImageFinderSimple.ScannerWrapper sw = new ImageFinderSimple.ScannerWrapper();

        while (true) {
            System.out.println("\n=== Головне меню ===");
            System.out.println("1. Завдання 1: Work-stealing (ForkJoin) - пошук в матриці");
            System.out.println("2. Завдання 1: Work-dealing (ExecutorService) - пошук в матриці");
            System.out.println("3. Завдання 1: Порівняння підходів");
            System.out.println("4. Завдання 2: Пошук зображень (ForkJoin Work-stealing)");
            System.out.println("5. Завдання 2: Пошук зображень (Простий підхід)");
            System.out.println("6. Вийти");
            System.out.print("Оберіть опцію: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    FindInMatrixForkJoin.interactiveRun();
                    break;
                case "2":
                    FindInMatrixDeal.interactiveRun();
                    break;
                case "3":
                    MatrixComparator.interactiveComparison();
                    break;
                case "4":
                    ImageFinderForkJoin.interactiveRun(sw);
                    break;
                case "5":
                    ImageFinderSimple.interactiveRun(sw);
                    break;
                case "6":
                    System.out.println("До побачення!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Невірний вибір. Спробуйте ще раз.");
            }
        }
    }
}