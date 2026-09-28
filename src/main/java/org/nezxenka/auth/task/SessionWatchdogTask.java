package org.nezxenka.auth.task;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.nezxenka.auth.config.ConfigService;
import org.nezxenka.auth.config.settings.PluginSettings;
import org.nezxenka.auth.config.settings.SecuritySettings;
import org.nezxenka.auth.message.MessageKey;
import org.nezxenka.auth.message.MessageService;
import org.nezxenka.auth.session.AuthState;
import org.nezxenka.auth.session.PlayerSession;
import org.nezxenka.auth.session.SessionService;
import org.nezxenka.auth.telegram.TelegramService;

@RequiredArgsConstructor
public final class SessionWatchdogTask implements Runnable {

    private final ConfigService config;
    private final MessageService messages;
    private final SessionService sessions;
    private final TelegramService telegram;

    @Override
    public void run() {
        PluginSettings settings = config.settings();
        SecuritySettings security = settings.security();
        long twoFactorTimeout = settings.telegram().twoFactor().timeoutMillis();
        long now = System.currentTimeMillis();
        for (PlayerSession session : sessions.all()) {
            switch (session.getState()) {
                case PENDING_TWO_FACTOR -> {
                    if (session.millisInState(now) > twoFactorTimeout) {
                        telegram.expireTwoFactor(session.getUuid());
                        kick(session, MessageKey.TWO_FACTOR_TIMEOUT);
                    }
                }
                case UNREGISTERED, UNAUTHENTICATED -> {
                    if (
                        security.loginTimeoutEnabled() &&
                        !session.isAuthenticatedOnce() &&
                        session.millisInState(now) > security.loginTimeoutMillis()
                    ) {
                        kick(session, MessageKey.TIMEOUT_REACHED);
                    }
                }
                case AUTHENTICATED -> {
                    if (
                        security.sessionTimeoutEnabled() &&
                        !session.isBypass() &&
                        session.isRegistered() &&
                        now - session.getLastActivity() > security.sessionTimeoutMillis()
                    ) {
                        expire(session);
                    }
                }
                default -> {
                }
            }
        }
    }

    private void kick(PlayerSession session, MessageKey reason) {
        Player player = Bukkit.getPlayer(session.getUuid());
        if (player != null) {
            player.kickPlayer(messages.raw(reason));
        }
    }

    private void expire(PlayerSession session) {
        session.transition(AuthState.UNAUTHENTICATED);
        Player player = Bukkit.getPlayer(session.getUuid());
        if (player != null) {
            messages.send(player, MessageKey.SESSION_EXPIRED);
        }
    }
}
