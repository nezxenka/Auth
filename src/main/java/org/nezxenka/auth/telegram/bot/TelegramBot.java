package org.nezxenka.auth.telegram.bot;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import lombok.Getter;
import org.nezxenka.auth.concurrent.NamedThreadFactory;
import org.nezxenka.auth.telegram.api.TelegramApiClient;
import org.nezxenka.auth.telegram.api.model.Update;

public final class TelegramBot {

    private static final int POLL_TIMEOUT_SECONDS = 30;
    private static final long RETRY_DELAY_MILLIS = 5000L;
    private static final long WORKER_SHUTDOWN_SECONDS = 5L;

    private final TelegramApiClient api;
    private final UpdateDispatcher dispatcher;
    private final Logger logger;
    private final ExecutorService poller;
    private final ExecutorService worker;
    private volatile boolean running;

    @Getter
    private volatile long offset;

    public TelegramBot(TelegramApiClient api, UpdateDispatcher dispatcher, Logger logger, long offset) {
        this.api = api;
        this.dispatcher = dispatcher;
        this.logger = logger;
        this.offset = offset;
        this.poller = Executors.newSingleThreadExecutor(new NamedThreadFactory("Auth-Telegram-Poller", logger));
        this.worker = Executors.newSingleThreadExecutor(new NamedThreadFactory("Auth-Telegram-Worker", logger));
    }

    public void start() {
        running = true;
        poller.execute(this::poll);
        logger.info("Telegram bot started!");
    }

    public void stop() {
        running = false;
        poller.shutdownNow();
        worker.shutdown();
        try {
            if (!worker.awaitTermination(WORKER_SHUTDOWN_SECONDS, TimeUnit.SECONDS)) {
                worker.shutdownNow();
            }
        } catch (InterruptedException exception) {
            worker.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void poll() {
        while (running) {
            try {
                for (Update update : api.getUpdates(offset, POLL_TIMEOUT_SECONDS)) {
                    offset = Math.max(offset, update.getUpdateId() + 1);
                    worker.execute(() -> dispatcher.dispatch(update));
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            } catch (IOException | RuntimeException exception) {
                if (!running) {
                    return;
                }
                logger.warning("Telegram polling error: " + exception.getMessage());
                if (!pause()) {
                    return;
                }
            }
        }
    }

    private boolean pause() {
        try {
            Thread.sleep(RETRY_DELAY_MILLIS);
            return true;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
