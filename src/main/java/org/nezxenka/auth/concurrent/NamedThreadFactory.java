package org.nezxenka.auth.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class NamedThreadFactory implements ThreadFactory {

    private final String prefix;
    private final Logger logger;
    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable, prefix + "-" + counter.incrementAndGet());
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler((failed, error) ->
            logger.log(Level.SEVERE, "Uncaught exception in thread " + failed.getName(), error)
        );
        return thread;
    }
}
