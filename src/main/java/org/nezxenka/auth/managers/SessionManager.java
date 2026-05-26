package org.nezxenka.auth.managers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.nezxenka.auth.Auth;
import org.nezxenka.auth.data.PlayerData;

public class SessionManager {

    private final Map<UUID, PlayerData> authenticatedPlayers =
        new ConcurrentHashMap<>();
    private final Map<UUID, Integer> failedAttempts = new ConcurrentHashMap<>();
    private final Map<UUID, Long> loginTimeout = new ConcurrentHashMap<>();
    private final Map<UUID, Long> pending2FA = new ConcurrentHashMap<>();
    private final Map<UUID, String> pending2FAIp = new ConcurrentHashMap<>();

    public void addAuthenticated(Player player) {
        authenticatedPlayers.put(player.getUniqueId(), new PlayerData(player));
        failedAttempts.remove(player.getUniqueId());
        loginTimeout.remove(player.getUniqueId());
        pending2FA.remove(player.getUniqueId());
        pending2FAIp.remove(player.getUniqueId());
    }

    public void removeAuthenticated(Player player) {
        authenticatedPlayers.remove(player.getUniqueId());
    }

    public boolean isAuthenticated(Player player) {
        UUID uuid = player.getUniqueId();
        if (pending2FA.containsKey(uuid)) {
            return false;
        }
        PlayerData data = authenticatedPlayers.get(uuid);
        if (data == null) return false;
        if (
            System.currentTimeMillis() - data.getLastActivity() >
            Auth.getInstance().getConfigManager().getSessionTimeout() * 1000L
        ) {
            authenticatedPlayers.remove(uuid);
            return false;
        }
        data.updateActivity();
        return true;
    }

    public void setPending2FA(Player player, String ip) {
        UUID uuid = player.getUniqueId();
        pending2FA.put(uuid, System.currentTimeMillis());
        pending2FAIp.put(uuid, ip);
    }

    public boolean isPending2FA(Player player) {
        UUID uuid = player.getUniqueId();
        if (!pending2FA.containsKey(uuid)) return false;
        long elapsed =
            (System.currentTimeMillis() - pending2FA.get(uuid)) / 1000;
        int timeout = Auth.getInstance()
            .getConfig()
            .getInt("telegram.2fa.timeout", 60);
        if (elapsed > timeout) {
            pending2FA.remove(uuid);
            pending2FAIp.remove(uuid);
            Auth.getInstance().getTelegramBot().cancel2FA(uuid);
            player.kickPlayer("§cВремя подтверждения истекло.");
            return false;
        }
        return true;
    }

    public String getPending2FAIp(Player player) {
        return pending2FAIp.get(player.getUniqueId());
    }

    public void recordFailedAttempt(Player player) {
        failedAttempts.merge(player.getUniqueId(), 1, Integer::sum);
        if (
            failedAttempts.get(player.getUniqueId()) >=
            Auth.getInstance().getConfigManager().getMaxLoginAttempts()
        ) {
            player.kickPlayer(
                Auth.getInstance()
                    .getMessageManager()
                    .getRawMessage("max_attempts")
            );
            failedAttempts.remove(player.getUniqueId());
            removeAuthenticated(player);
            loginTimeout.remove(player.getUniqueId());
        }
    }

    public void resetFailedAttempts(Player player) {
        failedAttempts.remove(player.getUniqueId());
    }

    public void setLoginTimeout(Player player) {
        loginTimeout.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public boolean isLoginTimeout(Player player) {
        if (!loginTimeout.containsKey(player.getUniqueId())) return false;
        long elapsed =
            (System.currentTimeMillis() -
                loginTimeout.get(player.getUniqueId())) / 1000;
        return (
            elapsed > Auth.getInstance().getConfigManager().getLoginTimeout()
        );
    }

    public void invalidateAll() {
        authenticatedPlayers.clear();
        failedAttempts.clear();
        loginTimeout.clear();
        pending2FA.clear();
        pending2FAIp.clear();
    }

    public void invalidate(Player player) {
        UUID uuid = player.getUniqueId();
        authenticatedPlayers.remove(uuid);
        failedAttempts.remove(uuid);
        loginTimeout.remove(uuid);
        pending2FA.remove(uuid);
        pending2FAIp.remove(uuid);
        Auth.getInstance().getTelegramBot().cancel2FA(uuid);
    }
}
