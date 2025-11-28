import java.util.concurrent.BlockingQueue;

/**
 * Клас баристи, що готує каву для клієнтів
 */
public class Barista implements Runnable {
    private final CoffeeShop shop;
    private final BlockingQueue<String> orderQueue;
    private volatile boolean shouldStop = false;

    public Barista(CoffeeShop shop, BlockingQueue<String> queue) {
        this.shop = shop;
        this.orderQueue = queue;
    }

    public void stopWork() {
        this.shouldStop = true;
    }

    @Override
    public void run() {
        System.out.println(Color.BLUE + "Бариста: Почав роботу. Очікую замовлення..." + Color.RESET);

        // Працюємо поки кав'ярня відчинена або є замовлення в черзі
        while ((shop.isOpen() || !orderQueue.isEmpty()) && !shouldStop) {
            try {
                // Використовуємо poll з таймаутом замість take()
                String customer = orderQueue.poll(1000, java.util.concurrent.TimeUnit.MILLISECONDS);

                if (customer == null) {
                    // Якщо немає замовлень протягом 1 секунди, перевіряємо чи продовжувати
                    if (!shop.isOpen() && orderQueue.isEmpty()) {
                        break;
                    }
                    continue;
                }

                System.out.println(Color.CYAN +
                        "Бариста: Отримав замовлення від " + customer +
                        " | Вільних слотів: " + (2 - shop.getBrewingSlots().availablePermits()) + "/2" +
                        Color.RESET);

                // Займаємо слот для приготування
                shop.getBrewingSlots().acquire();

                System.out.println(Color.CYAN +
                        "Бариста: Готую каву для " + customer +
                        " | Занято слотів: " + (2 - shop.getBrewingSlots().availablePermits()) + "/2" +
                        Color.RESET);

                // Імітація часу приготування кави
                Thread.sleep(2000 + (int)(Math.random() * 1000));

                System.out.println(Color.GREEN +
                        "✓ Бариста: Кава для " + customer + " готова!" +
                        Color.RESET);

                // Звільняємо слот
                shop.getBrewingSlots().release();

            } catch (InterruptedException e) {
                System.out.println(Color.YELLOW + "Бариста: Отримав сигнал завершення роботи" + Color.RESET);
                break;
            }
        }

        System.out.println(Color.PURPLE + "Бариста: Всі замовлення виконані. Завершую роботу." + Color.RESET);
    }
}
