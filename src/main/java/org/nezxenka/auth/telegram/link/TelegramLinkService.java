package org.nezxenka.auth.telegram.link;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import org.nezxenka.auth.session.PlayerSession;
import org.nezxenka.auth.session.SessionService;

@RequiredArgsConstructor
public final class TelegramLinkService {

    private final TelegramLinkRepository repository;
    private final SessionService sessions;
    private final Executor executor;

    public CompletableFuture<Boolean> isTelegramLinked(long telegramId) {
        return CompletableFuture.supplyAsync(() -> repository.isTelegramLinked(telegramId), executor);
    }

    public CompletableFuture<Boolean> link(UUID uuid, String username, long telegramId) {
        return CompletableFuture
            .supplyAsync(() -> repository.link(uuid, username, telegramId), executor)
            .thenApply(linked -> {
                if (linked) {
                    updateCachedAccount(uuid, telegramId);
                }
                return linked;
            });
    }

    public CompletableFuture<Void> unlink(UUID uuid) {
        return CompletableFuture
            .runAsync(() -> repository.unlink(uuid), executor)
            .thenRun(() -> updateCachedAccount(uuid, 0L));
    }

    private void updateCachedAccount(UUID uuid, long telegramId) {
        PlayerSession session = sessions.get(uuid);
        if (session != null) {
            session.updateAccount(account -> account.withTelegramId(telegramId));
        }
    }
}
