package org.nezxenka.auth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.nezxenka.auth.Auth;

public class RegisterCommand implements CommandExecutor {

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

        if (Auth.getInstance().getSessionManager().isAuthenticated(player)) {
            player.sendMessage(
                Auth.getInstance()
                    .getMessageManager()
                    .getMessage("already_logged_in")
            );
            return true;
        }

        if (
            Auth.getInstance()
                .getDatabaseManager()
                .isRegistered(player.getUniqueId().toString())
        ) {
            player.sendMessage(
                Auth.getInstance()
                    .getMessageManager()
                    .getMessage("already_registered")
            );
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(
                Auth.getInstance()
                    .getMessageManager()
                    .getMessage("not_registered")
            );
            return true;
        }

        String password = args[0];
        String confirmPassword = args[1];

        if (!password.equals(confirmPassword)) {
            player.sendMessage(
                Auth.getInstance()
                    .getMessageManager()
                    .getMessage("password_mismatch")
            );
            return true;
        }

        if (
            password.length() <
            Auth.getInstance().getConfigManager().getMinPasswordLength()
        ) {
            player.sendMessage(
                Auth.getInstance()
                    .getMessageManager()
                    .getMessage("password_too_short")
            );
            return true;
        }

        if (
            password.length() >
            Auth.getInstance().getConfigManager().getMaxPasswordLength()
        ) {
            player.sendMessage(
                Auth.getInstance()
                    .getMessageManager()
                    .getMessage("password_too_long")
            );
            return true;
        }

        String hash = Auth.getInstance()
            .getPasswordHasher()
            .hashPassword(password);
        Auth.getInstance()
            .getDatabaseManager()
            .register(player.getUniqueId().toString(), player.getName(), hash);
        Auth.getInstance().getSessionManager().addAuthenticated(player);
        saveIpSession(player);
        player.sendMessage(
            Auth.getInstance()
                .getMessageManager()
                .getMessage("register_success")
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
