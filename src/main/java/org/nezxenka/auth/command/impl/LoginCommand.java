package org.nezxenka.auth.command.impl;

import org.bukkit.entity.Player;
import org.nezxenka.auth.command.PlayerCommand;
import org.nezxenka.auth.message.MessageService;
import org.nezxenka.auth.service.AuthenticationService;

public final class LoginCommand extends PlayerCommand {

    private final AuthenticationService authentication;

    public LoginCommand(MessageService messages, AuthenticationService authentication) {
        super(messages);
        this.authentication = authentication;
    }

    @Override
    protected void handle(Player player, String[] args) {
        authentication.login(player, argument(args, 0));
    }
}
