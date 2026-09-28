package org.nezxenka.auth.config.settings;

import org.bukkit.configuration.ConfigurationSection;
import org.nezxenka.auth.config.ConfigSections;
import org.nezxenka.auth.telegram.TelegramText;

public record KeyboardSettings(
    ButtonSettings approve,
    ButtonSettings decline,
    ButtonSettings unlink,
    ButtonSettings info,
    ButtonSettings instruction
) {

    public static KeyboardSettings from(ConfigurationSection section) {
        return new KeyboardSettings(
            ButtonSettings.from(ConfigSections.child(section, "approve"), TelegramText.BUTTON_APPROVE, "2fa_approve"),
            ButtonSettings.from(ConfigSections.child(section, "decline"), TelegramText.BUTTON_DECLINE, "2fa_decline"),
            ButtonSettings.from(ConfigSections.child(section, "unlink"), TelegramText.BUTTON_UNLINK, "unlink"),
            ButtonSettings.from(ConfigSections.child(section, "info"), TelegramText.BUTTON_INFO, "info"),
            ButtonSettings.from(ConfigSections.child(section, "instruction"), TelegramText.BUTTON_INSTRUCTION, "info")
        );
    }
}
