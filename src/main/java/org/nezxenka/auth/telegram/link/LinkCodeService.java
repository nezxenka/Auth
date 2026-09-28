package org.nezxenka.auth.telegram.link;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.nezxenka.auth.config.ConfigService;
import org.nezxenka.auth.config.settings.TelegramSettings;

@RequiredArgsConstructor
public final class LinkCodeService {

    private static final long MAX_PENDING_LINKS = 10_000L;
    private static final int DIGITS = 10;

    private final ConfigService config;
    private final SecureRandom random = new SecureRandom();
    private final Cache<UUID, PendingLink> pending = Caffeine.newBuilder()
        .maximumSize(MAX_PENDING_LINKS)
        .expireAfter(new PendingLinkExpiry())
        .build();

    public String create(UUID uuid, long telegramId) {
        TelegramSettings settings = config.settings().telegram();
        String code = generate(settings.linkCodeLength());
        pending.put(uuid, new PendingLink(code, telegramId, Duration.ofSeconds(settings.linkCodeExpirySeconds())));
        return code;
    }

    public Optional<PendingLink> find(UUID uuid) {
        return Optional.ofNullable(pending.getIfPresent(uuid));
    }

    public void invalidate(UUID uuid) {
        pending.invalidate(uuid);
    }

    private String generate(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(DIGITS));
        }
        return code.toString();
    }
}
