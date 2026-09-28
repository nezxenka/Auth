package org.nezxenka.auth.command.impl;

import org.bukkit.entity.Player;
import org.nezxenka.auth.command.PlayerCommand;
import org.nezxenka.auth.message.MessageService;
import org.nezxenka.auth.service.PasswordChangeService;

public final class ChangePasswordCommand extends PlayerCommand {

    private final PasswordChangeService passwordChange;

    public ChangePasswordCommand(MessageService messages, PasswordChangeService passwordChange) {
        super(messages);
        this.passwordChange = passwordChange;
    }

    @Override
    protected void handle(Player player, String[] args) {
        passwordChange.change(player, argument(args, 0), argument(args, 1));
    }
}
