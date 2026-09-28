package org.nezxenka.auth.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import lombok.Getter;

@Getter
public final class AsyncExecutors {

    private static final long SHUTDOWN_TIMEOUT_SECONDS = 10L;

    private final ExecutorService database;
    private final ExecutorService crypto;

    public AsyncExecutors(int databaseThreads, Logger logger) {
        this.database = Executors.newFixedThreadPool(
            Math.max(1, databaseThreads),
            new NamedThreadFactory("Auth-Database", logger)
        );
        this.crypto = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
            new NamedThreadFactory("Auth-Crypto", logger)
        );
    }

    public void shutdown() {
        shutdown(crypto);
        shutdown(database);
    }

    private static void shutdown(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException exception) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
