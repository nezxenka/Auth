package org.nezxenka.auth.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.nezxenka.auth.message.MessageKey;
import org.nezxenka.auth.message.MessageService;

public abstract class PlayerCommand extends BaseCommand {

    protected PlayerCommand(MessageService messages) {
        super(messages, null);
    }

    @Override
    protected final void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, MessageKey.ONLY_PLAYERS);
            return;
        }
        handle(player, args);
    }

    protected abstract void handle(Player player, String[] args);
}
