package org.nezxenka.auth.managers;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.nezxenka.auth.Auth;

public class ReminderManager {

    private final Auth plugin;
    private int taskId = -1;

    public ReminderManager() {
        this.plugin = Auth.getInstance();
    }

    public void startReminders() {
        if (!plugin.getConfigManager().isRemindersEnabled()) {
            return;
        }

        if (taskId != -1) {
            stopReminders();
        }

        long interval = plugin.getConfigManager().getRemindersInterval();

        taskId = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (plugin.getSessionManager().isPending2FA(player)) {
                        continue;
                    }
                    if (!plugin.getSessionManager().isAuthenticated(player)) {
                        if (
                            plugin
                                .getDatabaseManager()
                                .isRegistered(player.getUniqueId().toString())
                        ) {
                            player.sendMessage(
                                plugin
                                    .getMessageManager()
                                    .getMessage("reminder_login")
                            );
                        } else {
                            player.sendMessage(
                                plugin
                                    .getMessageManager()
                                    .getMessage("reminder_register")
                            );
                        }
                    }
                }
            }
        }
            .runTaskTimer(plugin, 0L, interval / 50)
            .getTaskId();
    }

    public void stopReminders() {
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
}
