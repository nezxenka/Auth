package org.nezxenka.auth.service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.nezxenka.auth.account.Account;
import org.nezxenka.auth.account.AccountService;
import org.nezxenka.auth.concurrent.AsyncExecutors;
import org.nezxenka.auth.concurrent.MainThreadExecutor;
import org.nezxenka.auth.message.MessageKey;
import org.nezxenka.auth.message.MessageService;
import org.nezxenka.auth.security.PasswordPolicy;
import org.nezxenka.auth.security.PasswordService;
import org.nezxenka.auth.security.PasswordViolation;
import org.nezxenka.auth.session.PlayerSession;
import org.nezxenka.auth.session.SessionService;

@RequiredArgsConstructor
public final class PasswordChangeService {

    private final MessageService messages;
    private final SessionService sessions;
    private final AccountService accounts;
    private final PasswordService passwords;
    private final PasswordPolicy policy;
    private final AsyncExecutors executors;
    private final MainThreadExecutor mainThread;
    private final FailureHandler failures;

    public void change(Player player, String currentPassword, String newPassword) {
        PlayerSession session = sessions.get(player.getUniqueId());
        if (session == null || !session.isAuthenticated()) {
            messages.send(player, MessageKey.NOT_LOGGED_IN);
            return;
        }
        Account account = session.getAccount();
        if (account == null) {
            messages.send(player, MessageKey.NOT_REGISTERED);
            return;
        }
        if (currentPassword == null || newPassword == null) {
            messages.send(player, MessageKey.USAGE_CHANGE);
            return;
        }
        if (rejectedByPolicy(player, newPassword)) {
            return;
        }
        if (!session.tryAcquire()) {
            messages.send(player, MessageKey.PROCESSING);
            return;
        }
        String storedHash = account.getPasswordHash();
        CompletableFuture
            .supplyAsync(() -> passwords.verify(currentPassword, storedHash).matched()
                ? Optional.of(passwords.hash(newPassword))
                : Optional.<String>empty(), executors.getCrypto())
            .thenCompose(hash -> hash.isEmpty()
                ? CompletableFuture.completedFuture(hash)
                : accounts.updatePassword(session.getUuid(), hash.get()).thenApply(ignored -> hash))
            .thenAcceptAsync(hash -> {
                if (hash.isEmpty()) {
                    messages.send(player, MessageKey.WRONG_PASSWORD);
                    return;
                }
                session.updateAccount(current -> current.withPasswordHash(hash.get()));
                messages.send(player, MessageKey.SUCCESSFULLY_CHANGED);
            }, mainThread)
            .whenComplete((ignored, error) -> {
                session.release();
                if (error != null) {
                    failures.handle(player, "Failed to change password of " + session.getName(), error);
                }
            });
    }

    public void forceChange(CommandSender sender, String targetName, String newPassword) {
        if (rejectedByPolicy(sender, newPassword)) {
            return;
        }
        Player online = Bukkit.getPlayerExact(targetName);
        PlayerSession targetSession = online != null ? sessions.get(online.getUniqueId()) : null;
        CompletableFuture<Optional<Account>> lookup = targetSession != null && targetSession.isRegistered()
            ? CompletableFuture.completedFuture(Optional.of(targetSession.getAccount()))
            : accounts.findByUsername(targetName);
        lookup
            .thenCompose(found -> found.isEmpty()
                ? CompletableFuture.completedFuture(Optional.<UUID>empty())
                : resetPassword(found.get().getUuid(), newPassword))
            .thenAcceptAsync(result -> {
                if (result.isEmpty()) {
                    messages.send(sender, MessageKey.PLAYER_NOT_FOUND);
                    return;
                }
                Player target = Bukkit.getPlayer(result.get());
                if (target != null) {
                    target.kickPlayer(messages.raw(MessageKey.FORCE_CHANGED));
                }
                messages.send(sender, MessageKey.SUCCESSFULLY_CHANGED);
            }, mainThread)
            .exceptionally(error -> {
                failures.handle(sender, "Failed to change password of " + targetName, error);
                return null;
            });
    }

    private CompletableFuture<Optional<UUID>> resetPassword(UUID uuid, String password) {
        return CompletableFuture
            .supplyAsync(() -> passwords.hash(password), executors.getCrypto())
            .thenCompose(hash -> accounts.resetPassword(uuid, hash))
            .thenApply(ignored -> Optional.of(uuid));
    }

    private boolean rejectedByPolicy(CommandSender sender, String password) {
        Optional<PasswordViolation> violation = policy.check(password);
        violation.ifPresent(found -> messages.send(sender, found.getMessageKey(), policy.placeholders()));
        return violation.isPresent();
    }
}
