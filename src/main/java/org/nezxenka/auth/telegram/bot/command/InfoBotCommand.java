package org.nezxenka.auth.telegram.bot.command;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.nezxenka.auth.account.Account;
import org.nezxenka.auth.account.AccountService;
import org.nezxenka.auth.message.Placeholder;
import org.nezxenka.auth.session.SessionService;
import org.nezxenka.auth.telegram.TelegramText;
import org.nezxenka.auth.telegram.bot.BotCommand;
import org.nezxenka.auth.telegram.bot.BotContext;
import org.nezxenka.auth.telegram.bot.BotToolkit;

@RequiredArgsConstructor
public final class InfoBotCommand implements BotCommand {

    private final BotToolkit toolkit;
    private final AccountService accounts;
    private final SessionService sessions;

    @Override
    public List<String> aliases() {
        return List.of("info");
    }

    @Override
    public void handle(BotContext context) {
        accounts
            .findByTelegramId(context.userId())
            .thenAccept(found -> {
                if (found.isEmpty()) {
                    toolkit.reply(context, TelegramText.INFO_NOT_LINKED);
                    return;
                }
                Account account = found.get();
                TelegramText status = sessions.isOnline(account.getUuid())
                    ? TelegramText.INFO_ONLINE
                    : TelegramText.INFO_OFFLINE;
                toolkit.reply(
                    context,
                    toolkit.keyboards().main(),
                    TelegramText.INFO_MESSAGE,
                    Placeholder.of("nickname", account.getUsername()),
                    Placeholder.of("status", toolkit.texts().render(status))
                );
            })
            .exceptionally(error -> toolkit.fail(context, error));
    }
}
