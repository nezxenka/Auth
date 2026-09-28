package org.nezxenka.auth.telegram.link;

import java.time.Duration;

public record PendingLink(String code, long telegramId, Duration ttl) {
}
