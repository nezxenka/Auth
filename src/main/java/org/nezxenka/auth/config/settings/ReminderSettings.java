package org.nezxenka.auth.config.settings;

import org.bukkit.configuration.ConfigurationSection;

public record ReminderSettings(boolean enabled, long intervalMillis) {

    private static final long MILLIS_PER_TICK = 50L;
    private static final long MIN_INTERVAL_TICKS = 20L;

    public static ReminderSettings from(ConfigurationSection section) {
        return new ReminderSettings(
            section.getBoolean("enabled", true),
            section.getLong("interval", 5000L)
        );
    }

    public long intervalTicks() {
        return Math.max(MIN_INTERVAL_TICKS, intervalMillis / MILLIS_PER_TICK);
    }
}
