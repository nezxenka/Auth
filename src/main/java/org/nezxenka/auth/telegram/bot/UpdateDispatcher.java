package org.nezxenka.auth.telegram.bot;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.nezxenka.auth.config.ConfigService;
import org.nezxenka.auth.telegram.api.TelegramMessenger;
import org.nezxenka.auth.telegram.api.model.CallbackQuery;
import org.nezxenka.auth.telegram.api.model.Message;
import org.nezxenka.auth.telegram.api.model.Update;

public final class UpdateDispatcher {

    private static final String COMMAND_PREFIX = "/";
    private static final char MENTION_SEPARATOR = '@';

    private final Logger logger;
    private final ConfigService config;
    private final TelegramMessenger messenger;
    private final Map<String, BotCommand> commands;
    private final Map<CallbackType, BotAction> callbacks;

    public UpdateDispatcher(
        Logger logger,
        ConfigService config,
        TelegramMessenger messenger,
        List<BotCommand> commands,
        Map<CallbackType, BotAction> callbacks
    ) {
        this.logger = logger;
        this.config = config;
        this.messenger = messenger;
        Map<String, BotCommand> byAlias = new HashMap<>();
        for (BotCommand command : commands) {
            for (String alias : command.aliases()) {
                byAlias.put(alias.toLowerCase(Locale.ROOT), command);
            }
        }
        this.commands = Map.copyOf(byAlias);
        this.callbacks = Map.copyOf(callbacks);
    }

    public void dispatch(Update update) {
        try {
            if (update.getCallbackQuery() != null) {
                handleCallback(update.getCallbackQuery());
            } else if (update.getMessage() != null) {
                handleMessage(update.getMessage());
            }
        } catch (RuntimeException exception) {
            logger.log(Level.WARNING, "Failed to handle Telegram update " + update.getUpdateId(), exception);
        }
    }

    private void handleMessage(Message message) {
        String text = message.getText();
        if (text == null || message.getChat() == null) {
            return;
        }
        String[] parts = text.trim().split("\\s+");
        if (!parts[0].startsWith(COMMAND_PREFIX)) {
            return;
        }
        BotCommand command = commands.get(commandName(parts[0]));
        if (command == null) {
            return;
        }
        long chatId = message.getChat().getId();
        long userId = message.getFrom() != null ? message.getFrom().getId() : chatId;
        command.handle(new BotContext(chatId, userId, List.of(parts).subList(1, parts.length)));
    }

    private void handleCallback(CallbackQuery query) {
        messenger.answerCallback(query.getId());
        Message message = query.getMessage();
        if (query.getFrom() == null || query.getData() == null || message == null || message.getChat() == null) {
            return;
        }
        BotContext context = new BotContext(message.getChat().getId(), query.getFrom().getId(), List.of());
        CallbackType
            .resolve(query.getData(), config.settings().telegram().keyboard())
            .map(callbacks::get)
            .ifPresent(action -> action.handle(context));
    }

    private static String commandName(String token) {
        String name = token.substring(COMMAND_PREFIX.length());
        int mention = name.indexOf(MENTION_SEPARATOR);
        if (mention >= 0) {
            name = name.substring(0, mention);
        }
        return name.toLowerCase(Locale.ROOT);
    }
}
