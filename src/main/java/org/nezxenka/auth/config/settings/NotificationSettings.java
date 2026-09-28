package org.nezxenka.auth.config.settings;

import org.bukkit.configuration.ConfigurationSection;
import org.nezxenka.auth.config.ConfigSections;
import org.nezxenka.auth.telegram.TelegramText;

public record NotificationSettings(NotificationTemplate join, NotificationTemplate leave) {

    public static NotificationSettings from(ConfigurationSection section) {
        return new NotificationSettings(
            NotificationTemplate.from(ConfigSections.child(section, "join"), TelegramText.NOTIFICATION_JOIN),
            NotificationTemplate.from(ConfigSections.child(section, "leave"), TelegramText.NOTIFICATION_LEAVE)
        );
    }
}
