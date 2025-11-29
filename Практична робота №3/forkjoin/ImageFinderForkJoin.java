package com.example.forkjoin;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

public class ImageFinderForkJoin {
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "tiff"
    );

    static class ImageSearchTask extends RecursiveTask<List<Path>> {
        private final Path directory;
        private static final int DIRECTORY_THRESHOLD = 10;

        public ImageSearchTask(Path directory) {
            this.directory = directory;
        }

        @Override
        protected List<Path> compute() {
            List<Path> imageFiles = new ArrayList<>();
            List<ImageSearchTask> subTasks = new ArrayList<>();

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
                for (Path entry : stream) {
                    if (Files.isDirectory(entry)) {
                        ImageSearchTask subTask = new ImageSearchTask(entry);
                        subTask.fork();
                        subTasks.add(subTask);
                    } else if (isImageFile(entry)) {
                        imageFiles.add(entry);
                    }
                }
            } catch (IOException e) {
                System.err.println("Помилка доступу до директорії: " + directory + " - " + e.getMessage());
                return imageFiles;
            }

            for (ImageSearchTask task : subTasks) {
                imageFiles.addAll(task.join());
            }

            return imageFiles;
        }

        private boolean isImageFile(Path file) {
            String fileName = file.getFileName().toString().toLowerCase();
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                String extension = fileName.substring(dotIndex + 1);
                return IMAGE_EXTENSIONS.contains(extension);
            }
            return false;
        }
    }

    public static List<Path> findImagesWithForkJoin(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Шлях не є директорією: " + directory);
        }

        ForkJoinPool pool = ForkJoinPool.commonPool();
        ImageSearchTask task = new ImageSearchTask(directory);
        return pool.invoke(task);
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

    public static void interactiveRun(ImageFinderSimple.ScannerWrapper sw) {
        System.out.println("=== Пошук зображень (ForkJoin Work-stealing) ===");
        String dirStr = sw.readLine("Введіть шлях до директорії:");
        Path directory = Paths.get(dirStr);

        if (!Files.isDirectory(directory)) {
            System.out.println("Помилка: вказаний шлях не є директорією або не існує.");
            return;
        }

        try {
            long startTime = System.nanoTime();
            List<Path> images = findImagesWithForkJoin(directory);
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
}