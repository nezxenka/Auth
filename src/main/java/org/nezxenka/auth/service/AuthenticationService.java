package org.nezxenka.auth.service;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.nezxenka.auth.account.AccountService;
import org.nezxenka.auth.concurrent.AsyncExecutors;
import org.nezxenka.auth.concurrent.Futures;
import org.nezxenka.auth.concurrent.MainThreadExecutor;
import org.nezxenka.auth.config.ConfigService;
import org.nezxenka.auth.config.settings.IpSessionSettings;
import org.nezxenka.auth.message.MessageKey;
import org.nezxenka.auth.message.MessageService;
import org.nezxenka.auth.security.PasswordService;
import org.nezxenka.auth.security.PasswordVerification;
import org.nezxenka.auth.session.AuthState;
import org.nezxenka.auth.session.PlayerSession;
import org.nezxenka.auth.session.SessionService;

@RequiredArgsConstructor
public final class AuthenticationService {

    private final ConfigService config;
    private final MessageService messages;
    private final SessionService sessions;
    private final AccountService accounts;
    private final PasswordService passwords;
    private final AsyncExecutors executors;
    private final MainThreadExecutor mainThread;
    private final FailureHandler failures;
    private final Logger logger;

    public void login(Player player, String password) {
        PlayerSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            messages.send(player, MessageKey.DATA_LOADING);
            return;
        }
        MessageKey rejection = rejectLogin(session);
        if (rejection != null) {
            messages.send(player, rejection);
            return;
        }
        if (password == null) {
            messages.send(player, MessageKey.NOT_LOGGED_IN);
            return;
        }
        if (!session.tryAcquire()) {
            messages.send(player, MessageKey.PROCESSING);
            return;
        }
        String storedHash = session.getAccount().getPasswordHash();
        CompletableFuture
            .supplyAsync(() -> passwords.verify(password, storedHash), executors.getCrypto())
            .thenAcceptAsync(result -> completeLogin(player, session, password, result), mainThread)
            .whenComplete((ignored, error) -> {
                session.release();
                if (error != null) {
                    failures.handle(player, "Failed to log in " + session.getName(), error);
                }
            });
    }

    public void authorize(Player player, PlayerSession session, MessageKey message, boolean renewIpSession) {
        session.resetFailedAttempts();
        session.transition(AuthState.AUTHENTICATED);
        messages.send(player, message);
        persistLogin(session, renewIpSession);
    }

    private MessageKey rejectLogin(PlayerSession session) {
        return switch (session.getState()) {
            case AUTHENTICATED -> MessageKey.ALREADY_LOGGED_IN;
            case LOADING -> MessageKey.DATA_LOADING;
            case UNREGISTERED -> MessageKey.NOT_REGISTERED;
            case PENDING_TWO_FACTOR -> MessageKey.TWO_FACTOR_WAITING;
            case UNAUTHENTICATED -> session.isRegistered() ? null : MessageKey.NOT_REGISTERED;
        };
    }

    private void completeLogin(Player player, PlayerSession session, String password, PasswordVerification result) {
        if (!sessions.isCurrent(player, session) || session.getState() != AuthState.UNAUTHENTICATED) {
            return;
        }
        if (!result.matched()) {
            int attempts = session.registerFailedAttempt();
            if (attempts >= config.settings().security().maxLoginAttempts()) {
                player.kickPlayer(messages.raw(MessageKey.MAX_ATTEMPTS));
                return;
            }
            messages.send(player, MessageKey.WRONG_PASSWORD);
            return;
        }
        authorize(player, session, MessageKey.LOGIN_SUCCESS, true);
        if (result.rehashRequired()) {
            upgradeHash(session, password);
        }
    }

    private void persistLogin(PlayerSession session, boolean renewIpSession) {
        if (!session.isRegistered()) {
            return;
        }
        long now = System.currentTimeMillis();
        String name = session.getName();
        CompletableFuture<Void> future;
        if (renewIpSession) {
            IpSessionSettings ipSession = config.settings().security().ipSession();
            String ip = ipSession.enabled() ? session.getIp() : null;
            long expiry = ipSession.enabled() ? now + ipSession.lifetimeMillis() : 0L;
            session.updateAccount(account -> account
                .withUsername(name)
                .withLastLogin(now)
                .withSessionIp(ip)
                .withSessionExpiry(expiry)
            );
            future = accounts.recordLogin(session.getUuid(), name, now, ip, expiry);
        } else {
            session.updateAccount(account -> account.withUsername(name).withLastLogin(now));
            future = accounts.recordLastLogin(session.getUuid(), name, now);
        }
        Futures.logFailure(future, logger, "Failed to save login of " + name);
    }

    private void upgradeHash(PlayerSession session, String password) {
        CompletableFuture<Void> future = CompletableFuture
            .supplyAsync(() -> passwords.hash(password), executors.getCrypto())
            .thenCompose(hash -> accounts
                .updatePassword(session.getUuid(), hash)
                .thenRun(() -> session.updateAccount(account -> account.withPasswordHash(hash)))
            );
        Futures.logFailure(future, logger, "Failed to upgrade password hash of " + session.getName());
    }
}
