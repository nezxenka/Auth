package org.nezxenka.auth.command.impl;

import java.util.List;
import java.util.function.BooleanSupplier;
import org.bukkit.command.CommandSender;
import org.nezxenka.auth.command.BaseCommand;
import org.nezxenka.auth.message.MessageKey;
import org.nezxenka.auth.message.MessageService;
import org.nezxenka.auth.message.Placeholder;
import org.nezxenka.auth.util.Permissions;

public final class AuthAdminCommand extends BaseCommand {

    private static final String RELOAD = "reload";

    private final String version;
    private final BooleanSupplier reloadAction;

    public AuthAdminCommand(MessageService messages, String version, BooleanSupplier reloadAction) {
        super(messages, Permissions.ADMIN);
        this.version = version;
        this.reloadAction = reloadAction;
    }

    @Override
    protected void execute(CommandSender sender, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase(RELOAD)) {
            messages.send(sender, reloadAction.getAsBoolean() ? MessageKey.ADMIN_RELOAD : MessageKey.ADMIN_RELOAD_FAILED);
            return;
        }
        sender.sendMessage(messages.raw(MessageKey.ADMIN_HELP_HEADER, Placeholder.of("version", version)));
        sender.sendMessage(messages.raw(MessageKey.ADMIN_HELP_RELOAD));
    }

    @Override
    protected List<String> complete(CommandSender sender, String[] args) {
        return args.length == 1 ? filter(List.of(RELOAD), args[0]) : List.of();
    }
}
