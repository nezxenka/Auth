package org.nezxenka.auth.telegram.link;

import java.util.UUID;

public interface TelegramLinkRepository {

    boolean isTelegramLinked(long telegramId);

    boolean link(UUID uuid, String username, long telegramId);

    void unlink(UUID uuid);
}
