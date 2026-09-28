package org.nezxenka.auth.config;

import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.nezxenka.auth.config.settings.DatabaseSettings;
import org.nezxenka.auth.config.settings.PluginSettings;

@RequiredArgsConstructor
public final class ConfigService {

    private static final String CONFIG_FILE = "config.yml";
    private static final String DATABASE_FILE = "databases.yml";

    private final JavaPlugin plugin;
    private volatile PluginSettings settings;
    private volatile DatabaseSettings database;

    public void load() {
        YamlConfiguration configuration = YamlFiles.load(plugin, CONFIG_FILE);
        YamlConfiguration databases = YamlFiles.load(plugin, DATABASE_FILE);
        PluginSettings loadedSettings = PluginSettings.from(configuration);
        DatabaseSettings loadedDatabase = DatabaseSettings.from(ConfigSections.child(databases, "database"));
        settings = loadedSettings;
        if (database == null) {
            database = loadedDatabase;
        } else if (!database.equals(loadedDatabase)) {
            plugin.getLogger().warning("Database settings were changed, restart the server to apply them");
        }
    }

    public PluginSettings settings() {
        return settings;
    }

    public DatabaseSettings database() {
        return database;
    }
}
