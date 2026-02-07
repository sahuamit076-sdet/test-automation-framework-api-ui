package in.zeta.qa.utils.cuncurrency;


import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class ExecutorPipeline {

    private static final ExecutorService DEFAULT_EXECUTOR =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private final List<Task<?>> tasks = new ArrayList<>();
    private boolean parallel;

    // Private constructor
    private ExecutorPipeline(boolean parallel) {
        this.parallel = parallel;
    }

    // Fluent entry points
    public static ExecutorPipeline getInstance() {
        return new ExecutorPipeline(true);
    }



    public ExecutorPipeline parallel() {
        this.parallel = true; // just switch mode, don’t create a new object
        return this;
    }

    public ExecutorPipeline sequential() {
        this.parallel = false; // just switch mode, don’t create a new object
        return this;
    }

    // Add a task
    public <T> ExecutorPipeline task(String name, Supplier<T> supplier) {
        Task<T> t = new Task<>(name, supplier);
        t.parallel = this.parallel;
        if (supplier != null) tasks.add(t);
        return this;
    }

    // New Runnable-based task for void methods
    public ExecutorPipeline task(String name, Runnable runnable) {
        if (runnable != null) {
            Task<Void> t = new Task<>(name, () -> {
                runnable.run();
                return null;
            });
            t.parallel = this.parallel;
            tasks.add(t);
        }
        return this;
    }


    // Execute tasks
    public Map<String, Object> execute() {
        Map<String, Object> results = new LinkedHashMap<>();
        int i = 0;
        while (i < tasks.size()) {
            Task<?> t = tasks.get(i);
            boolean validTask = t.name != null && t.supplier != null;
            if (!validTask) {
                i++;
                continue; // skip invalid tasks
            }
            if (Boolean.TRUE.equals(t.parallel)) {
                i = executeParallelBatch(i, results); // returns the index of last executed task
            } else {
                executeSequentialTask(t, results);
                i++;
            }
        }
        tasks.clear();
        return results;
    }

    private void executeSequentialTask(Task<?> task, Map<String, Object> results) {
        if (task.name != null && task.supplier != null) {
            results.put(task.name, task.supplier.get());
        }
    }

    private int executeParallelBatch(int startIndex, Map<String, Object> results) {
        List<Task<?>> parallelBatch = new ArrayList<>();
        int i = startIndex;

        // collect consecutive parallel tasks
        while (i < tasks.size() && Boolean.TRUE.equals(tasks.get(i).parallel)) {
            Task<?> t = tasks.get(i);
            if (t.name != null && t.supplier != null) {
                parallelBatch.add(t);
            }
            i++;
        }

        // execute all tasks in parallel
        List<CompletableFuture<Map.Entry<String, Object>>> futures = parallelBatch.stream()
                .map(t -> CompletableFuture.supplyAsync(() -> {
                    Object value;
                    try {
                        value = t.supplier.get(); // might be null
                    } catch (Exception e) {
                        value = e; // store the exception instead of breaking
                    }
                    // Use AbstractMap.SimpleEntry to allow null values
                    return (Map.Entry<String, Object>) new AbstractMap.SimpleEntry<>(t.name, value);
                }, DEFAULT_EXECUTOR))
                .toList();
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            // Prevent circular cause chain
            Set<Throwable> seen = new HashSet<>();
            while (cause instanceof CompletionException && cause.getCause() != null && !seen.contains(cause)) {
                seen.add(cause);
                cause = cause.getCause();
            }
            if (cause instanceof AssertionError) {
                throw (AssertionError) cause;
            } else {
                throw new RuntimeException(cause);
            }
        }

        futures.stream().map(CompletableFuture::join)
                .forEach(entry -> results.put(entry.getKey(), entry.getValue()));

        return i; // return the index after the last parallel task
    }


    // Internal task wrapper
    private static class Task<T> {
        private final String name;
        private final Supplier<T> supplier;
        private Boolean parallel;

        public Task(String name, Supplier<T> supplier) {
            this.name = name;
            this.supplier = supplier;
        }
    }


}
