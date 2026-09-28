package org.nezxenka.auth.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.nezxenka.auth.config.ConfigService;
import org.nezxenka.auth.config.settings.RestrictionSettings;

@RequiredArgsConstructor
public final class InteractionListener implements Listener {

    private final ConfigService config;
    private final AccessGuard guard;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (guard.isRestrictedAction(event.getPlayer()) && restrictions().blockInteract()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (guard.isRestrictedAction(event.getPlayer()) && restrictions().blockInteract()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && restrictions().blockDamage() && guard.isRestricted(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player && restrictions().blockInteract() && guard.isRestricted(player)) {
            event.setCancelled(true);
        }
    }

    private RestrictionSettings restrictions() {
        return config.settings().restrictions();
    }
}
