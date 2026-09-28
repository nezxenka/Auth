package org.nezxenka.auth.bootstrap;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.nezxenka.auth.account.AccountService;
import org.nezxenka.auth.account.SqlAccountRepository;
import org.nezxenka.auth.command.CommandRegistry;
import org.nezxenka.auth.command.impl.AdminChangePasswordCommand;
import org.nezxenka.auth.command.impl.AuthAdminCommand;
import org.nezxenka.auth.command.impl.ChangePasswordCommand;
import org.nezxenka.auth.command.impl.LinkCommand;
import org.nezxenka.auth.command.impl.LoginCommand;
import org.nezxenka.auth.command.impl.RegisterCommand;
import org.nezxenka.auth.concurrent.AsyncExecutors;
import org.nezxenka.auth.concurrent.MainThreadExecutor;
import org.nezxenka.auth.config.ConfigService;
import org.nezxenka.auth.config.settings.DatabaseSettings;
import org.nezxenka.auth.database.Database;
import org.nezxenka.auth.database.SchemaInitializer;
import org.nezxenka.auth.listener.AccessGuard;
import org.nezxenka.auth.listener.ChatListener;
import org.nezxenka.auth.listener.ConnectionListener;
import org.nezxenka.auth.listener.InteractionListener;
import org.nezxenka.auth.listener.InventoryListener;
import org.nezxenka.auth.listener.MovementListener;
import org.nezxenka.auth.listener.PreLoginListener;
import org.nezxenka.auth.listener.WorldListener;
import org.nezxenka.auth.message.MessageService;
import org.nezxenka.auth.security.PasswordPolicy;
import org.nezxenka.auth.security.PasswordService;
import org.nezxenka.auth.service.AuthenticationService;
import org.nezxenka.auth.service.FailureHandler;
import org.nezxenka.auth.service.JoinService;
import org.nezxenka.auth.service.LinkConfirmationService;
import org.nezxenka.auth.service.PasswordChangeService;
import org.nezxenka.auth.service.RegistrationService;
import org.nezxenka.auth.session.SessionService;
import org.nezxenka.auth.task.ReminderTask;
import org.nezxenka.auth.task.SessionWatchdogTask;
import org.nezxenka.auth.task.TaskScheduler;
import org.nezxenka.auth.telegram.TelegramService;
import org.nezxenka.auth.telegram.link.LinkCodeService;
import org.nezxenka.auth.telegram.link.SqlTelegramLinkRepository;
import org.nezxenka.auth.telegram.link.TelegramLinkService;

public final class AuthBootstrap {

    private final JavaPlugin plugin;
    private final Logger logger;

    private ConfigService config;
    private MessageService messages;
    private AsyncExecutors executors;
    private Database database;
    private PasswordService passwords;
    private TelegramService telegram;
    private TaskScheduler tasks;

    public AuthBootstrap(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void enable() {
        config = new ConfigService(plugin);
        config.load();
        messages = new MessageService(plugin);
        messages.load(config.settings().language());
        logger.info("Loaded language: " + messages.language());

        DatabaseSettings databaseSettings = config.database();
        executors = new AsyncExecutors(databaseSettings.threads(), logger);
        MainThreadExecutor mainThread = new MainThreadExecutor(plugin);

        database = Database.connect(databaseSettings, plugin.getDataFolder());
        new SchemaInitializer(database).initialize();
        logger.info("Connected to " + databaseSettings.type() + " database");

        SessionService sessions = new SessionService();
        AccountService accounts = new AccountService(new SqlAccountRepository(database), executors.getDatabase());
        passwords = new PasswordService(config.settings().security().password());
        PasswordPolicy policy = new PasswordPolicy(config);
        FailureHandler failures = new FailureHandler(logger, messages, mainThread);

        AuthenticationService authentication = new AuthenticationService(
            config, messages, sessions, accounts, passwords, executors, mainThread, failures, logger
        );
        RegistrationService registration = new RegistrationService(
            messages, sessions, accounts, passwords, policy, authentication, executors, mainThread, failures
        );
        PasswordChangeService passwordChange = new PasswordChangeService(
            messages, sessions, accounts, passwords, policy, executors, mainThread, failures
        );

        TelegramLinkService links = new TelegramLinkService(
            new SqlTelegramLinkRepository(database), sessions, executors.getDatabase()
        );
        LinkCodeService linkCodes = new LinkCodeService(config);
        telegram = new TelegramService(
            logger, config, messages, sessions, accounts, links, linkCodes, authentication, mainThread
        );
        LinkConfirmationService linkConfirmation = new LinkConfirmationService(
            config, messages, sessions, links, linkCodes, telegram, mainThread, failures
        );
        JoinService join = new JoinService(
            logger, config, messages, sessions, accounts, authentication, telegram, mainThread
        );
        AccessGuard guard = new AccessGuard(sessions, messages);

        CommandRegistry commands = new CommandRegistry(plugin);
        commands.register("login", new LoginCommand(messages, authentication));
        commands.register("register", new RegisterCommand(messages, registration));
        commands.register("changepass", new ChangePasswordCommand(messages, passwordChange));
        commands.register("adminchangepass", new AdminChangePasswordCommand(messages, passwordChange));
        commands.register("auth", new AuthAdminCommand(messages, plugin.getDescription().getVersion(), this::reload));
        commands.register("link", new LinkCommand(messages, linkConfirmation));

        registerListeners(List.of(
            new PreLoginListener(logger, config, messages, sessions, accounts),
            new ConnectionListener(join),
            new MovementListener(config, guard),
            new ChatListener(config, guard),
            new InteractionListener(config, guard),
            new InventoryListener(config, guard),
            new WorldListener(config, guard)
        ));

        tasks = new TaskScheduler(
            plugin,
            config,
            new ReminderTask(messages, sessions),
            new SessionWatchdogTask(config, messages, sessions, telegram)
        );
        tasks.start();
        telegram.start();
        join.restoreOnlinePlayers();
    }

    public void disable() {
        if (telegram != null) {
            telegram.stop();
        }
        if (tasks != null) {
            tasks.stop();
        }
        if (executors != null) {
            executors.shutdown();
        }
        if (database != null) {
            database.close();
        }
    }

    public boolean reload() {
        try {
            config.load();
            messages.load(config.settings().language());
            passwords.configure(config.settings().security().password());
            tasks.restart();
            telegram.restart();
            return true;
        } catch (RuntimeException exception) {
            logger.log(Level.SEVERE, "Failed to reload configuration", exception);
            return false;
        }
    }

    private void registerListeners(List<Listener> listeners) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        for (Listener listener : listeners) {
            pluginManager.registerEvents(listener, plugin);
        }
    }
}
