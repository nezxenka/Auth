package org.nezxenka.auth.config.settings;

import org.bukkit.configuration.ConfigurationSection;
import org.nezxenka.auth.config.ConfigSections;

public record PluginSettings(
    String language,
    SecuritySettings security,
    ReminderSettings reminders,
    RestrictionSettings restrictions,
    TelegramSettings telegram
) {

    public static PluginSettings from(ConfigurationSection root) {
        return new PluginSettings(
            root.getString("language", "en"),
            SecuritySettings.from(ConfigSections.child(root, "security")),
            ReminderSettings.from(ConfigSections.child(root, "reminders")),
            RestrictionSettings.from(ConfigSections.child(root, "restrictions")),
            TelegramSettings.from(ConfigSections.child(root, "telegram"))
        );
    }
}
