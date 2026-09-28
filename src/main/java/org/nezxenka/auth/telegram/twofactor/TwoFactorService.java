package org.nezxenka.auth.telegram.twofactor;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TwoFactorService {

    private final Map<UUID, PendingConfirmation> pending = new ConcurrentHashMap<>();

    public void register(PendingConfirmation confirmation) {
        pending.put(confirmation.uuid(), confirmation);
    }

    public Optional<PendingConfirmation> complete(long telegramId) {
        for (PendingConfirmation confirmation : pending.values()) {
            if (confirmation.telegramId() == telegramId && pending.remove(confirmation.uuid(), confirmation)) {
                return Optional.of(confirmation);
            }
        }
        return Optional.empty();
    }

    public Optional<PendingConfirmation> cancel(UUID uuid) {
        return Optional.ofNullable(pending.remove(uuid));
    }
}
