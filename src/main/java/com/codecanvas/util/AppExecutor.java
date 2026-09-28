package com.codecanvas.util;

import java.util.concurrent.*;

/**
 * Central concurrency provider for background operations (GitHub API, DB queries, async tasks).
 * Configured with a fixed thread pool of 4 worker threads and a clean shutdown hook.
 */
public class AppExecutor {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4, new ThreadFactory() {
        private int count = 1;
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "codecanvas-worker-" + count++);
            t.setDaemon(true);
            return t;
        }
    });

    public static ExecutorService getExecutor() {
        return EXECUTOR;
    }

    public static void execute(Runnable task) {
        EXECUTOR.execute(task);
    }

    public static <T> Future<T> submit(Callable<T> task) {
        return EXECUTOR.submit(task);
    }

    public static void shutdown() {
        EXECUTOR.shutdown();
        try {
            if (!EXECUTOR.awaitTermination(3, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
