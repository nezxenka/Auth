package org.nezxenka.auth.telegram.bot.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.nezxenka.auth.telegram.TelegramText;
import org.nezxenka.auth.telegram.bot.BotCommand;
import org.nezxenka.auth.telegram.bot.BotContext;
import org.nezxenka.auth.telegram.bot.BotToolkit;

@RequiredArgsConstructor
public final class KeyboardBotCommand implements BotCommand {

    private final BotToolkit toolkit;

    @Override
    public List<String> aliases() {
        return List.of("keyboard", "клавиатура");
    }

    @Override
    public void handle(BotContext context) {
        toolkit.reply(context, toolkit.keyboards().main(), TelegramText.START);
    }
}
