package org.nezxenka.auth.listener;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.nezxenka.auth.config.ConfigService;
import org.nezxenka.auth.config.settings.RestrictionSettings;

@RequiredArgsConstructor
public final class ChatListener implements Listener {

    private static final String COMMAND_PREFIX = "/";
    private static final String OWN_NAMESPACE = "auth:";

    private final ConfigService config;
    private final AccessGuard guard;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!guard.isRestrictedAction(player) || !config.settings().restrictions().blockChat()) {
            return;
        }
        event.setCancelled(true);
        guard.prompt(player);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!guard.isRestrictedAction(player)) {
            return;
        }
        RestrictionSettings restrictions = config.settings().restrictions();
        if (!restrictions.blockCommands() || restrictions.isCommandAllowed(label(event.getMessage()))) {
            return;
        }
        event.setCancelled(true);
        guard.prompt(player);
    }

    private static String label(String message) {
        String command = message.startsWith(COMMAND_PREFIX) ? message.substring(COMMAND_PREFIX.length()) : message;
        int space = command.indexOf(' ');
        if (space >= 0) {
            command = command.substring(0, space);
        }
        command = command.toLowerCase(Locale.ROOT);
        return command.startsWith(OWN_NAMESPACE) ? command.substring(OWN_NAMESPACE.length()) : command;
    }
}
