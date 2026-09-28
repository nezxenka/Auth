package org.nezxenka.auth.config.settings;

import org.bukkit.configuration.ConfigurationSection;
import org.nezxenka.auth.telegram.TelegramText;

public record NotificationTemplate(boolean enabled, TelegramText text) {

    public static NotificationTemplate from(ConfigurationSection section, TelegramText text) {
        return new NotificationTemplate(section.getBoolean("enabled", true), text);
    }
}
