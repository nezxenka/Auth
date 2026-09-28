package org.nezxenka.auth.config.settings;

import java.util.concurrent.TimeUnit;
import org.bukkit.configuration.ConfigurationSection;

public record IpSessionSettings(boolean enabled, long lifetimeSeconds) {

    public static IpSessionSettings from(ConfigurationSection section) {
        return new IpSessionSettings(
            section.getBoolean("enabled", true),
            Math.max(0L, section.getLong("lifetime", 21600L))
        );
    }

    public long lifetimeMillis() {
        return TimeUnit.SECONDS.toMillis(lifetimeSeconds);
    }
}
