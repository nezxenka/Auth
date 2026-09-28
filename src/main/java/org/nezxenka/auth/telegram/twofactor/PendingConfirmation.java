package org.nezxenka.auth.telegram.twofactor;

import java.util.UUID;

public record PendingConfirmation(UUID uuid, String name, long telegramId) {
}
