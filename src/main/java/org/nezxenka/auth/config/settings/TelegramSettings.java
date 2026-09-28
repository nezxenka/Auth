package org.nezxenka.auth.config.settings;

import org.bukkit.configuration.ConfigurationSection;
import org.nezxenka.auth.config.ConfigSections;

public record TelegramSettings(
    boolean enabled,
    String botToken,
    int linkCodeLength,
    long linkCodeExpirySeconds,
    TwoFactorSettings twoFactor,
    NotificationSettings notifications,
    KeyboardSettings keyboard
) {

    private static final String TOKEN_PLACEHOLDER = "YOUR_BOT_TOKEN_HERE";
    private static final int MIN_CODE_LENGTH = 4;
    private static final int MAX_CODE_LENGTH = 16;
    private static final long MIN_CODE_EXPIRY_SECONDS = 30L;

    public static TelegramSettings from(ConfigurationSection section) {
        return new TelegramSettings(
            section.getBoolean("enabled", false),
            section.getString("bot-token", ""),
            Math.min(MAX_CODE_LENGTH, Math.max(MIN_CODE_LENGTH, section.getInt("link-code-length", 8))),
            Math.max(MIN_CODE_EXPIRY_SECONDS, section.getLong("link-code-expiry", 300L)),
            TwoFactorSettings.from(ConfigSections.child(section, "2fa")),
            NotificationSettings.from(ConfigSections.child(section, "notifications")),
            KeyboardSettings.from(ConfigSections.child(section, "keyboard"))
        );
    }

    public boolean hasValidToken() {
        return botToken != null && !botToken.isBlank() && !TOKEN_PLACEHOLDER.equals(botToken);
    }
}
