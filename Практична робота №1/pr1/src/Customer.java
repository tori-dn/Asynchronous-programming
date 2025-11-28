import java.util.Random;
import java.util.concurrent.BlockingQueue;

/**
 * Клас клієнта, що відвідує кав'ярню
 */
public class Customer implements Runnable {
    private final String name;
    private final CoffeeShop shop;
    private final BlockingQueue<String> orderQueue;
    private final Random random = new Random();

    public Customer(String name, CoffeeShop shop, BlockingQueue<String> queue) {
        this.name = name;
        this.shop = shop;
        this.orderQueue = queue;
    }

    @Override
    public void run() {
        System.out.println(Color.YELLOW + name + ": Підійшов(ла) до кав'ярні" + Color.RESET);

        // Перевірка чи відчинена кав'ярня
        if (!shop.isOpen()) {
            System.out.println(Color.RED + "✗ " + name + ": Кав'ярня зачинена! Не можу увійти." + Color.RESET);
            return;
        }

        try {
            // Спроба увійти до кав'ярні
            if (!shop.tryEnter()) {
                System.out.println(Color.RED + "✗ " + name + ": Забагато людей всередині (" +
                        shop.getAvailableSpots() + " вільних місць). Чекатиму..." + Color.RESET);
                return;
            }

            System.out.println(Color.GREEN + "✓ " + name + ": Зайшов(ла) у кав'ярню. Вільних місць: " +
                    shop.getAvailableSpots() + Color.RESET);

            // Час на вибір кави
            Thread.sleep(500 + random.nextInt(800));
            System.out.println(Color.CYAN + name + ": Обрав(ла) каву і роблю замовлення" + Color.RESET);

            // Додаємо замовлення до черги
            orderQueue.put(name);
            System.out.println(Color.CYAN + "✓ " + name + ": Замовлення прийнято. Чекаю на каву..." + Color.RESET);

            // Час очікування на каву
            Thread.sleep(1000 + random.nextInt(1000));

            System.out.println(Color.YELLOW + name + ": Отримав(ла) каву і виходжу" + Color.RESET);

            // Звільняємо місце в кав'ярні
            shop.leave();

        } catch (InterruptedException e) {
            System.out.println(Color.RED + name + ": Перервано під час відвідування кав'ярні" + Color.RESET);
            Thread.currentThread().interrupt();
        }
    }

    public String getName() {
        return name;
    }
}
