package org.nezxenka.auth.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.nezxenka.auth.Auth;

public class BlockListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!Auth.getInstance().getSessionManager().isAuthenticated(player)) {
            if (Auth.getInstance().getConfigManager().isBlockBreak()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!Auth.getInstance().getSessionManager().isAuthenticated(player)) {
            if (Auth.getInstance().getConfigManager().isBlockPlace()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (
                !Auth.getInstance().getSessionManager().isAuthenticated(player)
            ) {
                if (Auth.getInstance().getConfigManager().isBlockDamage()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            if (
                !Auth.getInstance().getSessionManager().isAuthenticated(player)
            ) {
                if (Auth.getInstance().getConfigManager().isBlockInventory()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!Auth.getInstance().getSessionManager().isAuthenticated(player)) {
            if (Auth.getInstance().getConfigManager().isBlockDrop()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (
                !Auth.getInstance().getSessionManager().isAuthenticated(player)
            ) {
                if (Auth.getInstance().getConfigManager().isBlockPickup()) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
