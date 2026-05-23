package org.nezxenka.auth.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.nezxenka.auth.Auth;

public class AdminChangePassCommand implements CommandExecutor {

    @Override
    public boolean onCommand(
        CommandSender sender,
        Command command,
        String label,
        String[] args
    ) {
        if (!sender.hasPermission("auth.admin")) {
            sender.sendMessage(
                Auth.getInstance()
                    .getMessageManager()
                    .getMessage("no_permission")
            );
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(
                Auth.getInstance()
                    .getMessageManager()
                    .getMessage("usage_admin_change")
            );
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(
                Auth.getInstance()
                    .getMessageManager()
                    .getMessage("player_not_found")
            );
            return true;
        }

        String newPassword = args[1];
        if (
            newPassword.length() <
            Auth.getInstance().getConfigManager().getMinPasswordLength()
        ) {
            sender.sendMessage(
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
            sender.sendMessage(
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
            .updatePassword(target.getUniqueId().toString(), newHash);
        Auth.getInstance().getSessionManager().invalidate(target);
        target.kickPlayer(
            Auth.getInstance()
                .getMessageManager()
                .getRawMessage("force_changed")
        );
        sender.sendMessage(
            Auth.getInstance()
                .getMessageManager()
                .getMessage("successfully_changed")
        );
        return true;
    }
}
