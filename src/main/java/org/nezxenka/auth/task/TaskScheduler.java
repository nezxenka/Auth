package org.nezxenka.auth.task;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.nezxenka.auth.config.ConfigService;
import org.nezxenka.auth.config.settings.ReminderSettings;

@RequiredArgsConstructor
public final class TaskScheduler {

    private static final long WATCHDOG_PERIOD_TICKS = 20L;

    private final Plugin plugin;
    private final ConfigService config;
    private final Runnable reminder;
    private final Runnable watchdog;
    private final List<BukkitTask> tasks = new ArrayList<>();

    public synchronized void start() {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        tasks.add(scheduler.runTaskTimer(plugin, watchdog, WATCHDOG_PERIOD_TICKS, WATCHDOG_PERIOD_TICKS));
        ReminderSettings reminders = config.settings().reminders();
        if (reminders.enabled()) {
            long interval = reminders.intervalTicks();
            tasks.add(scheduler.runTaskTimer(plugin, reminder, interval, interval));
        }
    }

    public synchronized void stop() {
        tasks.forEach(BukkitTask::cancel);
        tasks.clear();
    }

    public void restart() {
        stop();
        start();
    }
}
