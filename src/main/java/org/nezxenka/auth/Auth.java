package org.nezxenka.auth;

import org.bukkit.plugin.java.JavaPlugin;
import org.nezxenka.auth.commands.*;
import org.nezxenka.auth.data.*;
import org.nezxenka.auth.data.impl.*;
import org.nezxenka.auth.listeners.*;
import org.nezxenka.auth.managers.*;
import org.nezxenka.auth.security.*;
import org.nezxenka.auth.telegram.*;

public final class Auth extends JavaPlugin {

    private static Auth instance;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private DatabaseConfig databaseConfig;
    private DatabaseManager databaseManager;
    private SessionManager sessionManager;
    private PasswordHasher passwordHasher;
    private ReminderManager reminderManager;
    private TelegramLinkManager telegramLinkManager;
    private TelegramBot telegramBot;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultResources();
        initializeManagers();
        registerCommands();
        registerListeners();
        startReminders();
        startTelegramBot();
        getLogger().info("Auth plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (telegramBot != null) telegramBot.stop();
        if (reminderManager != null) reminderManager.stopReminders();
        if (sessionManager != null) sessionManager.invalidateAll();
        if (databaseManager != null) databaseManager.shutdown();
        getLogger().info("Auth plugin disabled!");
    }

    private void saveDefaultResources() {
        saveDefaultConfig();
        if (!new java.io.File(getDataFolder(), "messages.yml").exists()) {
            saveResource("messages.yml", false);
        }
        if (!new java.io.File(getDataFolder(), "databases.yml").exists()) {
            saveResource("databases.yml", false);
        }
    }

    private void initializeManagers() {
        configManager = new ConfigManager();
        messageManager = new MessageManager();
        databaseConfig = new DatabaseConfig();
        passwordHasher = new PasswordHasher();
        sessionManager = new SessionManager();
        reminderManager = new ReminderManager();
        telegramLinkManager = new TelegramLinkManager();
        initDatabase();
    }

    private void initDatabase() {
        String type = databaseConfig.getType();
        if (type.equals("MYSQL")) {
            databaseManager = new MySQLManager(databaseConfig);
        } else {
            databaseManager = new SQLiteManager(databaseConfig);
        }
        databaseManager.createTable();
        databaseManager.createTelegramTable();
    }

    private void registerCommands() {
        getCommand("login").setExecutor(new LoginCommand());
        getCommand("register").setExecutor(new RegisterCommand());
        getCommand("changepass").setExecutor(new ChangePassCommand());
        getCommand("auth").setExecutor(new AuthAdminCommand());
        getCommand("adminchangepass").setExecutor(new AdminChangePassCommand());
        getCommand("link").setExecutor(new LinkCommand());
    }

    private void registerListeners() {
        getServer()
            .getPluginManager()
            .registerEvents(new PlayerListener(), this);
        getServer()
            .getPluginManager()
            .registerEvents(new BlockListener(), this);
    }

    private void startReminders() {
        if (reminderManager != null) {
            reminderManager.startReminders();
        }
    }

    private void startTelegramBot() {
        if (!getConfig().getBoolean("telegram.enabled", false)) {
            getLogger().info("Telegram integration is disabled.");
            return;
        }
        String botToken = getConfig().getString("telegram.bot-token", "");
        if (!botToken.isEmpty() && !botToken.equals("YOUR_BOT_TOKEN_HERE")) {
            telegramBot = new TelegramBot(botToken);
            telegramBot.start();
        } else {
            getLogger().warning("Telegram bot token not configured!");
        }
    }

    public void reloadPlugin() {
        if (telegramBot != null) telegramBot.stop();
        if (reminderManager != null) reminderManager.stopReminders();
        reloadConfig();
        configManager.load(getConfig());
        messageManager.reload();
        databaseConfig.load();
        if (databaseManager != null) databaseManager.shutdown();
        initDatabase();
        sessionManager.invalidateAll();
        startReminders();
        startTelegramBot();
    }

    public static Auth getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public DatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public PasswordHasher getPasswordHasher() {
        return passwordHasher;
    }

    public TelegramLinkManager getTelegramLinkManager() {
        return telegramLinkManager;
    }

    public TelegramBot getTelegramBot() {
        return telegramBot;
    }
}
