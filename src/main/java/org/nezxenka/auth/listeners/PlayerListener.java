package org.nezxenka.auth.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.nezxenka.auth.Auth;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        String ip = player.getAddress().getAddress().getHostAddress();

        if (player.hasPermission("auth.bypass")) {
            Auth.getInstance().getSessionManager().addAuthenticated(player);
            return;
        }
        Auth.getInstance().getSessionManager().setLoginTimeout(player);

        boolean isRegistered = Auth.getInstance()
            .getDatabaseManager()
            .isRegistered(uuid);

        if (!isRegistered) {
            player.sendMessage(
                Auth.getInstance()
                    .getMessageManager()
                    .getMessage("not_registered")
            );
            return;
        }

        boolean ipSessionEnabled = Auth.getInstance()
            .getConfig()
            .getBoolean("security.ip-session.enabled", true);
        long sessionLifetime = Auth.getInstance()
            .getConfig()
            .getLong("security.ip-session.lifetime", 21600);

        if (ipSessionEnabled) {
            String storedIp = Auth.getInstance()
                .getDatabaseManager()
                .getSessionIp(uuid);
            long expiry = Auth.getInstance()
                .getDatabaseManager()
                .getSessionExpiry(uuid);

            if (
                storedIp != null &&
                storedIp.equals(ip) &&
                System.currentTimeMillis() < expiry
            ) {
                Auth.getInstance().getSessionManager().addAuthenticated(player);
                Auth.getInstance().getDatabaseManager().updateLastLogin(uuid);
                player.sendMessage(
                    Auth.getInstance()
                        .getMessageManager()
                        .getMessage("login_success")
                );
                return;
            }
        }

        boolean has2FA = Auth.getInstance()
            .getDatabaseManager()
            .is2FAEnabled(uuid);
        boolean telegram2FA = Auth.getInstance()
            .getConfig()
            .getBoolean("telegram.2fa.enabled", false);

        if (
            has2FA && telegram2FA && Auth.getInstance().getTelegramBot() != null
        ) {
            Auth.getInstance().getSessionManager().setPending2FA(player, ip);
            Auth.getInstance().getTelegramBot().request2FA(player, ip);
            player.sendMessage(
                "§eОжидайте подтверждения входа через Telegram..."
            );
            return;
        }

        player.sendMessage(
            Auth.getInstance().getMessageManager().getMessage("not_logged_in")
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoinNotify(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (Auth.getInstance().getTelegramBot() == null) return;
        String ip = player.getAddress().getAddress().getHostAddress();
        Auth.getInstance().getTelegramBot().notifyJoin(player, ip);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (Auth.getInstance().getTelegramBot() != null) {
            String ip = player.getAddress().getAddress().getHostAddress();
            Auth.getInstance().getTelegramBot().notifyLeave(player, ip);
        }
        Auth.getInstance().getSessionManager().invalidate(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!Auth.getInstance().getSessionManager().isAuthenticated(player)) {
            if (Auth.getInstance().getConfigManager().isBlockMovement()) {
                if (
                    event.getFrom().getX() != event.getTo().getX() ||
                    event.getFrom().getY() != event.getTo().getY() ||
                    event.getFrom().getZ() != event.getTo().getZ()
                ) {
                    event.setTo(event.getFrom());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!Auth.getInstance().getSessionManager().isAuthenticated(player)) {
            if (Auth.getInstance().getConfigManager().isBlockChat()) {
                event.setCancelled(true);
                if (
                    Auth.getInstance()
                        .getDatabaseManager()
                        .isRegistered(player.getUniqueId().toString())
                ) {
                    player.sendMessage(
                        Auth.getInstance()
                            .getMessageManager()
                            .getMessage("not_logged_in")
                    );
                } else {
                    player.sendMessage(
                        Auth.getInstance()
                            .getMessageManager()
                            .getMessage("not_registered")
                    );
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (Auth.getInstance().getSessionManager().isAuthenticated(player)) {
            return;
        }
        String cmd = event
            .getMessage()
            .split(" ")[0]
            .toLowerCase()
            .replace("/", "");
        if (
            cmd.equals("login") ||
            cmd.equals("register") ||
            cmd.equals("log") ||
            cmd.equals("reg") ||
            cmd.equals("l")
        ) {
            return;
        }
        if (Auth.getInstance().getConfigManager().isBlockCommands()) {
            event.setCancelled(true);
            if (
                Auth.getInstance()
                    .getDatabaseManager()
                    .isRegistered(player.getUniqueId().toString())
            ) {
                player.sendMessage(
                    Auth.getInstance()
                        .getMessageManager()
                        .getMessage("not_logged_in")
                );
            } else {
                player.sendMessage(
                    Auth.getInstance()
                        .getMessageManager()
                        .getMessage("not_registered")
                );
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (
            !Auth.getInstance()
                .getSessionManager()
                .isAuthenticated(event.getPlayer())
        ) {
            if (Auth.getInstance().getConfigManager().isBlockInteract()) {
                event.setCancelled(true);
            }
        }
    }
}
