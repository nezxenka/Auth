package org.nezxenka.auth.task;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.nezxenka.auth.message.MessageKey;
import org.nezxenka.auth.message.MessageService;
import org.nezxenka.auth.session.PlayerSession;
import org.nezxenka.auth.session.SessionService;

@RequiredArgsConstructor
public final class ReminderTask implements Runnable {

    private final MessageService messages;
    private final SessionService sessions;

    @Override
    public void run() {
        for (PlayerSession session : sessions.all()) {
            MessageKey reminder = switch (session.getState()) {
                case UNREGISTERED -> MessageKey.REMINDER_REGISTER;
                case UNAUTHENTICATED -> MessageKey.REMINDER_LOGIN;
                default -> null;
            };
            if (reminder == null) {
                continue;
            }
            Player player = Bukkit.getPlayer(session.getUuid());
            if (player != null) {
                messages.send(player, reminder);
            }
        }
    }
}
