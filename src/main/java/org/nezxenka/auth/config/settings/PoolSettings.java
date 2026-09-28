package org.nezxenka.auth.config.settings;

import org.bukkit.configuration.ConfigurationSection;

public record PoolSettings(
    int maxPoolSize,
    int minIdle,
    long connectionTimeout,
    long idleTimeout,
    long maxLifetime
) {

    public static PoolSettings from(ConfigurationSection section) {
        int maxPoolSize = Math.max(1, section.getInt("max-pool-size", 10));
        return new PoolSettings(
            maxPoolSize,
            Math.min(maxPoolSize, Math.max(0, section.getInt("min-idle", 2))),
            section.getLong("connection-timeout", 5000L),
            section.getLong("idle-timeout", 600000L),
            section.getLong("max-lifetime", 1800000L)
        );
    }
}
