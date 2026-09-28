package org.nezxenka.auth.telegram.link;

import com.github.benmanes.caffeine.cache.Expiry;
import java.util.UUID;

public final class PendingLinkExpiry implements Expiry<UUID, PendingLink> {

    @Override
    public long expireAfterCreate(UUID key, PendingLink value, long currentTime) {
        return value.ttl().toNanos();
    }

    @Override
    public long expireAfterUpdate(UUID key, PendingLink value, long currentTime, long currentDuration) {
        return value.ttl().toNanos();
    }

    @Override
    public long expireAfterRead(UUID key, PendingLink value, long currentTime, long currentDuration) {
        return currentDuration;
    }
}
