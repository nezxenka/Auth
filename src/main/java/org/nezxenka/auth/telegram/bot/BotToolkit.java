package org.nezxenka.auth.telegram.bot;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.nezxenka.auth.concurrent.Futures;
import org.nezxenka.auth.message.Placeholder;
import org.nezxenka.auth.telegram.KeyboardFactory;
import org.nezxenka.auth.telegram.TelegramText;
import org.nezxenka.auth.telegram.TelegramTexts;
import org.nezxenka.auth.telegram.api.TelegramMessenger;
import org.nezxenka.auth.telegram.api.model.InlineKeyboardMarkup;

public record BotToolkit(TelegramMessenger messenger, TelegramTexts texts, KeyboardFactory keyboards, Logger logger) {

    public void reply(BotContext context, TelegramText text, Placeholder... placeholders) {
        messenger.send(context.chatId(), texts.render(text, placeholders));
    }

    public void reply(
        BotContext context,
        InlineKeyboardMarkup keyboard,
        TelegramText text,
        Placeholder... placeholders
    ) {
        messenger.send(context.chatId(), texts.render(text, placeholders), keyboard);
    }

    public Void fail(BotContext context, Throwable error) {
        logger.log(Level.WARNING, "Failed to handle Telegram command", Futures.unwrap(error));
        reply(context, TelegramText.ERROR);
        return null;
    }
}
