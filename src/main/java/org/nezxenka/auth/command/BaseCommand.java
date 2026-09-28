package org.nezxenka.auth.command;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.nezxenka.auth.message.MessageKey;
import org.nezxenka.auth.message.MessageService;

public abstract class BaseCommand implements CommandExecutor, TabCompleter {

    protected final MessageService messages;
    private final String permission;

    protected BaseCommand(MessageService messages, String permission) {
        this.messages = messages;
        this.permission = permission;
    }

    @Override
    public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!hasAccess(sender)) {
            messages.send(sender, MessageKey.NO_PERMISSION);
            return true;
        }
        execute(sender, args);
        return true;
    }

    @Override
    public final List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return hasAccess(sender) ? complete(sender, args) : List.of();
    }

    protected abstract void execute(CommandSender sender, String[] args);

    protected List<String> complete(CommandSender sender, String[] args) {
        return List.of();
    }

    protected static String argument(String[] args, int index) {
        return index < args.length ? args[index] : null;
    }

    protected static List<String> filter(Collection<String> options, String prefix) {
        String lowerPrefix = prefix.toLowerCase(Locale.ROOT);
        return options
            .stream()
            .filter(option -> option.toLowerCase(Locale.ROOT).startsWith(lowerPrefix))
            .sorted()
            .toList();
    }

    private boolean hasAccess(CommandSender sender) {
        return permission == null || sender.hasPermission(permission);
    }
}
