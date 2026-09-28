package org.nezxenka.auth.command.impl;

import org.bukkit.entity.Player;
import org.nezxenka.auth.command.PlayerCommand;
import org.nezxenka.auth.message.MessageService;
import org.nezxenka.auth.service.RegistrationService;

public final class RegisterCommand extends PlayerCommand {

    private final RegistrationService registration;

    public RegisterCommand(MessageService messages, RegistrationService registration) {
        super(messages);
        this.registration = registration;
    }

    @Override
    protected void handle(Player player, String[] args) {
        registration.register(player, argument(args, 0), argument(args, 1));
    }
}
