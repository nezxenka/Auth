package org.nezxenka.auth.telegram.bot.callback;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.nezxenka.auth.telegram.TelegramText;
import org.nezxenka.auth.telegram.bot.BotAction;
import org.nezxenka.auth.telegram.bot.BotContext;
import org.nezxenka.auth.telegram.bot.BotToolkit;
import org.nezxenka.auth.telegram.twofactor.PendingConfirmation;
import org.nezxenka.auth.telegram.twofactor.TwoFactorService;

@RequiredArgsConstructor
public final class ApproveTwoFactorAction implements BotAction {

    private final BotToolkit toolkit;
    private final TwoFactorService twoFactor;
    private final Consumer<PendingConfirmation> onApproved;

    @Override
    public void handle(BotContext context) {
        twoFactor.complete(context.userId()).ifPresent(confirmation -> {
            toolkit.reply(context, TelegramText.TWO_FACTOR_APPROVED);
            onApproved.accept(confirmation);
        });
    }
}
