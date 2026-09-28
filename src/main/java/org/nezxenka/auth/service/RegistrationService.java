package org.nezxenka.auth.service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
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
import org.nezxenka.auth.session.AuthState;
import org.nezxenka.auth.session.PlayerSession;
import org.nezxenka.auth.session.SessionService;

@RequiredArgsConstructor
public final class RegistrationService {

    private final MessageService messages;
    private final SessionService sessions;
    private final AccountService accounts;
    private final PasswordService passwords;
    private final PasswordPolicy policy;
    private final AuthenticationService authentication;
    private final AsyncExecutors executors;
    private final MainThreadExecutor mainThread;
    private final FailureHandler failures;

    public void register(Player player, String password, String confirmation) {
        PlayerSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            messages.send(player, MessageKey.DATA_LOADING);
            return;
        }
        MessageKey rejection = switch (session.getState()) {
            case AUTHENTICATED -> MessageKey.ALREADY_LOGGED_IN;
            case LOADING -> MessageKey.DATA_LOADING;
            case UNAUTHENTICATED, PENDING_TWO_FACTOR -> MessageKey.ALREADY_REGISTERED;
            case UNREGISTERED -> null;
        };
        if (rejection != null) {
            messages.send(player, rejection);
            return;
        }
        if (password == null || confirmation == null) {
            messages.send(player, MessageKey.NOT_REGISTERED);
            return;
        }
        if (!password.equals(confirmation)) {
            messages.send(player, MessageKey.PASSWORD_MISMATCH);
            return;
        }
        Optional<PasswordViolation> violation = policy.check(password);
        if (violation.isPresent()) {
            messages.send(player, violation.get().getMessageKey(), policy.placeholders());
            return;
        }
        if (!session.tryAcquire()) {
            messages.send(player, MessageKey.PROCESSING);
            return;
        }
        CompletableFuture
            .supplyAsync(() -> newAccount(session, passwords.hash(password)), executors.getCrypto())
            .thenCompose(account -> accounts.create(account).thenApply(ignored -> account))
            .thenAcceptAsync(account -> completeRegistration(player, session, account), mainThread)
            .whenComplete((ignored, error) -> {
                session.release();
                if (error != null) {
                    failures.handle(player, "Failed to register " + session.getName(), error);
                }
            });
    }

    private Account newAccount(PlayerSession session, String passwordHash) {
        return Account.builder()
            .uuid(session.getUuid())
            .username(session.getName())
            .passwordHash(passwordHash)
            .registeredAt(System.currentTimeMillis())
            .build();
    }

    private void completeRegistration(Player player, PlayerSession session, Account account) {
        if (!sessions.isCurrent(player, session) || session.getState() != AuthState.UNREGISTERED) {
            return;
        }
        session.setAccount(account);
        authentication.authorize(player, session, MessageKey.REGISTER_SUCCESS, true);
    }
}
