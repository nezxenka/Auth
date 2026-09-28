package org.nezxenka.auth.command.impl;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.nezxenka.auth.command.BaseCommand;
import org.nezxenka.auth.message.MessageKey;
import org.nezxenka.auth.message.MessageService;
import org.nezxenka.auth.service.PasswordChangeService;
import org.nezxenka.auth.util.Permissions;

public final class AdminChangePasswordCommand extends BaseCommand {

    private final PasswordChangeService passwordChange;

    public AdminChangePasswordCommand(MessageService messages, PasswordChangeService passwordChange) {
        super(messages, Permissions.ADMIN);
        this.passwordChange = passwordChange;
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            messages.send(sender, MessageKey.USAGE_ADMIN_CHANGE);
            return;
        }
        passwordChange.forceChange(sender, args[0], args[1]);
    }

    @Override
    protected List<String> complete(CommandSender sender, String[] args) {
        if (args.length != 1) {
            return List.of();
        }
        return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[0]);
    }
}
