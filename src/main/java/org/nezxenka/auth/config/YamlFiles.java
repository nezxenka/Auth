package org.nezxenka.auth.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

@UtilityClass
public class YamlFiles {

    public YamlConfiguration load(JavaPlugin plugin, String name) {
        saveDefault(plugin, name);
        YamlConfiguration configuration = read(new File(plugin.getDataFolder(), name));
        resource(plugin, name).ifPresent(configuration::setDefaults);
        return configuration;
    }

    public void saveDefault(JavaPlugin plugin, String name) {
        if (!new File(plugin.getDataFolder(), name).exists()) {
            plugin.saveResource(name, false);
        }
    }

    public YamlConfiguration read(File file) {
        YamlConfiguration configuration = new YamlConfiguration();
        try {
            configuration.load(file);
        } catch (IOException | InvalidConfigurationException exception) {
            throw new IllegalStateException("Failed to load " + file.getPath(), exception);
        }
        return configuration;
    }

    public Optional<YamlConfiguration> resource(JavaPlugin plugin, String name) {
        InputStream stream = plugin.getResource(name);
        if (stream == null) {
            return Optional.empty();
        }
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return Optional.of(YamlConfiguration.loadConfiguration(reader));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read bundled " + name, exception);
        }
    }
}
