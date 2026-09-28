package org.nezxenka.auth.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.nezxenka.auth.config.ConfigService;
import org.nezxenka.auth.config.settings.RestrictionSettings;

@RequiredArgsConstructor
public final class InventoryListener implements Listener {

    private final ConfigService config;
    private final AccessGuard guard;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player player && restrictions().blockInventory() && guard.isRestricted(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player && restrictions().blockInventory() && guard.isRestricted(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (restrictions().blockDrop() && guard.isRestricted(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && restrictions().blockPickup() && guard.isRestricted(player)) {
            event.setCancelled(true);
        }
    }

    private RestrictionSettings restrictions() {
        return config.settings().restrictions();
    }
}
