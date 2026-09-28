package org.nezxenka.auth.config.settings;

import java.util.concurrent.TimeUnit;
import org.bukkit.configuration.ConfigurationSection;

public record TwoFactorSettings(boolean enabled, long timeoutSeconds) {

    public static TwoFactorSettings from(ConfigurationSection section) {
        return new TwoFactorSettings(
            section.getBoolean("enabled", true),
            Math.max(10L, section.getLong("timeout", 60L))
        );
    }

    public long timeoutMillis() {
        return TimeUnit.SECONDS.toMillis(timeoutSeconds);
    }
}
