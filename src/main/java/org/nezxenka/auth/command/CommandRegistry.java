package org.nezxenka.auth.command;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public final class CommandRegistry {

    private final JavaPlugin plugin;

    public void register(String name, BaseCommand executor) {
        PluginCommand command = plugin.getCommand(name);
        if (command == null) {
            throw new IllegalStateException("Command " + name + " is not defined in plugin.yml");
        }
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }
}
