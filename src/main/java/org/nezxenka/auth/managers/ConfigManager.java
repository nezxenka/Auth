package org.nezxenka.auth.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.nezxenka.auth.Auth;

public class ConfigManager {

    private FileConfiguration config;
    private final Auth plugin;

    public ConfigManager() {
        this.plugin = Auth.getInstance();
        this.config = plugin.getConfig();
        setupDefaults();
    }

    public void load(FileConfiguration config) {
        this.config = config;
        setupDefaults();
    }

    private void setupDefaults() {
        config.addDefault("security.password.min-length", 6);
        config.addDefault("security.password.max-length", 30);
        config.addDefault("security.password.hash-algorithm", "SHA-256");
        config.addDefault("security.session.timeout", 300);
        config.addDefault("security.max-login-attempts", 3);
        config.addDefault("security.login-timeout", 60);
        config.addDefault("security.ip-session.enabled", true);
        config.addDefault("security.ip-session.lifetime", 21600);
        config.addDefault("reminders.enabled", true);
        config.addDefault("reminders.interval", 5000);
        config.addDefault("restrictions.block-commands", true);
        config.addDefault("restrictions.block-movement", true);
        config.addDefault("restrictions.block-chat", true);
        config.addDefault("restrictions.block-interact", true);
        config.addDefault("restrictions.block-damage", true);
        config.addDefault("restrictions.block-inventory", true);
        config.addDefault("restrictions.block-drop", true);
        config.addDefault("restrictions.block-pickup", true);
        config.addDefault("restrictions.block-break", true);
        config.addDefault("restrictions.block-place", true);
        config.addDefault("telegram.enabled", false);
        config.addDefault("telegram.bot-token", "YOUR_BOT_TOKEN_HERE");
        config.addDefault("telegram.bot-username", "avannils_bot");
        config.addDefault("telegram.link-code-length", 8);
        config.addDefault("telegram.link-code-expiry", 300);
        config.options().copyDefaults(true);
        plugin.saveConfig();
    }

    public int getMinPasswordLength() {
        return config.getInt("security.password.min-length");
    }

    public int getMaxPasswordLength() {
        return config.getInt("security.password.max-length");
    }

    public String getHashAlgorithm() {
        return config.getString("security.password.hash-algorithm");
    }

    public int getSessionTimeout() {
        return config.getInt("security.session.timeout");
    }

    public int getMaxLoginAttempts() {
        return config.getInt("security.max-login-attempts");
    }

    public int getLoginTimeout() {
        return config.getInt("security.login-timeout");
    }

    public boolean isRemindersEnabled() {
        return config.getBoolean("reminders.enabled");
    }

    public long getRemindersInterval() {
        return config.getLong("reminders.interval");
    }

    public boolean isBlockCommands() {
        return config.getBoolean("restrictions.block-commands");
    }

    public boolean isBlockMovement() {
        return config.getBoolean("restrictions.block-movement");
    }

    public boolean isBlockChat() {
        return config.getBoolean("restrictions.block-chat");
    }

    public boolean isBlockInteract() {
        return config.getBoolean("restrictions.block-interact");
    }

    public boolean isBlockDamage() {
        return config.getBoolean("restrictions.block-damage");
    }

    public boolean isBlockInventory() {
        return config.getBoolean("restrictions.block-inventory");
    }

    public boolean isBlockDrop() {
        return config.getBoolean("restrictions.block-drop");
    }

    public boolean isBlockPickup() {
        return config.getBoolean("restrictions.block-pickup");
    }

    public boolean isBlockBreak() {
        return config.getBoolean("restrictions.block-break");
    }

    public boolean isBlockPlace() {
        return config.getBoolean("restrictions.block-place");
    }

    public boolean isTelegramEnabled() {
        return config.getBoolean("telegram.enabled");
    }

    public String getTelegramBotToken() {
        return config.getString("telegram.bot-token");
    }

    public String getTelegramBotUsername() {
        return config.getString("telegram.bot-username");
    }

    public int getTelegramLinkCodeLength() {
        return config.getInt("telegram.link-code-length");
    }

    public int getTelegramLinkCodeExpiry() {
        return config.getInt("telegram.link-code-expiry");
    }
}
