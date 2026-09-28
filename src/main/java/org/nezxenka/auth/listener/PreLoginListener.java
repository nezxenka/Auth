package org.nezxenka.auth.listener;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.nezxenka.auth.account.Account;
import org.nezxenka.auth.account.AccountService;
import org.nezxenka.auth.concurrent.Futures;
import org.nezxenka.auth.config.ConfigService;
import org.nezxenka.auth.database.DataAccessException;
import org.nezxenka.auth.message.MessageKey;
import org.nezxenka.auth.message.MessageService;
import org.nezxenka.auth.session.PlayerSession;
import org.nezxenka.auth.session.SessionService;

@RequiredArgsConstructor
public final class PreLoginListener implements Listener {

    private final Logger logger;
    private final ConfigService config;
    private final MessageService messages;
    private final SessionService sessions;
    private final AccountService accounts;

    @EventHandler(priority = EventPriority.LOW)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }
        UUID uuid = event.getUniqueId();
        String name = event.getName();
        if (config.settings().security().singleSession()) {
            PlayerSession online = sessions.get(uuid);
            if (online != null && online.isAuthenticated()) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, messages.raw(MessageKey.ALREADY_ONLINE));
                return;
            }
        }
        try {
            Account account = accounts.loadBlocking(uuid).map(found -> refreshName(found, name)).orElse(null);
            sessions.prepare(new PlayerSession(uuid, name, event.getAddress().getHostAddress(), account));
        } catch (DataAccessException exception) {
            logger.log(Level.SEVERE, "Failed to load account of " + name, exception);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, messages.raw(MessageKey.DATABASE_ERROR));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLoginResult(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            sessions.discardPrepared(event.getUniqueId());
        }
    }

    private Account refreshName(Account account, String name) {
        if (name.equals(account.getUsername())) {
            return account;
        }
        Futures.logFailure(accounts.updateUsername(account.getUuid(), name), logger, "Failed to update username of " + name);
        return account.withUsername(name);
    }
}
