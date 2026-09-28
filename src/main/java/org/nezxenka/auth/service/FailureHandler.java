package org.nezxenka.auth.service;

import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.nezxenka.auth.concurrent.Futures;
import org.nezxenka.auth.concurrent.MainThreadExecutor;
import org.nezxenka.auth.message.MessageKey;
import org.nezxenka.auth.message.MessageService;

@RequiredArgsConstructor
public final class FailureHandler {

    private final Logger logger;
    private final MessageService messages;
    private final MainThreadExecutor mainThread;

    public void handle(CommandSender sender, String action, Throwable error) {
        logger.log(Level.SEVERE, action, Futures.unwrap(error));
        mainThread.execute(() -> messages.send(sender, MessageKey.DATABASE_ERROR));
    }
}
