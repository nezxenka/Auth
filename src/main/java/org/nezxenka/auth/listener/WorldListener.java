package org.nezxenka.auth.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.nezxenka.auth.config.ConfigService;

@RequiredArgsConstructor
public final class WorldListener implements Listener {

    private final ConfigService config;
    private final AccessGuard guard;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (config.settings().restrictions().blockBreak() && guard.isRestricted(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (config.settings().restrictions().blockPlace() && guard.isRestricted(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
