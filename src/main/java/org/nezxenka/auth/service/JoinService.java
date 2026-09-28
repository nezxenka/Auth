package org.nezxenka.auth.service;

import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.nezxenka.auth.account.Account;
import org.nezxenka.auth.account.AccountService;
import org.nezxenka.auth.concurrent.Futures;
import org.nezxenka.auth.concurrent.MainThreadExecutor;
import org.nezxenka.auth.config.ConfigService;
import org.nezxenka.auth.config.settings.IpSessionSettings;
import org.nezxenka.auth.message.MessageKey;
import org.nezxenka.auth.message.MessageService;
import org.nezxenka.auth.session.AuthState;
import org.nezxenka.auth.session.PlayerSession;
import org.nezxenka.auth.session.SessionService;
import org.nezxenka.auth.telegram.TelegramService;
import org.nezxenka.auth.util.Permissions;
import org.nezxenka.auth.util.Players;

@RequiredArgsConstructor
public final class JoinService {

    private final Logger logger;
    private final ConfigService config;
    private final MessageService messages;
    private final SessionService sessions;
    private final AccountService accounts;
    private final AuthenticationService authentication;
    private final TelegramService telegram;
    private final MainThreadExecutor mainThread;

    public void handleJoin(Player player) {
        PlayerSession prepared = sessions.activate(player.getUniqueId());
        if (prepared != null) {
            begin(player, prepared);
            return;
        }
        PlayerSession loading = new PlayerSession(player.getUniqueId(), player.getName(), Players.address(player), null);
        sessions.register(loading);
        accounts
            .findByUuid(player.getUniqueId())
            .thenAcceptAsync(account -> {
                if (!sessions.isCurrent(player, loading)) {
                    return;
                }
                loading.setAccount(account.orElse(null));
                begin(player, loading);
            }, mainThread)
            .exceptionally(error -> {
                logger.log(Level.SEVERE, "Failed to load account of " + player.getName(), Futures.unwrap(error));
                mainThread.execute(() -> player.kickPlayer(messages.raw(MessageKey.DATABASE_ERROR)));
                return null;
            });
    }

    public void handleQuit(Player player) {
        PlayerSession session = sessions.remove(player.getUniqueId());
        if (session == null) {
            return;
        }
        telegram.cancelTwoFactor(session.getUuid());
        telegram.notifyLeave(session);
    }

    public void restoreOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            handleJoin(player);
        }
    }

    private void begin(Player player, PlayerSession session) {
        if (player.hasPermission(Permissions.BYPASS)) {
            session.setBypass(true);
            session.transition(AuthState.AUTHENTICATED);
            telegram.notifyJoin(session);
            return;
        }
        Account account = session.getAccount();
        if (account == null) {
            session.transition(AuthState.UNREGISTERED);
            messages.send(player, MessageKey.NOT_REGISTERED);
            return;
        }
        IpSessionSettings ipSession = config.settings().security().ipSession();
        if (ipSession.enabled() && account.hasValidIpSession(session.getIp(), System.currentTimeMillis())) {
            authentication.authorize(player, session, MessageKey.LOGIN_SUCCESS, false);
            telegram.notifyJoin(session);
            return;
        }
        if (telegram.requiresTwoFactor(account)) {
            session.transition(AuthState.PENDING_TWO_FACTOR);
            telegram.requestTwoFactor(session);
            messages.send(player, MessageKey.TWO_FACTOR_WAITING);
            return;
        }
        session.transition(AuthState.UNAUTHENTICATED);
        messages.send(player, MessageKey.NOT_LOGGED_IN);
        telegram.notifyJoin(session);
    }
}
