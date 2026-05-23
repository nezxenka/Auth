package org.nezxenka.auth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.nezxenka.auth.Auth;

public class ChangePassCommand implements CommandExecutor {

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

        if (!Auth.getInstance().getSessionManager().isAuthenticated(player)) {
            player.sendMessage(
                Auth.getInstance()
                    .getMessageManager()
                    .getMessage("not_logged_in")
            );
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(
                Auth.getInstance()
                    .getMessageManager()
                    .getMessage("usage_change")
            );
            return true;
        }

        String oldPassword = args[0];
        String newPassword = args[1];

        String storedHash = Auth.getInstance()
            .getDatabaseManager()
            .getPassword(player.getUniqueId().toString());
        if (
            !Auth.getInstance()
                .getPasswordHasher()
                .verifyPassword(oldPassword, storedHash)
        ) {
            player.sendMessage(
                Auth.getInstance()
                    .getMessageManager()
                    .getMessage("wrong_password")
            );
            return true;
        }

        if (
            newPassword.length() <
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
            newPassword.length() >
            Auth.getInstance().getConfigManager().getMaxPasswordLength()
        ) {
            player.sendMessage(
                Auth.getInstance()
                    .getMessageManager()
                    .getMessage("password_too_long")
            );
            return true;
        }

        String newHash = Auth.getInstance()
            .getPasswordHasher()
            .hashPassword(newPassword);
        Auth.getInstance()
            .getDatabaseManager()
            .updatePassword(player.getUniqueId().toString(), newHash);
        player.sendMessage(
            Auth.getInstance()
                .getMessageManager()
                .getMessage("successfully_changed")
        );
        return true;
    }
}
