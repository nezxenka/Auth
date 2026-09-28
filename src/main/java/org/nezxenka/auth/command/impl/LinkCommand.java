package org.nezxenka.auth.command.impl;

import org.bukkit.entity.Player;
import org.nezxenka.auth.command.PlayerCommand;
import org.nezxenka.auth.message.MessageService;
import org.nezxenka.auth.service.LinkConfirmationService;

public final class LinkCommand extends PlayerCommand {

    private final LinkConfirmationService linkConfirmation;

    public LinkCommand(MessageService messages, LinkConfirmationService linkConfirmation) {
        super(messages);
        this.linkConfirmation = linkConfirmation;
    }

    @Override
    protected void handle(Player player, String[] args) {
        linkConfirmation.confirm(player, argument(args, 0));
    }
}
