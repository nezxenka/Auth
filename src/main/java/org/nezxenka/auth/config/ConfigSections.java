package org.nezxenka.auth.config;

import lombok.experimental.UtilityClass;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

@UtilityClass
public class ConfigSections {

    public ConfigurationSection child(ConfigurationSection parent, String path) {
        ConfigurationSection section = parent.getConfigurationSection(path);
        return section != null ? section : new MemoryConfiguration();
    }
}
