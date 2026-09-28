package org.nezxenka.auth.config.settings;

import org.bukkit.configuration.ConfigurationSection;

public record SqliteSettings(String filename) {

    public static SqliteSettings from(ConfigurationSection section) {
        return new SqliteSettings(section.getString("filename", "auth.db"));
    }
}
