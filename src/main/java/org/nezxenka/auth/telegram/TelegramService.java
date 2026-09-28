package org.nezxenka.auth.telegram;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.nezxenka.auth.account.Account;
import org.nezxenka.auth.account.AccountService;
import org.nezxenka.auth.concurrent.MainThreadExecutor;
import org.nezxenka.auth.config.ConfigService;
import org.nezxenka.auth.config.settings.NotificationTemplate;
import org.nezxenka.auth.config.settings.TelegramSettings;
import org.nezxenka.auth.message.MessageKey;
import org.nezxenka.auth.message.MessageService;
import org.nezxenka.auth.message.Placeholder;
import org.nezxenka.auth.service.AuthenticationService;
import org.nezxenka.auth.session.AuthState;
import org.nezxenka.auth.session.PlayerSession;
import org.nezxenka.auth.session.SessionService;
import org.nezxenka.auth.telegram.api.TelegramApiClient;
import org.nezxenka.auth.telegram.api.TelegramMessenger;
import org.nezxenka.auth.telegram.bot.BotAction;
import org.nezxenka.auth.telegram.bot.BotToolkit;
import org.nezxenka.auth.telegram.bot.CallbackType;
import org.nezxenka.auth.telegram.bot.TelegramBot;
import org.nezxenka.auth.telegram.bot.UpdateDispatcher;
import org.nezxenka.auth.telegram.bot.callback.ApproveTwoFactorAction;
import org.nezxenka.auth.telegram.bot.callback.DeclineTwoFactorAction;
import org.nezxenka.auth.telegram.bot.command.InfoBotCommand;
import org.nezxenka.auth.telegram.bot.command.KeyboardBotCommand;
import org.nezxenka.auth.telegram.bot.command.LinkBotCommand;
import org.nezxenka.auth.telegram.bot.command.StartBotCommand;
import org.nezxenka.auth.telegram.bot.command.UnlinkBotCommand;
import org.nezxenka.auth.telegram.link.LinkCodeService;
import org.nezxenka.auth.telegram.link.TelegramLinkService;
import org.nezxenka.auth.telegram.twofactor.PendingConfirmation;
import org.nezxenka.auth.telegram.twofactor.TwoFactorService;

public final class TelegramService {

    private final Logger logger;
    private final ConfigService config;
    private final MessageService messages;
    private final SessionService sessions;
    private final AccountService accounts;
    private final TelegramLinkService links;
    private final LinkCodeService linkCodes;
    private final AuthenticationService authentication;
    private final MainThreadExecutor mainThread;
    private final TwoFactorService twoFactor = new TwoFactorService();
    private final TelegramTexts texts;
    private final KeyboardFactory keyboards;

    private volatile TelegramBot bot;
    private volatile TelegramMessenger messenger;
    private long offset;

    public TelegramService(
        Logger logger,
        ConfigService config,
        MessageService messages,
        SessionService sessions,
        AccountService accounts,
        TelegramLinkService links,
        LinkCodeService linkCodes,
        AuthenticationService authentication,
        MainThreadExecutor mainThread
    ) {
        this.logger = logger;
        this.config = config;
        this.messages = messages;
        this.sessions = sessions;
        this.accounts = accounts;
        this.links = links;
        this.linkCodes = linkCodes;
        this.authentication = authentication;
        this.mainThread = mainThread;
        this.texts = new TelegramTexts(messages);
        this.keyboards = new KeyboardFactory(config, texts);
    }

    public synchronized void start() {
        TelegramSettings settings = config.settings().telegram();
        if (!settings.enabled()) {
            logger.info("Telegram integration is disabled.");
            return;
        }
        if (!settings.hasValidToken()) {
            logger.warning("Telegram bot token not configured!");
            return;
        }
        TelegramApiClient api = new TelegramApiClient(settings.botToken(), logger);
        TelegramMessenger createdMessenger = new TelegramMessenger(api, logger);
        BotToolkit toolkit = new BotToolkit(createdMessenger, texts, keyboards, logger);
        UnlinkBotCommand unlink = new UnlinkBotCommand(toolkit, accounts, links, this::handleUnlinked);
        InfoBotCommand info = new InfoBotCommand(toolkit, accounts, sessions);
        UpdateDispatcher dispatcher = new UpdateDispatcher(
            logger,
            config,
            createdMessenger,
            List.of(
                new StartBotCommand(toolkit),
                new LinkBotCommand(toolkit, accounts, links, linkCodes),
                unlink,
                info,
                new KeyboardBotCommand(toolkit)
            ),
            Map.<CallbackType, BotAction>of(
                CallbackType.APPROVE, new ApproveTwoFactorAction(toolkit, twoFactor, this::handleApproved),
                CallbackType.DECLINE, new DeclineTwoFactorAction(toolkit, twoFactor, this::handleDeclined),
                CallbackType.UNLINK, unlink,
                CallbackType.INFO, info
            )
        );
        TelegramBot created = new TelegramBot(api, dispatcher, logger, offset);
        messenger = createdMessenger;
        bot = created;
        created.start();
    }

    public synchronized void stop() {
        TelegramBot current = bot;
        if (current == null) {
            return;
        }
        bot = null;
        messenger = null;
        current.stop();
        offset = current.getOffset();
    }

    public void restart() {
        stop();
        start();
    }

    public boolean isRunning() {
        return bot != null;
    }

    public boolean requiresTwoFactor(Account account) {
        return isRunning() && account.isTelegramLinked() && config.settings().telegram().twoFactor().enabled();
    }

    public void requestTwoFactor(PlayerSession session) {
        Account account = session.getAccount();
        TelegramMessenger current = messenger;
        if (account == null || current == null) {
            return;
        }
        twoFactor.register(new PendingConfirmation(session.getUuid(), session.getName(), account.getTelegramId()));
        current.send(
            account.getTelegramId(),
            texts.render(
                TelegramText.TWO_FACTOR_REQUEST,
                Placeholder.of("nickname", session.getName()),
                Placeholder.of("ip", session.getIp())
            ),
            keyboards.twoFactor()
        );
    }

    public void expireTwoFactor(UUID uuid) {
        twoFactor.cancel(uuid).ifPresent(confirmation -> send(confirmation.telegramId(), texts.render(TelegramText.TWO_FACTOR_KICK)));
    }

    public void cancelTwoFactor(UUID uuid) {
        twoFactor.cancel(uuid);
    }

    public void notifyJoin(PlayerSession session) {
        notify(session, config.settings().telegram().notifications().join());
    }

    public void notifyLeave(PlayerSession session) {
        notify(session, config.settings().telegram().notifications().leave());
    }

    public void notifyLinked(long telegramId) {
        TelegramMessenger current = messenger;
        if (current != null) {
            current.send(telegramId, texts.render(TelegramText.LINK_SUCCESS), keyboards.main());
        }
    }

    private void notify(PlayerSession session, NotificationTemplate template) {
        Account account = session.getAccount();
        if (!template.enabled() || account == null || !account.isTelegramLinked()) {
            return;
        }
        send(
            account.getTelegramId(),
            texts.render(
                template.text(),
                Placeholder.of("nickname", session.getName()),
                Placeholder.of("ip", session.getIp())
            )
        );
    }

    private void send(long chatId, String text) {
        TelegramMessenger current = messenger;
        if (current != null) {
            current.send(chatId, text);
        }
    }

    private void handleApproved(PendingConfirmation confirmation) {
        mainThread.execute(() -> {
            Player player = Bukkit.getPlayer(confirmation.uuid());
            PlayerSession session = sessions.get(confirmation.uuid());
            if (player == null || session == null || session.getState() != AuthState.PENDING_TWO_FACTOR) {
                return;
            }
            authentication.authorize(player, session, MessageKey.LOGIN_SUCCESS, true);
            notifyJoin(session);
        });
    }

    private void handleDeclined(PendingConfirmation confirmation) {
        mainThread.execute(() -> {
            Player player = Bukkit.getPlayer(confirmation.uuid());
            if (player != null) {
                player.kickPlayer(texts.render(TelegramText.TWO_FACTOR_DECLINED));
            }
        });
    }

    private void handleUnlinked(UUID uuid) {
        twoFactor.cancel(uuid);
        mainThread.execute(() -> {
            Player player = Bukkit.getPlayer(uuid);
            PlayerSession session = sessions.get(uuid);
            if (player == null || session == null || session.getState() != AuthState.PENDING_TWO_FACTOR) {
                return;
            }
            session.transition(AuthState.UNAUTHENTICATED);
            messages.send(player, MessageKey.NOT_LOGGED_IN);
        });
    }
}
