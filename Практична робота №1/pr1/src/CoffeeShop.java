import java.util.concurrent.Semaphore;

/**
 * Клас кав'ярні, що керує станом та обмеженнями
 */
public class CoffeeShop {
    private volatile boolean open = true;
    private final Semaphore brewingSlots;     // 2 слоти для приготування кави
    private final Semaphore customersInside;  // 5 місць для клієнтів

    public CoffeeShop(int brewingCount, int maxCustomers) {
        this.brewingSlots = new Semaphore(brewingCount);
        this.customersInside = new Semaphore(maxCustomers);
    }

    /**
     * Перевіряє, чи відчинена кав'ярня
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Спроба увійти до кав'ярні з перевіркою стану
     */
    public boolean tryEnter() {
        if (!open) {
            return false; // Кав'ярня зачинена
        }
        return customersInside.tryAcquire();
    }

    /**
     * Клієнт виходить з кав'ярні
     */
    public void leave() {
        customersInside.release();
    }

    /**
     * Закрити кав'ярню
     */
    public void close() {
        this.open = false;
        System.out.println(Color.RED + "\n=== КАВ'ЯРНЯ ЗАЧИНЕНА ===" + Color.RESET);
        System.out.println(Color.RED + "Нові клієнти не можуть заходити" + Color.RESET);
    }

    public Semaphore getBrewingSlots() {
        return brewingSlots;
    }

    /**
     * Отримати поточну кількість вільних місць
     */
    public int getAvailableSpots() {
        return customersInside.availablePermits();
    }
}
