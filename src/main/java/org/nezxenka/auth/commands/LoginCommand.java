package org.nezxenka.auth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.nezxenka.auth.Auth;
import org.nezxenka.auth.managers.SessionManager;

public class LoginCommand implements CommandExecutor {

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        SessionManager sessionManager = Auth.getInstance().getSessionManager();

        if (sessionManager.isAuthenticated(player)) {
            player.sendMessage(
                Auth.getInstance()
                    .getMessageManager()
                    .getMessage("already_logged_in")
            );
            return true;
        }

        if (
            !Auth.getInstance()
                .getDatabaseManager()
                .isRegistered(player.getUniqueId().toString())
        ) {
            player.sendMessage(
                Auth.getInstance()
                    .getMessageManager()
                    .getMessage("not_registered")
            );
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(
                Auth.getInstance()
                    .getMessageManager()
                    .getMessage("not_logged_in")
            );
            return true;
        }

        String password = args[0];
        String storedHash = Auth.getInstance()
            .getDatabaseManager()
            .getPassword(player.getUniqueId().toString());

        if (
            !Auth.getInstance()
                .getPasswordHasher()
                .verifyPassword(password, storedHash)
        ) {
            sessionManager.recordFailedAttempt(player);
            player.sendMessage(
                Auth.getInstance()
                    .getMessageManager()
                    .getMessage("wrong_password")
            );
            return true;
        }

        sessionManager.resetFailedAttempts(player);
        sessionManager.addAuthenticated(player);
        Auth.getInstance()
            .getDatabaseManager()
            .updateLastLogin(player.getUniqueId().toString());
        saveIpSession(player);
        player.sendMessage(
            Auth.getInstance().getMessageManager().getMessage("login_success")
        );
        return true;
    }

    private void saveIpSession(Player player) {
        boolean enabled = Auth.getInstance()
            .getConfig()
            .getBoolean("security.ip-session.enabled", true);
        if (!enabled) return;
        long lifetime = Auth.getInstance()
            .getConfig()
            .getLong("security.ip-session.lifetime", 21600);
        String ip = player.getAddress().getAddress().getHostAddress();
        long expiry = System.currentTimeMillis() + (lifetime * 1000L);
        Auth.getInstance()
            .getDatabaseManager()
            .saveSession(player.getUniqueId().toString(), ip, expiry);
    }
}
