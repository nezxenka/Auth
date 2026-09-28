package org.nezxenka.auth.config.settings;

import java.util.concurrent.TimeUnit;
import org.bukkit.configuration.ConfigurationSection;
import org.nezxenka.auth.config.ConfigSections;

public record SecuritySettings(
    PasswordSettings password,
    IpSessionSettings ipSession,
    long sessionTimeoutSeconds,
    int maxLoginAttempts,
    long loginTimeoutSeconds,
    boolean singleSession
) {

    public static SecuritySettings from(ConfigurationSection section) {
        return new SecuritySettings(
            PasswordSettings.from(ConfigSections.child(section, "password")),
            IpSessionSettings.from(ConfigSections.child(section, "ip-session")),
            section.getLong("session.timeout", 300L),
            Math.max(1, section.getInt("max-login-attempts", 3)),
            section.getLong("login-timeout", 60L),
            section.getBoolean("single-session", true)
        );
    }

    public boolean sessionTimeoutEnabled() {
        return sessionTimeoutSeconds > 0;
    }

    public long sessionTimeoutMillis() {
        return TimeUnit.SECONDS.toMillis(sessionTimeoutSeconds);
    }

    public boolean loginTimeoutEnabled() {
        return loginTimeoutSeconds > 0;
    }

    public long loginTimeoutMillis() {
        return TimeUnit.SECONDS.toMillis(loginTimeoutSeconds);
    }
}
