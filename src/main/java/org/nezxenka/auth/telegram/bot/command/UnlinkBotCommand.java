package org.nezxenka.auth.telegram.bot.command;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.nezxenka.auth.account.AccountService;
import org.nezxenka.auth.telegram.TelegramText;
import org.nezxenka.auth.telegram.bot.BotCommand;
import org.nezxenka.auth.telegram.bot.BotContext;
import org.nezxenka.auth.telegram.bot.BotToolkit;
import org.nezxenka.auth.telegram.link.TelegramLinkService;

@RequiredArgsConstructor
public final class UnlinkBotCommand implements BotCommand {

    private final BotToolkit toolkit;
    private final AccountService accounts;
    private final TelegramLinkService links;
    private final Consumer<UUID> onUnlinked;

    @Override
    public List<String> aliases() {
        return List.of("отвязать", "unlink");
    }

    @Override
    public void handle(BotContext context) {
        accounts
            .findByTelegramId(context.userId())
            .thenCompose(found -> {
                if (found.isEmpty()) {
                    toolkit.reply(context, TelegramText.UNLINK_NOT_LINKED);
                    return CompletableFuture.<Void>completedFuture(null);
                }
                UUID uuid = found.get().getUuid();
                return links.unlink(uuid).thenRun(() -> {
                    onUnlinked.accept(uuid);
                    toolkit.reply(context, TelegramText.UNLINK_SUCCESS);
                });
            })
            .exceptionally(error -> toolkit.fail(context, error));
    }
}
