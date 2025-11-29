package com.example.forkjoin;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImageFinderSimple {
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "tiff"
    );

    public static List<Path> findImagesSimple(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Шлях не є директорією: " + directory);
        }

        try (Stream<Path> stream = Files.walk(directory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(ImageFinderSimple::isImageFile)
                    .collect(Collectors.toList());
        }
    }

    private static boolean isImageFile(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            String extension = fileName.substring(dotIndex + 1);
            return IMAGE_EXTENSIONS.contains(extension);
        }
        return false;
    }

    public static void openFile(Path file) throws IOException {
        if (!Files.exists(file)) {
            throw new IllegalArgumentException("Файл не знайдено: " + file);
        }
        if (!Desktop.isDesktopSupported()) {
            throw new UnsupportedOperationException("Desktop API не підтримується");
        }
        Desktop.getDesktop().open(file.toFile());
    }

    public static void interactiveRun(ScannerWrapper sw) {
        System.out.println("=== Пошук зображень (Простий підхід) ===");
        String dirStr = sw.readLine("Введіть шлях до директорії:");
        Path directory = Paths.get(dirStr);

        if (!Files.isDirectory(directory)) {
            System.out.println("Помилка: вказаний шлях не є директорією або не існує.");
            return;
        }

        try {
            long startTime = System.nanoTime();
            List<Path> images = findImagesSimple(directory);
            long endTime = System.nanoTime();

            System.out.printf("Знайдено зображень: %d%n", images.size());
            System.out.printf("Час пошуку: %.3f ms%n", (endTime - startTime) / 1_000_000.0);

            if (!images.isEmpty()) {
                images.sort((p1, p2) -> {
                    try {
                        return Files.getLastModifiedTime(p2).compareTo(Files.getLastModifiedTime(p1));
                    } catch (IOException e) {
                        return 0;
                    }
                });

                Path lastImage = images.get(0);
                System.out.println("Останній файл: " + lastImage.toAbsolutePath());

                String response = sw.readLine("Відкрити файл? (y/n):").trim().toLowerCase();
                if (response.equals("y") || response.equals("так")) {
                    openFile(lastImage);
                    System.out.println("Файл відкрито!");
                }
            }
        } catch (Exception e) {
            System.out.println("Помилка під час пошуку: " + e.getMessage());
        }
    }

    public static class ScannerWrapper {
        private final Scanner scanner = new Scanner(System.in);

        public String readLine(String prompt) {
            if (prompt != null && !prompt.isEmpty()) {
                System.out.println(prompt);
            }
            return scanner.nextLine();
        }
    }
}