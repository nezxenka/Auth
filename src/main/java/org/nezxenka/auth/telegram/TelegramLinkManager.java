package org.nezxenka.auth.telegram;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.nezxenka.auth.Auth;

public class TelegramLinkManager {

    private final Random random = new Random();
    private final Auth plugin = Auth.getInstance();
    private final Map<String, Long> pendingLinks = new ConcurrentHashMap<>();

    public String generateLinkCode(String uuid) {
        StringBuilder code = new StringBuilder();
        int length = plugin.getConfig().getInt("telegram.link-code-length", 8);
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        plugin.getDatabaseManager().saveLinkCode(uuid, code.toString());
        return code.toString();
    }

    public void setPendingTelegramId(String uuid, long telegramId) {
        pendingLinks.put(uuid, telegramId);
    }

    public Long getPendingTelegramId(String uuid) {
        return pendingLinks.get(uuid);
    }

    public void clearPendingLink(String uuid) {
        pendingLinks.remove(uuid);
    }

    public boolean isLinked(String uuid) {
        return plugin.getDatabaseManager().isTelegramLinkedByUUID(uuid);
    }

    public void unlink(String uuid) {
        plugin.getDatabaseManager().set2FAEnabled(uuid, false);
        plugin.getDatabaseManager().unlinkTelegram(uuid);
    }

    public String getNicknameByTelegramId(long telegramId) {
        String uuid = plugin
            .getDatabaseManager()
            .getUUIDByTelegramId(telegramId);
        if (uuid == null) return null;
        OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
        return player.getName();
    }
}
