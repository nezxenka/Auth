package org.nezxenka.auth.account;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class AccountService {

    private final AccountRepository repository;
    private final Executor executor;

    public Optional<Account> loadBlocking(UUID uuid) {
        return repository.findByUuid(uuid);
    }

    public CompletableFuture<Optional<Account>> findByUuid(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> repository.findByUuid(uuid), executor);
    }

    public CompletableFuture<Optional<Account>> findByUsername(String username) {
        return CompletableFuture.supplyAsync(() -> repository.findByUsername(username), executor);
    }

    public CompletableFuture<Optional<Account>> findByTelegramId(long telegramId) {
        return CompletableFuture.supplyAsync(() -> repository.findByTelegramId(telegramId), executor);
    }

    public CompletableFuture<Void> create(Account account) {
        return CompletableFuture.runAsync(() -> repository.insert(account), executor);
    }

    public CompletableFuture<Void> updatePassword(UUID uuid, String passwordHash) {
        return CompletableFuture.runAsync(() -> repository.updatePassword(uuid, passwordHash), executor);
    }

    public CompletableFuture<Void> resetPassword(UUID uuid, String passwordHash) {
        return CompletableFuture.runAsync(() -> repository.resetPassword(uuid, passwordHash), executor);
    }

    public CompletableFuture<Void> recordLogin(
        UUID uuid,
        String username,
        long lastLogin,
        String sessionIp,
        long sessionExpiry
    ) {
        return CompletableFuture.runAsync(
            () -> repository.updateLogin(uuid, username, lastLogin, sessionIp, sessionExpiry),
            executor
        );
    }

    public CompletableFuture<Void> recordLastLogin(UUID uuid, String username, long lastLogin) {
        return CompletableFuture.runAsync(() -> repository.updateLastLogin(uuid, username, lastLogin), executor);
    }

    public CompletableFuture<Void> updateUsername(UUID uuid, String username) {
        return CompletableFuture.runAsync(() -> repository.updateUsername(uuid, username), executor);
    }
}
