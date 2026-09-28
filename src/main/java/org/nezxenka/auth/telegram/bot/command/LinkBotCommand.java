package org.nezxenka.auth.telegram.bot.command;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.nezxenka.auth.account.Account;
import org.nezxenka.auth.account.AccountService;
import org.nezxenka.auth.message.Placeholder;
import org.nezxenka.auth.telegram.TelegramText;
import org.nezxenka.auth.telegram.bot.BotCommand;
import org.nezxenka.auth.telegram.bot.BotContext;
import org.nezxenka.auth.telegram.bot.BotToolkit;
import org.nezxenka.auth.telegram.link.LinkCodeService;
import org.nezxenka.auth.telegram.link.TelegramLinkService;

@RequiredArgsConstructor
public final class LinkBotCommand implements BotCommand {

    private final BotToolkit toolkit;
    private final AccountService accounts;
    private final TelegramLinkService links;
    private final LinkCodeService linkCodes;

    @Override
    public List<String> aliases() {
        return List.of("привязать", "link");
    }

    @Override
    public void handle(BotContext context) {
        if (context.args().isEmpty()) {
            toolkit.reply(context, toolkit.keyboards().instruction(), TelegramText.LINK_USAGE);
            return;
        }
        String nickname = context.args().get(0);
        accounts
            .findByUsername(nickname)
            .thenCompose(found -> {
                if (found.isEmpty()) {
                    toolkit.reply(context, TelegramText.LINK_PLAYER_NOT_FOUND, Placeholder.of("nickname", nickname));
                    return CompletableFuture.<Void>completedFuture(null);
                }
                Account account = found.get();
                if (account.isTelegramLinked()) {
                    toolkit.reply(context, TelegramText.LINK_ALREADY_LINKED);
                    return CompletableFuture.<Void>completedFuture(null);
                }
                return links.isTelegramLinked(context.userId()).thenAccept(taken -> {
                    if (taken) {
                        toolkit.reply(context, TelegramText.LINK_TELEGRAM_ALREADY_LINKED);
                        return;
                    }
                    String code = linkCodes.create(account.getUuid(), context.userId());
                    toolkit.reply(context, TelegramText.LINK_CODE, Placeholder.of("code", code));
                });
            })
            .exceptionally(error -> toolkit.fail(context, error));
    }
}
