package org.nezxenka.auth.config.settings;

import org.bukkit.configuration.ConfigurationSection;
import org.nezxenka.auth.telegram.TelegramText;

public record ButtonSettings(TelegramText text, String callback) {

    public static ButtonSettings from(ConfigurationSection section, TelegramText text, String defaultCallback) {
        return new ButtonSettings(text, section.getString("callback", defaultCallback));
    }
}
