package org.nezxenka.auth.config.settings;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.nezxenka.auth.config.ConfigSections;

public record MySqlSettings(
    String host,
    int port,
    String database,
    String username,
    String password,
    boolean useSsl,
    PoolSettings pool,
    Map<String, String> properties
) {

    public static MySqlSettings from(ConfigurationSection section) {
        return new MySqlSettings(
            section.getString("host", "localhost"),
            section.getInt("port", 3306),
            section.getString("database", "auth"),
            section.getString("username", "root"),
            section.getString("password", ""),
            section.getBoolean("useSSL", false),
            PoolSettings.from(ConfigSections.child(section, "pool")),
            loadProperties(ConfigSections.child(section, "properties"))
        );
    }

    private static Map<String, String> loadProperties(ConfigurationSection section) {
        Map<String, String> properties = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            properties.put(key, String.valueOf(section.get(key)));
        }
        return Collections.unmodifiableMap(properties);
    }

    public String jdbcUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + database;
    }
}
