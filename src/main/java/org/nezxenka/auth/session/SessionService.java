package org.nezxenka.auth.session;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

public final class SessionService {

    private static final Duration PREPARED_SESSION_TTL = Duration.ofMinutes(1);

    private final Map<UUID, PlayerSession> active = new ConcurrentHashMap<>();
    private final Cache<UUID, PlayerSession> prepared = Caffeine.newBuilder()
        .expireAfterWrite(PREPARED_SESSION_TTL)
        .build();

    public void prepare(PlayerSession session) {
        prepared.put(session.getUuid(), session);
    }

    public void discardPrepared(UUID uuid) {
        prepared.invalidate(uuid);
    }

    public PlayerSession activate(UUID uuid) {
        PlayerSession session = prepared.asMap().remove(uuid);
        if (session != null) {
            active.put(uuid, session);
        }
        return session;
    }

    public void register(PlayerSession session) {
        active.put(session.getUuid(), session);
    }

    public PlayerSession get(UUID uuid) {
        return active.get(uuid);
    }

    public PlayerSession remove(UUID uuid) {
        return active.remove(uuid);
    }

    public boolean isOnline(UUID uuid) {
        return active.containsKey(uuid);
    }

    public boolean isCurrent(Player player, PlayerSession session) {
        return player.isOnline() && active.get(player.getUniqueId()) == session;
    }

    public Collection<PlayerSession> all() {
        return active.values();
    }
}
