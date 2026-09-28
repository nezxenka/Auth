package org.nezxenka.auth.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.nezxenka.auth.config.ConfigService;

@RequiredArgsConstructor
public final class MovementListener implements Listener {

    private final ConfigService config;
    private final AccessGuard guard;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!guard.isRestrictedAction(event.getPlayer()) || !config.settings().restrictions().blockMovement()) {
            return;
        }
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) {
            return;
        }
        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            event.setTo(from);
        }
    }
}
