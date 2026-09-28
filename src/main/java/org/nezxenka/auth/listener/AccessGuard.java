package org.nezxenka.auth.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.nezxenka.auth.message.MessageKey;
import org.nezxenka.auth.message.MessageService;
import org.nezxenka.auth.session.PlayerSession;
import org.nezxenka.auth.session.SessionService;

@RequiredArgsConstructor
public final class AccessGuard {

    private static final String NPC_METADATA = "NPC";

    private final SessionService sessions;
    private final MessageService messages;

    public boolean isRestricted(Player player) {
        PlayerSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return !player.hasMetadata(NPC_METADATA);
        }
        return !session.isAuthenticated();
    }

    public boolean isRestrictedAction(Player player) {
        PlayerSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return !player.hasMetadata(NPC_METADATA);
        }
        if (session.isAuthenticated()) {
            session.touch();
            return false;
        }
        return true;
    }

    public void prompt(Player player) {
        PlayerSession session = sessions.get(player.getUniqueId());
        MessageKey key = session == null ? MessageKey.NOT_LOGGED_IN : switch (session.getState()) {
            case LOADING -> MessageKey.DATA_LOADING;
            case UNREGISTERED -> MessageKey.NOT_REGISTERED;
            case PENDING_TWO_FACTOR -> MessageKey.TWO_FACTOR_WAITING;
            case UNAUTHENTICATED, AUTHENTICATED -> MessageKey.NOT_LOGGED_IN;
        };
        messages.send(player, key);
    }
}
