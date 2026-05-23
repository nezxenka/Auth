package org.nezxenka.auth.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.nezxenka.auth.Auth;

public class AuthAdminCommand implements CommandExecutor {

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

        if (args.length < 1) {
            sender.sendMessage(
                "§eAuth v" +
                    Auth.getInstance().getDescription().getVersion() +
                    " by nezxenka"
            );
            sender.sendMessage("§e/auth reload - Reload configuration");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            Auth.getInstance().reloadPlugin();
            sender.sendMessage(
                Auth.getInstance()
                    .getMessageManager()
                    .getMessage("admin_reload")
            );
            return true;
        }

        return false;
    }
}
