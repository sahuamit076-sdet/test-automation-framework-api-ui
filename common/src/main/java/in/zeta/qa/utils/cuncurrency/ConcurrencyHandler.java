package in.zeta.qa.utils.cuncurrency;

import lombok.SneakyThrows;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class ConcurrencyHandler<T> {
    private static final ConcurrentHashMap<String, Semaphore> semaphoreMap = new ConcurrentHashMap<>();
    private final ThreadLocal<T> contextLocal = new ThreadLocal<>();
    // Method to set the context
    public void setContext(T context) {
        contextLocal.set(context);
    }
    // Method to get the context
    public T getContext() {
        return contextLocal.get();
    }

    // Acquire semaphore for a unique key (like card ID)
    @SneakyThrows
    public Semaphore acquireSemaphore(String key) {
        semaphoreMap.computeIfAbsent(key, k -> new Semaphore(1));
        Semaphore semaphore = semaphoreMap.get(key);
        semaphore.acquire();
        return semaphore;
    }

    public void releaseSemaphore(String key) {
        Semaphore semaphore = semaphoreMap.get(key);
        if (semaphore != null) {
            semaphore.release();
        }
    }
}
