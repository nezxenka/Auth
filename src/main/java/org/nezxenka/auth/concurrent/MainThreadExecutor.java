package org.nezxenka.auth.concurrent;

import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public final class MainThreadExecutor implements Executor {

    private final Plugin plugin;

    @Override
    public void execute(Runnable command) {
        if (!plugin.isEnabled()) {
            return;
        }
        if (Bukkit.isPrimaryThread()) {
            command.run();
            return;
        }
        Bukkit.getScheduler().runTask(plugin, command);
    }
}
