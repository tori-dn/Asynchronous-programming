import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class AsyncTasksDemo {

    public static void main(String[] args) {
        System.out.println("=== ПОЧАТОК ЛАБОРАТОРНОЇ РОБОТИ ===\n");

        runTask1_DependentActions();
        runTask2_AdvancedParallelFetching();

        System.out.println("\n=== ЗАВЕРШЕННЯ РОБОТИ ===");
    }

    // =========================================================
    // ЗАВДАННЯ 1. Залежні асинхронні завдання (thenCompose)
    // =========================================================
    private static void runTask1_DependentActions() {
        System.out.println("### 1. Демонстрація thenCompose (Ланцюжок залежних задач)");

        // Ланцюжок: Отримати ID -> (чекати) -> Отримати дані по ID
        CompletableFuture<String> result = getUserIdAsync()
                .thenCompose(userId -> processUserDataAsync(userId));

        // join() блокує main потік, поки не отримаємо результат
        System.out.println("✅ Фінальний результат Завдання 1: " + result.join());
        System.out.println("--------------------------------------------------\n");
    }

    private static CompletableFuture<Integer> getUserIdAsync() {
        return CompletableFuture.supplyAsync(() -> {
            log("Завдання 1.1: Пошук ID користувача в БД...");
            sleep(1000);
            return 12345;
        });
    }

    private static CompletableFuture<String> processUserDataAsync(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            log("Завдання 1.2: Генерація звіту для User ID: " + userId);
            sleep(800);
            return "Звіт DATA-" + userId + "-COMPLETED";
        });
    }

    // =========================================================
    // ЗАВДАННЯ 2. Паралельне отримання погоди (allOf, anyOf, thenCombine)
    // =========================================================
    private static void runTask2_AdvancedParallelFetching() {
        System.out.println("### 2. Паралельне порівняння погоди (Complex approach)");

        // 1. КИЇВ: Використовуємо anyOf()
        // Імітуємо два сервери. Беремо дані з того, який відповість першим.
        CompletableFuture<Weather> kyivFastest = CompletableFuture.anyOf(
                getWeatherFromSource("Київ (Server A)", 26, 55, 4, 3000), // Повільний
                getWeatherFromSource("Київ (Server B)", 26, 55, 4, 1000)  // Швидкий
        ).thenApply(result -> (Weather) result); // Приведення типу Object -> Weather


        // 2. ЛЬВІВ: Використовуємо thenCombine()
        // Отримуємо температуру і вологість окремими потоками та об'єднуємо їх.
        CompletableFuture<Integer> lvivTempFuture = CompletableFuture.supplyAsync(() -> {
            log("Отримання температури Львів (Датчик 1)...");
            sleep(1500);
            return 18;
        });

        CompletableFuture<Integer> lvivHumFuture = CompletableFuture.supplyAsync(() -> {
            log("Отримання вологості Львів (Датчик 2)...");
            sleep(1200);
            return 70;
        });

        // Об'єднуємо два незалежних значення в один об'єкт Weather
        CompletableFuture<Weather> lvivCombined = lvivTempFuture.thenCombine(lvivHumFuture, (temp, hum) -> {
            log("Об'єднання даних для Львова...");
            return new Weather("Львів", temp, hum, 8);
        });


        // 3. ОДЕСА: Просто supplyAsync (стандартний підхід)
        CompletableFuture<Weather> odesaStandard = CompletableFuture.supplyAsync(() -> {
            log("Отримання погоди Одеса (Стандартний API)...");
            sleep(2000);
            return new Weather("Одеса", 29, 60, 3);
        });


        // 4. ЗБІР УСІХ ДАНИХ: Використовуємо allOf()
        // Чекаємо завершення всіх трьох задач (Київ, Львів, Одеса)
        CompletableFuture.allOf(kyivFastest, lvivCombined, odesaStandard)
                .thenRun(() -> {
                    List<Weather> forecast = List.of(
                            kyivFastest.join(),
                            lvivCombined.join(),
                            odesaStandard.join()
                    );
                    analyzeWeather(forecast);
                })
                .join(); // Чекаємо виконання блоку thenRun
    }

    // Метод імітує отримання погоди з затримкою
    private static CompletableFuture<Weather> getWeatherFromSource(String sourceName, int t, int h, int w, int delay) {
        return CompletableFuture.supplyAsync(() -> {
            log("Запит до " + sourceName + "...");
            sleep(delay);
            log("Відповідь від " + sourceName + " отримана!");
            return new Weather(sourceName.split(" ")[0], t, h, w); // Повертаємо назву міста без "Server X"
        });
    }

    private static void analyzeWeather(List<Weather> list) {
        System.out.println("\n=== Фінальний аналіз погоди ===");
        for (Weather w : list) {
            System.out.printf("Місто: %-10s | T: %d°C | H: %d%% | Wind: %d м/с%n",
                    w.city, w.temperature, w.humidity, w.windSpeed);

            if (w.temperature >= 25 && w.windSpeed <= 5) {
                System.out.println(" ➡ Чудова погода для пляжу!");
            } else {
                System.out.println(" ➡ Краще вдягнутись тепліше.");
            }
            System.out.println();
        }
    }

    // Допоміжний метод для логування з іменем потоку
    private static void log(String message) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + message);
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Weather {
    String city;
    int temperature;
    int humidity;
    int windSpeed;

    public Weather(String city, int temperature, int humidity, int windSpeed) {
        this.city = city;
        this.temperature = temperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
    }
}
