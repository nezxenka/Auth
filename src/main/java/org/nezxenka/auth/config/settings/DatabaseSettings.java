package org.nezxenka.auth.config.settings;

import org.bukkit.configuration.ConfigurationSection;
import org.nezxenka.auth.config.ConfigSections;
import org.nezxenka.auth.database.DatabaseType;

public record DatabaseSettings(DatabaseType type, MySqlSettings mysql, SqliteSettings sqlite) {

    public static DatabaseSettings from(ConfigurationSection section) {
        return new DatabaseSettings(
            DatabaseType.parse(section.getString("type", "SQLite")),
            MySqlSettings.from(ConfigSections.child(section, "mysql")),
            SqliteSettings.from(ConfigSections.child(section, "sqlite"))
        );
    }

    public int threads() {
        return type == DatabaseType.MYSQL ? mysql.pool().maxPoolSize() : 1;
    }
}
