package org.nezxenka.auth.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.nezxenka.auth.account.Account;
import org.nezxenka.auth.concurrent.MainThreadExecutor;
import org.nezxenka.auth.config.ConfigService;
import org.nezxenka.auth.message.MessageKey;
import org.nezxenka.auth.message.MessageService;
import org.nezxenka.auth.session.PlayerSession;
import org.nezxenka.auth.session.SessionService;
import org.nezxenka.auth.telegram.TelegramService;
import org.nezxenka.auth.telegram.link.LinkCodeService;
import org.nezxenka.auth.telegram.link.PendingLink;
import org.nezxenka.auth.telegram.link.TelegramLinkService;

@RequiredArgsConstructor
public final class LinkConfirmationService {

    private final ConfigService config;
    private final MessageService messages;
    private final SessionService sessions;
    private final TelegramLinkService links;
    private final LinkCodeService linkCodes;
    private final TelegramService telegram;
    private final MainThreadExecutor mainThread;
    private final FailureHandler failures;

    public void confirm(Player player, String code) {
        PlayerSession session = sessions.get(player.getUniqueId());
        if (session == null || !session.isAuthenticated()) {
            messages.send(player, MessageKey.NOT_LOGGED_IN);
            return;
        }
        Account account = session.getAccount();
        if (account == null) {
            messages.send(player, MessageKey.NOT_REGISTERED);
            return;
        }
        if (account.isTelegramLinked()) {
            messages.send(player, MessageKey.TELEGRAM_ALREADY_LINKED);
            return;
        }
        if (!telegram.isRunning()) {
            messages.send(player, MessageKey.TELEGRAM_DISABLED);
            return;
        }
        if (code == null) {
            messages.send(player, MessageKey.TELEGRAM_LINK_USAGE);
            messages.send(player, MessageKey.TELEGRAM_LINK_HINT);
            return;
        }
        Optional<PendingLink> pending = linkCodes.find(session.getUuid());
        if (pending.isEmpty()) {
            messages.send(player, MessageKey.TELEGRAM_NO_PENDING);
            return;
        }
        if (!pending.get().code().equals(code)) {
            messages.send(player, MessageKey.TELEGRAM_INVALID_CODE);
            return;
        }
        if (!session.tryAcquire()) {
            messages.send(player, MessageKey.PROCESSING);
            return;
        }
        long telegramId = pending.get().telegramId();
        links
            .link(session.getUuid(), session.getName(), telegramId)
            .thenAcceptAsync(linked -> {
                if (!linked) {
                    messages.send(player, MessageKey.TELEGRAM_ID_TAKEN);
                    return;
                }
                linkCodes.invalidate(session.getUuid());
                boolean twoFactor = config.settings().telegram().twoFactor().enabled();
                messages.send(player, twoFactor ? MessageKey.TELEGRAM_LINKED_TWO_FACTOR : MessageKey.TELEGRAM_LINKED);
                telegram.notifyLinked(telegramId);
            }, mainThread)
            .whenComplete((ignored, error) -> {
                session.release();
                if (error != null) {
                    failures.handle(player, "Failed to link Telegram of " + session.getName(), error);
                }
            });
    }
}
