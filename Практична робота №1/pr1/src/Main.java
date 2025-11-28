import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Головний клас для запуску програми кав'ярні
 */
public class Main {
    public static void main(String[] args) {
        System.out.println(Color.PURPLE + "=== ЗАПУСК КАВ'ЯРНІ ===" + Color.RESET);

        // Створюємо кав'ярню з 2 слотами для кави та 5 місцями для клієнтів
        CoffeeShop shop = new CoffeeShop(2, 5);
        BlockingQueue<String> orderQueue = new ArrayBlockingQueue<>(20);

        // Запускаємо баристу
        Thread baristaThread = new Thread(new Barista(shop, orderQueue));
        baristaThread.start();

        // Список клієнтів
        String[] customerNames = {"Оля", "Саша", "Петро", "Марія", "Ігор", "Катя", "Юля", "Максим", "Анна", "Дмитро"};
        List<Thread> customerThreads = new ArrayList<>();
        Random random = new Random();

        System.out.println(Color.BLUE + "\nКлієнти починають приходити до кав'ярні..." + Color.RESET);

        // Запускаємо клієнтів з випадковими інтервалами
        for (String customerName : customerNames) {
            Thread customerThread = new Thread(new Customer(customerName, shop, orderQueue));
            customerThreads.add(customerThread);
            customerThread.start();

            // Випадкова затримка між клієнтами
            try {
                Thread.sleep(300 + random.nextInt(700));
            } catch (InterruptedException e) {
                System.out.println(Color.RED + "Перервано створення клієнтів" + Color.RESET);
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Чекаємо завершення всіх клієнтів
        System.out.println(Color.BLUE + "\nВсі клієнти прийшли. Чекаємо завершення..." + Color.RESET);

        for (Thread thread : customerThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println(Color.RED + "Перервано очікування клієнтів" + Color.RESET);
                Thread.currentThread().interrupt();
            }
        }

        // Додаємо затримку, щоб дати баристі час завершити поточні замовлення
        System.out.println(Color.BLUE + "\nУсі клієнти вийшли. Даємо баристі час завершити поточні замовлення..." + Color.RESET);
        try {
            Thread.sleep(3000); // Чекаємо 3 секунди на завершення поточних замовлень
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Перевіряємо, чи черга порожня
        if (!orderQueue.isEmpty()) {
            System.out.println(Color.YELLOW + "Залишилось замовлень в черзі: " + orderQueue.size() + Color.RESET);
        }

        // Закриваємо кав'ярню
        System.out.println(Color.BLUE + "\nЗакриваємо кав'ярню..." + Color.RESET);
        shop.close();

        // Чекаємо завершення баристи
        try {
            // Даємо додатковий час на завершення роботи
            baristaThread.join(5000);
            if (baristaThread.isAlive()) {
                System.out.println(Color.YELLOW + "Бариста все ще працює, але замовлень більше немає..." + Color.RESET);
                // Якщо бариста все ще працює, перериваємо його
                baristaThread.interrupt();
                baristaThread.join(1000);
            }
        } catch (InterruptedException e) {
            System.out.println(Color.RED + "Перервано очікування баристи" + Color.RESET);
            Thread.currentThread().interrupt();
        }

        // Фінальний статус
        System.out.println(Color.PURPLE + "\n=== КАВ'ЯРНЯ ЗАВЕРШИЛА РОБОТУ ===" + Color.RESET);
        System.out.println(Color.GREEN + "Статистика:" + Color.RESET);
        System.out.println(Color.GREEN + "- Обслуговано клієнтів: " + customerNames.length + Color.RESET);
        System.out.println(Color.GREEN + "- Залишилось необроблених замовлень: " + orderQueue.size() + Color.RESET);
        System.out.println(Color.PURPLE + "Дякуємо за відвідування!" + Color.RESET);
    }
}
