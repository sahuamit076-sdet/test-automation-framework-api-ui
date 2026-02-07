package in.zeta.qa.utils.cuncurrency;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class SingletonFactory {

    private SingletonFactory() {
        throw new AssertionError("No instances");
    }

    private static final Cache<Class<?>, CompletableFuture<Object>> INSTANCE_CACHE =
            Caffeine.newBuilder().expireAfterAccess(15, TimeUnit.MINUTES)
                    .maximumSize(100)
                    .build();
    /**
     * Returns the singleton instance of the given class.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getInstance(Class<T> clazz) {
        log.info("Requesting singleton instance for class: {} | Current pool size: {}", clazz.getName(), INSTANCE_CACHE.estimatedSize());
        Objects.requireNonNull(clazz, "Class must not be null");
        CompletableFuture<Object> future = INSTANCE_CACHE.get(clazz, key ->
                CompletableFuture.supplyAsync(() -> createInstance(key))
        );

        try {
            return (T) future.get();
        } catch (Exception e) {
            INSTANCE_CACHE.invalidate(clazz);
            log.error("[SingletonFactory] Failed to create singleton for class: {} | Thread: {} | Error: {}", clazz.getName(), Thread.currentThread().getName(), e.getMessage(), e);
            throw new RuntimeException("Cannot create singleton for class: " + clazz.getName(), e);
        }
    }

    /**
     * Creates an instance using the no-arg constructor.
     */
    private static <T> T createInstance(Class<T> clazz) {
        log.debug("[SingletonFactory] Creating instance for class: {} | Thread: {}", clazz.getName(), Thread.currentThread().getName());
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            int mods = constructor.getModifiers();
            if (!(Modifier.isPrivate(mods) || Modifier.isProtected(mods))) {
                log.error("[SingletonFactory] Class {} must have a private or protected no-arg constructor", clazz.getName());
                throw new IllegalStateException(
                        "Class " + clazz.getName() + " must have a private or protected no-arg constructor"
                );
            }
            constructor.setAccessible(true);
            T instance = constructor.newInstance();
            log.info("[SingletonFactory] Instance created for class: {} | Thread: {}", clazz.getName(), Thread.currentThread().getName());
            return instance;
        } catch (Exception e) {
            log.error("[SingletonFactory] Failed to create instance for class: {} | Thread: {} | Error: {}", clazz.getName(), Thread.currentThread().getName(), e.getMessage(), e);
            throw new RuntimeException("Failed to create instance for class: " + clazz.getName(), e);
        }
    }
}
